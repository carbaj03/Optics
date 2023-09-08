package screen

import App
import arrow.optics.optics
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.tools.ReactAgentEvents
import com.xebia.functional.xef.tracing.*
import common.Info
import common.Store
import common.toInfo
import components.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import screen
import track

@optics data class ColorsScreen(
  override val topBar: TopBar,
  override val load: Button,
  override val cancel: Button,
  override val back: Button,
  override val search: SearchBox,
  override val filter: Filter,
  override val rows: List<Row<Info>>,
  override val info: List<Boolean>,
  override val isLoading: Boolean = false,
) : GenericScreen {
  companion object
}

@Serializable data class Colors(val colors: List<String>)

data class Row<A>(val value: A, val onClick: () -> Unit, val show: Boolean)

context(Store<App>, App.Companion)
fun createColors(): ColorsScreen = screen.genericScreen.colorsScreen {

  val show = { conversation: Info ->
    track transform { it + conversation }
    info transform { it + false }
    rows transform {
      it + Row(
        value = conversation,
        onClick = {
          info transform { it.map { !it } }
          rows transform { it.map { row -> if (row.value == conversation) row.copy(show = !row.show) else row } }
        },
        show = false
      )
    }
  }

  parent.launch {
    event.collect { event ->
      when (event) {
        is Messages -> show(event.toInfo())
        is OpenAiEvent -> show(event.toInfo())
        is ImagesRequest -> show(event.toInfo())
        is ImagesResponse -> show(event.toInfo())
        else -> {}
      }
    }
  }

  val msg = Tracker<Messages> { launch { log(this@Tracker) } }
  val react = Tracker<ReactAgentEvents> { launch { log(this@Tracker) } }
  val openAI = Tracker<OpenAiEvent> { launch { log(this@Tracker) } }
  val imageRequest = Tracker<ImagesRequest> { launch { log(this@Tracker) } }
  val imageResponse = Tracker<ImagesResponse> { launch { log(this@Tracker) } }

  ColorsScreen(
    topBar = TopBar(
      title = Text("Colors"),
      navigationIcon = Button(
        onClick = {
          screen set createHome()
          coroutineContext.cancelChildren()
        },
        text = Text("Back")
      ),
    ),
    load = Button(
      onClick = {
        track set emptyList()
        info set emptyList()
        rows set emptyList()
        isLoading set true

        launch {
          OpenAI.conversation(createDispatcher(msg, react, openAI, imageRequest, imageResponse)) {
            addContext("I want to paint my room")
            val colors : Colors = prompt("a selection of 10 beautiful colors that go well together")
            OpenAI().DEFAULT_IMAGES.images(Prompt("Pain a picture with this colors ${colors.colors.joinToString(", ")}"))
            isLoading set false
          }
        }
      },
      text = Text("Prompt")
    ),
    cancel = Button(
      onClick = { coroutineContext.cancelChildren() },
      text = Text("Cancel")
    ),
    back = Button(
      onClick = {
        screen set createHome()
        coroutineContext.cancelChildren()
      },
      text = Text("Mode")
    ),
    search = SearchBox(
      text = Text(""),
      onValueChange = { text ->
        search.text set Text(text)
      }
    ),
    filter = Filter(),
    rows = emptyList(),
    info = emptyList()
  )
}
