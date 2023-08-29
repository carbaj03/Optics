@file:OptIn(ExperimentalTypeInference::class, BetaOpenAI::class)

package chef

import arrow.core.continuations.Raise
import arrow.core.continuations.effect
import arrow.core.continuations.fold
import arrow.optics.optics
import chat.Button
import chat.Text
import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.tracing.Messages
import common.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import react.toInfo
import with
import kotlin.experimental.ExperimentalTypeInference

@optics sealed interface Screen {
  companion object
}

@optics data class Splash(val next: () -> Unit) : Screen {
  companion object
}

@optics data class Home(
  val cuisine: ChipGroup,
  val ingredients: ChipGroup,
  val cookingMethod: ChipGroup,
  val allergens: List<String>,
  val dislikedIngredients: List<String>,
  val maxCalories: Slider,
  val generateRecipe: Button,
  val isLoading: Boolean = false,
  val recipePrompt: RecipePrompt? = null,
  val image: String? = null,
  val cancel: Button,
) : Screen {
  companion object
}

@optics data class ChefApp(
  val screen: Screen? = null,
  val user: String = "",
  val track: List<Info> = emptyList(),
) : State {
  companion object
}

interface Info {

  @optics data class Conversation(val message: List<Message>) : Info {

    @optics sealed interface Message {
      val content: String

      @optics data class User(override val content: String) : Message {
        companion object
      }

      @optics data class Assistant(override val content: String) : Message {
        companion object
      }

      @optics data class System(override val content: String) : Message {
        companion object
      }

      companion object
    }

    companion object
  }

  @optics data class OpenAI(val event: OpenAiEvent) : Info {
    companion object
  }

}

inline operator fun <A : State> ChefApp.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference crossinline f: context(Store<A>, ChefApp.Companion, CoroutineScope, Raise<Error>) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also {
    scope.launch { effect { f(it, this@invoke, scope, this) }.fold({ println(it) }, { println(it) }) }
  }.state

val app = ChefApp(
  initialState = ChefApp(),
) {

  val logger = LoggerImpl()

  screen set Splash(
    next = {
      launch {
        delay(2000)
        screen set with(logger, this) { Home() }
      }
    }
  )

  launch {
    logger.state.collect { event ->
      track transform {
        when (event) {
          is Messages -> it + event.toInfo()
          is OpenAiEvent -> it + event.toInfo()
          else -> it
        }
      }
    }
  }
}


context(Store<S>)
inline fun <S : State, A : Screen> screen(f: context(CoroutineScope) () -> A): A {
  val scope = CoroutineScope(SupervisorJob() + parent.coroutineContext)
  return f(scope)
}

context(Store<ChefApp>, ChefApp.Companion, Logger)
fun Home() = screen.home {
  user set "user"

  Home(
    cuisine = ChipGroup(
      title = "Cuisine",
      chips = Cuisine.values().map { Chip(text = it.name, selected = false) },
      onChipClick = { chip ->
        cuisine.chips transform { it.map { it.copy(selected = if (it.text == chip.text) !it.selected else false) } }
      }
    ),
    ingredients = ChipGroup(
      title = "Main Ingredient",
      chips = MainIngredient.values().map { Chip(text = it.name, selected = false) },
      onChipClick = { chip ->
        ingredients.chips transform { it.map { it.copy(selected = if (it.text == chip.text) !it.selected else false) } }
      }
    ),
    cookingMethod = ChipGroup(
      title = "Cooking Method",
      chips = CookingMethod.values().map { Chip(text = it.name, selected = false) },
      onChipClick = { chip ->
        cookingMethod.chips transform { it.map { it.copy(selected = if (it.text == chip.text) !it.selected else false) } }
      }
    ),
    allergens = listOf(
      "Dairy",
      "Eggs",
      "Fish",
      "Shellfish",
      "Tree Nuts",
      "Peanuts",
      "Wheat",
      "Soy",
    ),
    dislikedIngredients = listOf(
      "Dairy",
      "Eggs",
      "Fish",
      "Shellfish",
      "Tree Nuts",
      "Peanuts",
      "Wheat",
      "Soy",
    ),
    maxCalories = Slider(
      value = 2000f,
      onValueChange = { maxCalories.value set it },
    ),
    generateRecipe = Button(
      text = Text("Generate Recipe"),
      onClick = {
        launch {
          coroutineContext.cancelChildren()
          isLoading set true
          screen.home transform { it.copy(recipePrompt = null) }

          effect {
            val recipe = generateReceipt(
              cuisine = Cuisine.valueOf(cuisine.chips.get().first { it.selected }.text),
              mainIngredient = MainIngredient.valueOf(ingredients.chips.get().first { it.selected }.text),
              cookingMethod = CookingMethod.valueOf(cookingMethod.chips.get().first { it.selected }.text),
              allergens = listOf("peanuts"),
              dislikedIngredients = listOf("onions"),
              calorieLimit = 2000,
              prompt = "prompt",
            )
            isLoading set false
            recipePrompt set recipe
            image set image(recipe.title)

          }.fold({
            isLoading set false
            ingredients.chips transform { it.map { it.copy(selected = false) } }
            cuisine.chips transform { it.map { it.copy(selected = false) } }
            cookingMethod.chips transform { it.map { it.copy(selected = false) } }
            screen.home transform { it.copy(recipePrompt = null) }
          }, {})
        }
      }
    ),
    cancel = Button(
      text = Text("Cancel"),
      onClick = {
        isLoading set false
        coroutineContext.cancelChildren()
      }
    )
  )
}

@optics data class ChipGroup(
  val title: String,
  val chips: List<Chip>,
  val onChipClick: (Chip) -> Unit,
) {
  companion object
}

@optics data class Chip(
  val text: String,
  val selected: Boolean,
) {
  companion object
}

@optics data class Slider(
  val value: Float,
  val onValueChange: (Float) -> Unit,
) {
  companion object
}