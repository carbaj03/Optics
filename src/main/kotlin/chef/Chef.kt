package chef

import arrow.core.continuations.Raise
import arrow.core.continuations.catch
import arrow.optics.optics
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import com.xebia.functional.xef.conversation.llm.openai.log
import com.xebia.functional.xef.tracing.*
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.lang.Infer
import common.AIError
import common.Error
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

enum class Cuisine {
  Italian,
  Indian,
  Chinese,
  Mediterranean,
  Vegan,
  Keto,
  Paleo,
  Infer
}

enum class MainIngredient {
  Chicken,
  Beef,
  Tofu,
  Mushroom,
  Lentils,
  Shrimp,
  Infer
}

enum class CookingMethod {
  Bake,
  Fry,
  Steam,
  Saute,
  Grill,
  SlowCook,
  Infer
}

@Serializable data class Ingredient(
  val name: String,
  val quantity: String,
  val unit: String? = null
)

@optics @Serializable data class RecipeState(
  val title: String,
  val cuisine: Cuisine,
  val mainIngredient: MainIngredient,
  val cookingMethod: CookingMethod,
  val ingredients: List<Ingredient>,
  val description: String,
  val steps: List<String>,
  val totalTime: Int,
//  val calories: Int,
) {
  companion object
}

@optics @Serializable data class DietaryConstraints(
  val allergens: List<String>,
  val dislikedIngredients: List<String>,
  @Description("limit per srving") val calorieLimit: Int
) {
  companion object
}

@optics @Serializable data class GenerateRecipe(
  val state: RecipeState,
  val constraints: DietaryConstraints
) {
  companion object
}

@Serializable data class RecipePrompt(
  val title: String,
  val ingredients: List<Ingredient>,
  val prepTime: String, // in minutes
  val cookTime: String, // in minutes
  val servings: Int,
  val steps: List<String>,
  val notes: String? = null,
//  val calories: Int,
)

context(Raise<Error>)
suspend fun image(title: String): String =
  catch(action = {
    OpenAI.conversation {
      val images = OpenAI.FromEnvironment.DEFAULT_IMAGES.images(Prompt(title))
      images.data.first().url
    }
  },
    {
      raise(AIError(it.message.toString()))
    }
  )

//interface Logger {
//  val state: SharedFlow<Event>
//  suspend fun log(message: Event)
//}

interface Logger {
  val state: StateFlow<Event>
  suspend fun log(message: Event)
}

sealed interface Tracing
object Empty : Event
data class MessageTracing(val message: Message) : Tracing

//class LoggerImpl : Logger {
//  val _state : MutableSharedFlow<Event> = MutableSharedFlow(2)
//
//  override val state: SharedFlow<Event> = _state
//
//  override suspend fun log(message: Event) {
//    _state.emit(message)
//  }
//}

class LoggerImpl : Logger {
  val _state : MutableStateFlow<Event> = MutableStateFlow(Empty)

  override val state: StateFlow<Event> = _state

  override suspend fun log(message: Event) {
    _state.value = message
  }
}

context(Raise<Error> , Logger)
suspend fun generateReceipt(
  prompt: String,
  cuisine: Cuisine,
  mainIngredient: MainIngredient,
  cookingMethod: CookingMethod,
  allergens: List<String>,
  dislikedIngredients: List<String>,
  calorieLimit: Int
): RecipePrompt = coroutineScope {

  catch(action = {

    val tracker = Tracker<Messages> {
    launch {    log(this@Tracker)}
    }

    val openAI = Tracker<OpenAiEvent> {
      launch {    log(this@Tracker)}
    }

    OpenAI.conversation(createDispatcher(OpenAI.log, tracker, openAI,)) {
      val infer = Infer(OpenAI().DEFAULT_SERIALIZATION, conversation)
      infer<GenerateRecipe, RecipePrompt>(
        Prompt(
          """
           Assume the role of a world-class chef. Your task is to create unique and delicious recipes tailored 
           to specific dietary constraints and preferences using the inputs provided. 
        """.trimIndent(),
          configuration = PromptConfiguration(minResponseTokens = 1000, maxDeserializationAttempts = 1)
        )
      ) {
        GenerateRecipe(
          state = RecipeState(
            title = inferString,
            cuisine = cuisine,
            mainIngredient = mainIngredient,
            cookingMethod = cookingMethod,
            ingredients = listOf(
              Ingredient(name = inferString, quantity = inferString, unit = inferString),
              Ingredient(name = inferString, quantity = inferString, unit = inferString)
            ),
            description = inferString,
            steps = listOf(inferString, inferString, inferString),
            totalTime = inferInt,
//          calories = inferInt,
          ),
          constraints = DietaryConstraints(
            allergens = allergens,
            dislikedIngredients = dislikedIngredients,
            calorieLimit = calorieLimit
          )
        )
      }
    }
  }, {
    raise(AIError(it.message.toString()))
  })
}