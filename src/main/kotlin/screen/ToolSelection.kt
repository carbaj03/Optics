package screen

import App
import arrow.optics.optics
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import com.xebia.functional.xef.reasoning.filesystem.Files
import com.xebia.functional.xef.reasoning.pdf.PDF
import com.xebia.functional.xef.reasoning.pdf.ReadPDFTracing
import com.xebia.functional.xef.reasoning.text.summarize.SummarizeTracing
import com.xebia.functional.xef.reasoning.tools.ToolSelectionTracing
import com.xebia.functional.xef.tracing.Messages
import com.xebia.functional.xef.tracing.Tracker
import com.xebia.functional.xef.tracing.createDispatcher
import common.Info
import common.Store
import common.toInfo
import components.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import screen
import toolselection.toInfo
import track

@optics data class ToolSelection(
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

context(Store<App>, App.Companion)
fun createToolSelection(): ToolSelection = screen.genericScreen.toolSelection {
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
        is ToolSelectionTracing -> show(event.toInfo())
        is SummarizeTracing -> show(event.toInfo())
        is ReadPDFTracing -> show(event.toInfo())
        else -> {}
      }
    }
  }

  val tracing = Tracker<ToolSelectionTracing> {
    launch { log(this@Tracker) }
  }

  val summary = Tracker<SummarizeTracing> {
    launch { log(this@Tracker) }
  }

  val pdf = Tracker<ReadPDFTracing> {
    launch { log(this@Tracker) }
  }

  val openAI = Tracker<OpenAiEvent> {
    launch { log(this@Tracker) }
  }

  val msg = Tracker<Messages> { launch { log(this@Tracker) } }

  ToolSelection(
    topBar = TopBar(
      title = Text("Tool Selection"),
      navigationIcon = Button(
        onClick = {
          launch { screen set createHome() }
          cancel()
        },
        text = Text("Back")
      ),
    ),
    load = Button(
      text = Text(text = "Load"),
      onClick = {
        track set emptyList()
        info set emptyList()
        rows set emptyList()
        isLoading set true

        launch {
          OpenAI.conversation(createDispatcher(tracing, summary, pdf, openAI, msg)) {
            val model = OpenAI().DEFAULT_CHAT
            val serialization = OpenAI().DEFAULT_SERIALIZATION
            val text = com.xebia.functional.xef.reasoning.text.Text(model = model, scope = this)
            val files = Files(model = serialization, scope = this)
            val pdf = PDF(chat = model, model = serialization, scope = this)

            val toolSelection = com.xebia.functional.xef.reasoning.tools.ToolSelection(
              model = serialization,
              scope = this,
              tools = listOf(
                text.summarize,
                pdf.readPDFFromUrl,
                files.readFile,
                files.writeToTextFile,
              ),
            )

            toolSelection.applyInferredTools("Extract information from https://arxiv.org/pdf/2305.10601.pdf")
            isLoading set false
          }
        }
      }
    ),
    cancel = Button(
      onClick = { cancel() },
      text = Text("Cancel")
    ),
    back = Button(
      onClick = {
        screen set createHome()
        cancel()
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
    info = emptyList(),
  )
}