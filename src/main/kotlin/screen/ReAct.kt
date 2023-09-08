package screen

import App
import arrow.optics.optics
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent
import com.xebia.functional.xef.reasoning.tools.ReactAgentEvents
import com.xebia.functional.xef.tracing.Messages
import com.xebia.functional.xef.tracing.Tracker
import com.xebia.functional.xef.tracing.createDispatcher
import common.Info
import common.Store
import common.toInfo
import components.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import react.toInfo
import screen
import track

@optics data class ReAct(
  override val topBar: TopBar,
  override val load: Button,
  override val cancel: Button,
  override val back: Button,
  override val search: SearchBox,
  override val filter: Filter,
  override val rows: List<Row<Info>>,
  override val info: List<Boolean>,
  override val isLoading: Boolean
) : GenericScreen {
  companion object
}

context(Store<App>, App.Companion)
fun createReAct(): ReAct = screen.genericScreen.reAct {
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
        is ReactAgentEvents -> show(event.toInfo())
        is Messages -> show(event.toInfo())
        is OpenAiEvent -> show(event.toInfo())
        else -> {}
      }
    }
  }

  val msg = Tracker<Messages> { launch { log(this@Tracker) } }
  val react = Tracker<ReactAgentEvents> { launch { log(this@Tracker) } }
  val openAI = Tracker<OpenAiEvent> { launch { log(this@Tracker) } }

  ReAct(
    topBar = TopBar(
      title = Text("ReAct"),
      navigationIcon = Button(
        onClick = {
          screen set createHome()
          coroutineContext.cancelChildren()
        },
        text = Text("Back")
      ),
    ),
    load = Button(
      text = Text(text = "Prompt"),
      onClick = {
        track set emptyList()
        info set emptyList()
        rows set emptyList()
        isLoading set true

        launch {
          OpenAI.conversation(dispatcher = createDispatcher(msg, react, openAI, Tracker.Default)) {
            val model = OpenAI().DEFAULT_CHAT
            val serialization = OpenAI().DEFAULT_SERIALIZATION
            val math = LLMTool.create(
              name = "Calculator",
              description = "Perform math operations and calculations processing them with an LLM model. The tool input is a simple string containing the operation to solve expressed in numbers and math symbols.",
              model = model,
              scope = this
            )
            val search = Search(model = model, scope = this)

            val reActAgent = ReActAgent(
              model = serialization,
              scope = this,
              tools = listOf(search, math),
            )
            val result = reActAgent.run(
              Prompt {
                +user("Find and multiply the number of Leonardo di Caprio's girlfriends by the number of Metallica albums")
              }
            )
            isLoading set false
          }
        }
      }
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
      onValueChange = { text -> search.text set Text(text) }
    ),
    filter = Filter(),
    rows = emptyList(),
    info = emptyList(),
    isLoading = false
  )
}