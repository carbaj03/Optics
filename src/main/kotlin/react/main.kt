package react

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import arrow.optics.optics
import chef.Info
import chef.invoke
import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent

@OptIn(ExperimentalMaterialApi::class)
fun main() = application {
  Window(
    state = rememberWindowState(size = DpSize(1200.dp, 1200.dp)),
    onCloseRequest = ::exitApplication,
  ) {
    MaterialTheme(
      colors = MaterialTheme.colors.copy(
        primary = Color.Black,
        secondary = Color.Black,
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
      )
    ) {
      val state by app.collectAsState()

      when (val screen = state.screen) {
        null -> {
          Text("null")
        }
        is Home -> {
//          LaunchedEffect(Unit) {
//            screen.load()
//          }

          Scaffold() {
            Column(
              modifier = Modifier.padding(8.dp)
            ) {
//              ChipShowCase()

              Row {
                screen.load()
                screen.cancel()
              }

              var search by remember { mutableStateOf("") }
              var leadingIcon: @Composable (() -> Unit)? by remember { mutableStateOf(null) }

              OutlinedTextField(
                modifier = Modifier.onFocusChanged {
                  leadingIcon = if (!it.hasFocus) null else @Composable {
                    {
                      Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                      )
                    }
                  }
                },
                value = search,
                onValueChange = { search = it },
                shape = CircleShape,
                leadingIcon = leadingIcon,
              )

              var filter by remember { mutableStateOf(Filter()) }
              ChipGroup(filter, { filter = it.onSelected(filter) })

              val predicate: (Info) -> Boolean = {
                filter.noneSelected()
                        || filter.xef.isSeclected && it is Info.Conversation
                        || filter.openAI.isSeclected && it is Info.OpenAI
                        || filter.agent.isSeclected && it is ReactInfo
              }
              val Content = state.track.filter(predicate)
              Content()
            }
          }
        }
      }
    }
  }
}

data class ChipSelectable(
  val text: String,
  val icon: Icon,
  val isSeclected: Boolean,
  val onSelected: (Filter) -> Filter
)

enum class Icon {
  Agent, OpenAI, Xef,
}

@optics
data class Filter(
  val agent: ChipSelectable = ChipSelectable(text = "Agent", icon = Icon.Agent, isSeclected = false, onSelected = { it.copy(agent = it.agent.copy(isSeclected = !it.agent.isSeclected)) }),
  val openAI: ChipSelectable = ChipSelectable(text = "OpenAI", icon = Icon.OpenAI, isSeclected = false, onSelected = { it.copy(openAI = it.openAI.copy(isSeclected = !it.openAI.isSeclected)) }),
  val xef: ChipSelectable = ChipSelectable(text = "Xef", icon = Icon.Xef, isSeclected = false, onSelected = { it.copy(xef = it.xef.copy(isSeclected = !it.xef.isSeclected)) }),
) {
  val list = listOf(agent, openAI, xef)
  fun noneSelected() = list.none { it.isSeclected }

  companion object
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChipGroup(
  filter: Filter,
  filterChange: (ChipSelectable) -> Unit
) {
  Row {
    filter.list.forEach {
      FilterChip(
        selected = it.isSeclected,
        onClick = { filterChange(it) },
        colors = ChipDefaults.outlinedFilterChipColors(),
        border = ChipDefaults.outlinedBorder,
      ) {
        Text(it.text)
      }
      Spacer(modifier = Modifier.width(8.dp))
    }
  }
}

sealed interface ReactInfo : Info {
  val content: String

  data class Thinking(override val content: String) : ReactInfo
  data class Search(override val content: String) : ReactInfo
  data class Observation(override val content: String) : ReactInfo
}

@OptIn(BetaOpenAI::class)
@Composable
operator fun List<Info>.invoke(
  modifier: Modifier = Modifier
) {
  var detail: Info? by remember { mutableStateOf(null) }
  Row {
    LazyColumn(modifier = modifier.weight(1f)) {
      items(this@invoke) { info ->
        when (info) {
          is ReactInfo -> info(onSelected = { detail = it })
          is Info.OpenAI -> info(onSelected = { detail = it })
          is Info.Conversation -> info(onSelected = { detail = it })
        }
        Divider()
      }
    }
    Column(Modifier.weight(1f)) {
      when (val info = detail) {
        is ReactInfo -> Text(text = info.content)
        is Info.OpenAI -> Text(text = info.event.toString())
        is Info.Conversation -> Text(text = info.message.toString())
        null -> {}
      }
    }
  }
}

@Composable
operator fun ReactInfo.invoke(
  onSelected: (Info) -> Unit
) {
  MyColumn(onClick = { onSelected(this) }) { show ->
    when (this) {
      is ReactInfo.Search -> AgentItem(
        show = show,
        content = content,
        title = "Agent",
        text = "🔍"
      )
      is ReactInfo.Thinking -> AgentItem(
        show = show,
        content = content,
        title = "Agent",
        text = "🤔"
      )
      is ReactInfo.Observation -> AgentItem(
        show = show,
        content = content,
        title = "Agent",
        text = "👀"
      )
    }
  }
}

@Composable
fun AgentItem(
  show: Boolean,
  content: String,
  title: String,
  text: String,
) {
  Row(modifier = Modifier.padding(20.dp)) {
    Budget(text = title, color = Color.Blue)
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = text, color = Color.Gray)
  }
  AnimatedVisibility(show) {
    Column {
      Text(text = content, color = Color.Black)
    }
  }
}

@Composable
operator fun Info.Conversation.invoke(
  onSelected: (Info) -> Unit
) {
  MyColumn(onClick = { onSelected(this) }) { show ->
    Row(modifier = Modifier.padding(20.dp)) {
      Budget(text = "Xef")
      Spacer(modifier = Modifier.width(4.dp))
      Text(text = "Context", color = Color.Gray)
    }
    AnimatedVisibility(show) {
      Column {
        message.forEach { Message ->
          Spacer(modifier = Modifier.height(8.dp))
          Message()
        }
      }
    }
  }
}

@OptIn(BetaOpenAI::class)
@Composable
operator fun Info.OpenAI.invoke(
  onSelected: (Info) -> Unit
) {
  MyColumn(onClick = { onSelected(this) }) { show ->
    when (event) {
      is OpenAiEvent.Chat.Chunk -> TODO()
      is OpenAiEvent.Chat.Request -> TODO()
      is OpenAiEvent.Chat.Response -> TODO()
      is OpenAiEvent.Chat.WithFunctionRequest -> {
        OpenAIItem(
          text = event.response.functionCall.toString() + "\n" + event.response.functions.toString() + "\n" + event.response.messages.toString(),
          show = show,
          type = Type.Request
        )
      }
      is OpenAiEvent.Chat.WithFunctionResponse -> {
        OpenAIItem(
          text = event.response.model.toString() + "\n" + event.response.choices.toString(),
          show = show,
          type = Type.Response
        )
      }
      is OpenAiEvent.Image.Request -> TODO()
      is OpenAiEvent.Image.Response -> TODO()
      is OpenAiEvent.Completion.Request -> TODO()
      is OpenAiEvent.Completion.Response -> TODO()
    }
  }
}

sealed interface Type {
  val name: String
  val color: Color

  data object Request : Type {
    override val name: String = "Request"
    override val color: Color = Color.Blue
  }

  data object Response : Type {
    override val name: String = "Response"
    override val color: Color = Color.Green
  }
}

@Composable
fun MyColumn(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  content: @Composable (Boolean) -> Unit
) {
  var show by remember { mutableStateOf(false) }
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clickable { show = !show; onClick() }
      .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
      .padding(8.dp)
  ) {
    content(show)
  }
}

@Composable
fun OpenAIItem(
  text: String,
  show: Boolean,
  type: Type
) {
  Row(modifier = Modifier.padding(20.dp)) {
    Budget("LLM", Color.Yellow)
    Budget(type.name, type.color)
    Text("Chat", color = Color.Blue)
    Spacer(modifier = Modifier.width(4.dp))
  }
  AnimatedVisibility(show) {
    Column {
      var lines by remember { mutableStateOf(1) }
      Text(
        modifier = Modifier.clickable { lines = if (lines == 1) Int.MAX_VALUE else 1 },
        text = text,
        color = Color.Gray,
        maxLines = lines
      )
    }
  }
}

@Composable
operator fun Info.Conversation.Message.invoke() {
  Row {
    when (this@invoke) {
      is Info.Conversation.Message.Assistant -> Budget(text = "Assistant", color = Color.Green)
      is Info.Conversation.Message.User -> Budget(text = "User", color = Color.Blue)
      is Info.Conversation.Message.System -> Budget(text = "System", color = Color.Red)
    }
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = content, color = Color.Black)
  }
}

@Composable
fun Budget(
  text: String,
  color: Color = MaterialTheme.colors.primary
) {
  Text(
    modifier = Modifier.background(color, RoundedCornerShape(4.dp)).padding(2.dp),
    text = text,
    style = MaterialTheme.typography.body2,
    color = MaterialTheme.colors.onPrimary,
  )
}
