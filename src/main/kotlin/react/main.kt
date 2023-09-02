package react

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import arrow.optics.optics
import chat.invoke
import chef.Info
import chef.invoke
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.FunctionMode
import com.example.compose.*
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import common.Green
import common.Pastel
import common.Peach
import common.Teal

@OptIn(ExperimentalMaterialApi::class)
fun main() = application {
  Window(
    state = rememberWindowState(size = DpSize(1200.dp, 1200.dp)),
    onCloseRequest = ::exitApplication,
  ) {
    MaterialTheme(
      colors = MaterialTheme.colors.copy(
        primary = md_theme_light_primary,
        primaryVariant = md_theme_light_primary,
        secondary = md_theme_light_secondary,
        secondaryVariant = md_theme_light_secondary,
        surface = md_theme_light_surface,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        onSurface = md_theme_light_onSurface,
        onPrimary = md_theme_light_onPrimary,
        onSecondary = md_theme_light_onSecondary,
        error = md_theme_light_error,
        onError = md_theme_light_onError,
        isLight = true,
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

              val Content = state.track.filter {
                filter.noneSelected()
                        || filter.xef.isSeclected && it is Info.Conversation
                        || filter.openAI.isSeclected && it is Info.OpenAI
                        || filter.agent.isSeclected && it is ReactInfo
              }
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

@OptIn(BetaOpenAI::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
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
    Column(modifier = Modifier.weight(1f).padding(8.dp).horizontalScroll(rememberScrollState())) {
      when (val info = detail) {
        is ReactInfo -> {
          info(onSelected = { detail = it })
        }
        is Info.OpenAI -> {
          when (info.event) {
            is OpenAiEvent.Chat.Chunk -> TODO()
            is OpenAiEvent.Chat.Request -> TODO()
            is OpenAiEvent.Chat.Response -> TODO()
            is OpenAiEvent.Chat.WithFunctionRequest -> {
              ColumnInfo("model", info.event.response.model.id)
              Spacer(dp = 8)
              ColumnInfo(
                label = "functionCall",
                text = when (val functionCall = info.event.response.functionCall) {
                  is FunctionMode.Default -> functionCall.value
                  is FunctionMode.Named -> functionCall.name
                  null -> ""
                }
              )
              Spacer(dp = 8)
              Text(text = "Functions", style = MaterialTheme.typography.h6)
              info.event.response.functions?.forEach {
                Column {
                  ColumnInfo(label = "name", text = it.name)
                  Spacer(dp = 8)
                  ColumnInfo(label = "description", text = it.description ?: "")
                  Spacer(dp = 8)
                  ColumnInfo(label = "params", text = it.parameters?.schema.toString())
                }
              }
              Spacer(dp = 8)
              Text(text = "Messages", style = MaterialTheme.typography.h6)
              info.event.response.messages.forEach {
                Row {
                  Budget(it.role.role, Color.Gray)
                  Spacer(dp = 4.dp)
                  Text(it.content ?: "", color = Color.Black)
                }
              }
              Spacer(dp = 8)
              Column {
                ColumnInfo("maxTokens", info.event.response.maxTokens.toString())
                Spacer(dp = 8)
                ColumnInfo("temperature", info.event.response.temperature.toString())
                Spacer(dp = 8)
                ColumnInfo("topP", info.event.response.topP.toString())
                Spacer(dp = 8)
                ColumnInfo("n", info.event.response.n.toString())
                Spacer(dp = 8)
                ColumnInfo("stop", info.event.response.stop.toString())
                Spacer(dp = 8)
                ColumnInfo("presencePenalty", info.event.response.presencePenalty.toString())
                Spacer(dp = 8)
                ColumnInfo("frequencyPenalty", info.event.response.frequencyPenalty.toString())
              }
            }
            is OpenAiEvent.Chat.WithFunctionResponse -> {
              ColumnInfo("model", info.event.response.model.id)
              Spacer(dp = 8)
              Column(modifier = Modifier.background(Pastel)) {
                Text(text = "Choices", style = MaterialTheme.typography.h6)
                info.event.response.choices.forEach {
                  Row {
                    Budget(it.message?.role?.role.toString(), Color.Gray)
                    Spacer(dp = 4.dp)
                    Text(it.message?.content ?: "", color = Color.Black)
                    Spacer(dp = 4.dp)
                    it.message?.functionCall?.let {
                      Column {
                        ColumnInfo(label = "name", text = it.name.toString())
                        Spacer(dp = 8)
                        ColumnInfo(label = "description", text = it.arguments.toString())
                      }
                    }
                    Spacer(dp = 4.dp)
                    Text(it.message?.name.toString(), color = Color.Black)
                  }
                  ColumnInfo(label = "index", text = it.index.toString())
                  Spacer(dp = 8)
                  ColumnInfo(label = "finishReason", text = it.finishReason.toString())
                }
              }
              Spacer(dp = 8)
              ColumnInfo("created", info.event.response.created.toString())
              Spacer(dp = 8)
              ColumnInfo("id", info.event.response.id)
              Spacer(dp = 8)
              ColumnInfo("usage", info.event.response.usage.toString())
            }
            is OpenAiEvent.Completion.Request -> TODO()
            is OpenAiEvent.Completion.Response -> TODO()
            is OpenAiEvent.Image.Request -> TODO()
            is OpenAiEvent.Image.Response -> TODO()
            is OpenAiEvent.Embedding.Request -> TODO()
            is OpenAiEvent.Embedding.Response -> TODO()
          }
        }
        is Info.Conversation -> {
          info.message()
        }
        null -> {}
      }
    }
  }
}

@Composable
fun ColumnInfo(label: String, text: String, color: Color = Color.Transparent) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(text = label, style = MaterialTheme.typography.subtitle2)
    Spacer(dp = 8.dp)
    BudgetInfo(text = text, color = color, border = Color.Black)
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
  Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
    Budget(text = title, color = Pastel)
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
  MyColumn(
    onClick = { onSelected(this) }
  ) { show ->
    HeadreRow(
      show = show,
      budgets = listOf(Budget.XEF),
      title = "Context",
      tokens = totalTokens
    ) {
      Divider()
      message()
    }
  }
}

@Composable
operator fun List<Info.Conversation.Message>.invoke() {
  Column(modifier = Modifier.padding(10.dp)) {
    forEach { Message ->
      Spacer(modifier = Modifier.height(8.dp))
      Message()
    }
  }
}

@Composable
fun Tokens(tokens: Int) {
  BudgetInfo(
    text = "$tokens tokens",
    color = Color.Transparent,
    border = Color.Black
  )
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
          text = event.response.functionCall.toString() + "\n" + event.response.functions.toString() + "\n" + event.response.messages.toString() + "\n" + event.response.model.toString(),
          show = show,
          type = Type.Request,
          tokens = event.tokensFromMessages
        )
      }
      is OpenAiEvent.Chat.WithFunctionResponse -> {
        OpenAIItem(
          text = event.response.model.toString() + "\n" + event.response.choices.toString(),
          show = show,
          type = Type.Response,
          tokens = event.response.usage?.totalTokens ?: 0
        )
      }
      is OpenAiEvent.Image.Request -> TODO()
      is OpenAiEvent.Image.Response -> TODO()
      is OpenAiEvent.Completion.Request -> TODO()
      is OpenAiEvent.Completion.Response -> TODO()
      is OpenAiEvent.Embedding.Request -> TODO()
      is OpenAiEvent.Embedding.Response -> TODO()
    }
  }
}

sealed interface Type {
  val name: String
  val color: Color

  data object Request : Type {
    override val name: String = "Request"
    override val color: Color = Teal
  }

  data object Response : Type {
    override val name: String = "Response"
    override val color: Color = Green
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
      .background(if (show) Color.LightGray.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
  ) {
    content(show)
  }
}

@Composable
fun HeadreRow(
  show: Boolean,
  title: String,
  budgets: List<Budget>,
  tokens: Int,
  content: @Composable ColumnScope.() -> Unit,
) {
  Row(
    modifier = Modifier.padding(20.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (show) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
      contentDescription = null,
      tint = Color.Gray,
    )
    Spacer(dp = 4.dp)
    budgets.forEach {
      Budget(text = it.text, color = it.background(), border = it.border())
      Spacer(dp = 4.dp)
    }
    Text(text = title)
    Spacer(dp = 20.dp)
    Tokens(tokens)
  }
  AnimatedVisibility(show) {
    Column(modifier = Modifier.padding(10.dp)) {
      Divider()
      content()
    }
  }
}

@Composable
fun OpenAIItem(
  text: String,
  show: Boolean,
  type: Type,
  tokens: Int
) {
  HeadreRow(
    show = show,
    budgets = listOf(Budget.LLM, Budget.OpenAI, if (type == Type.Request) Budget.Request else Budget.Response),
    title = "Chat",
    tokens = tokens
  ) {
    Text(text = text, color = Color.Gray)
  }
}

@Composable
fun RowScope.Spacer(dp: Dp) {
  Spacer(modifier = Modifier.width(dp))
}

@Composable
fun RowScope.Spacer(weight: Float) {
  Spacer(modifier = Modifier.weight(weight))
}

@Composable
fun ColumnScope.Spacer(dp: Int) {
  Spacer(modifier = Modifier.height(dp.dp))
}

@Composable
operator fun Info.Conversation.Message.invoke() {
  Row {
    when (this@invoke) {
      is Info.Conversation.Message.Assistant -> Budget(text = "Assistant", color = Peach)
      is Info.Conversation.Message.User -> Budget(text = "User", color = Green)
      is Info.Conversation.Message.System -> Budget(text = "System", color = Teal)
    }
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = content, color = Color.Black)
  }
}

interface Budget {
  val text: String
  val background: chat.Color
  val border: chat.Color

  data object XEF : Budget {
    override val text: String = "XEF"
    override val background: chat.Color = chat.Color.Pastel
    override val border: chat.Color = chat.Color.Pastel
  }

  data object LLM : Budget {
    override val text: String = "LLM"
    override val background: chat.Color = chat.Color.Pastel
    override val border: chat.Color = chat.Color.Pastel
  }

  data object OpenAI : Budget {
    override val text: String = "OpenAI"
    override val background: chat.Color = chat.Color.Teal
    override val border: chat.Color = chat.Color.Teal
  }

  data object Request : Budget {
    override val text: String = "Request"
    override val background: chat.Color = chat.Color.Teal
    override val border: chat.Color = chat.Color.Teal
  }

  data object Response : Budget {
    override val text: String = "Response"
    override val background: chat.Color = chat.Color.Green
    override val border: chat.Color = chat.Color.Green
  }
}

@Composable
fun Budget(
  text: String,
  color: Color = MaterialTheme.colors.primary,
  border: Color = MaterialTheme.colors.onPrimary,
) {
  Text(
    modifier = Modifier
      .border(width = 1.dp, color = border, shape = CircleShape)
      .background(color, CircleShape)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    text = text,
    style = MaterialTheme.typography.caption,
    color = MaterialTheme.colors.onSecondary,
  )
}

@Composable
fun BudgetInfo(
  text: String,
  color: Color = MaterialTheme.colors.primary,
  border: Color = MaterialTheme.colors.onPrimary,
) {
  Text(
    modifier = Modifier
      .border(1.dp, border, CircleShape)
      .background(color, CircleShape)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    text = text,
    style = MaterialTheme.typography.caption,
    color = MaterialTheme.colors.primary
  )
}
