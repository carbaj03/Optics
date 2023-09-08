package react

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import arrow.optics.optics
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionFunction
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.FunctionCall
import com.aallam.openai.api.chat.FunctionMode
import com.aallam.openai.api.model.ModelId
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import common.Info
import components.*
import screen.Row
import toolselection.ToolSelectionInfo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChipGroup(
  filter: Filter,
  filterChange: (ChipSelectable) -> Unit
) {
  Row {
    filter.list.forEach {
      FilterChip(
        selected = it.isSelected,
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

@Composable
operator fun List<Row<Info>>.invoke(
  modifier: Modifier = Modifier,
  state: LazyListState,
  isLoading: Boolean,
) {
  LazyColumn(modifier = modifier, state = state) {
    items(this@invoke) {
      when (val Info = it.value) {
        is ToolSelectionInfo -> Info()
        is ReactInfo -> Info()
        is Info.OpenAI -> Info(it.show, it.onClick)
        is Info.Xef.Conversation -> Info(it.show, it.onClick)
        is Info.Xef.Images.Request -> Info(it.show, it.onClick)
        is Info.Xef.Images.Response -> Info(it.show, it.onClick)
      }
      Divider()
    }
    item {
      if(isLoading)
        CircularProgressIndicator()
    }
  }
}

@Composable
operator fun ToolSelectionInfo.invoke() {
  MyColumn(onClick = onClick) { show ->
    when (this) {
      is ToolSelectionInfo.Trace -> content(show)
    }
  }
}

@Composable
operator fun ReactInfo.invoke() {
  MyColumn(onClick = onClick) { show ->
    when (this) {
      is ReactInfo.Search -> content(show)
      is ReactInfo.Thinking -> content(show)
      is ReactInfo.Observation -> content(show)
      is ReactInfo.FinalAnswer -> content(show)
    }
  }
}

@optics data class AgentItem(
  val content: Text,
  val budgets: List<Budget>,
) {
  companion object
}

@Composable
operator fun AgentItem.invoke(show: Boolean) {
  Row(
    modifier = Modifier.padding(20.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (show) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
      contentDescription = null,
      tint = Color.Gray,
    )
    Spacer(dp = 4)
    budgets.forEach { Budget ->
      Budget()
      Spacer(dp = 4)
    }
    Text(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
      text = content.text,
      color = Color.Black,
      maxLines = if (show) Int.MAX_VALUE else 1,
      style = content.style(),
    )
  }
}

@Composable
operator fun Info.Xef.Images.Request.invoke(show: Boolean, onClick: () -> Unit) {
  HeadreRow(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .background(Color.Transparent, RoundedCornerShape(8.dp)),
    show = show,
    budgets = listOf(Budget.Xef, Budget.Request, Budget.Image),
  ) {
    Text(size)
    Text(numberImages.toString())
    prompt.forEach { it() }
  }
}

@Composable
operator fun Info.Xef.Images.Response.invoke(show: Boolean, onClick: () -> Unit) {
  HeadreRow(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .background(Color.Transparent, RoundedCornerShape(8.dp)),
    show = show,
    budgets = listOf(Budget.Xef, Budget.Response, Budget.Image),
  ) {
    request.urls.forEach { Text("url : " + it.url) }
  }
}

@Composable
operator fun Info.Xef.Conversation.invoke(show: Boolean, onClick: () -> Unit) {
  HeadreRow(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .background(Color.Transparent, RoundedCornerShape(8.dp)),
    show = show,
    budgets = listOf(Budget.Xef, Budget.Context),
    tokens = totalTokens,
  ) {
    history("history")
    context("context")
    message("message")
  }
}

@JvmName("conversation")
@Composable
operator fun List<Info.Xef.Conversation.Message>.invoke(title: String) {
  Column(modifier = Modifier.padding(10.dp)) {
    Text(text = title, style = MaterialTheme.typography.caption)
    forEach { Message ->
      Spacer(4)
      Message()
    }
  }
}

@OptIn(BetaOpenAI::class)
@Composable
operator fun List<ChatMessage>.invoke(title: String) {
  Column(modifier = Modifier.padding(10.dp)) {
    Text(text = title, style = MaterialTheme.typography.caption)
    forEach {
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        when (it.role.role) {
          "system" -> Budget.System()
          "user" -> Budget.User()
          "assistant" -> Budget.Assistant()
          "function" -> Budget.Function()
        }
        Spacer(4)
        Text(it.content.toString())
      }
    }
  }
}

@JvmName("ChatCompletionFunction")
@OptIn(BetaOpenAI::class)
@Composable
operator fun List<ChatCompletionFunction>.invoke(title: String) {
  Column(modifier = Modifier.padding(10.dp)) {
    Text(text = title, style = MaterialTheme.typography.caption)
    forEach {
      Spacer(modifier = Modifier.height(8.dp))
      Text(it.name)
      it.description?.let { Text(it) }
      it.parameters?.let { Text(it.schema.toString()) }
    }
  }
}

@OptIn(BetaOpenAI::class)
@Composable
operator fun Info.OpenAI.invoke(show: Boolean, onClick: () -> Unit) {
  when (event) {
    is OpenAiEvent.Chat.Chunk -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Request, Budget.Chunk),
        tokens = event.value.usage?.totalTokens,
      ) {
        Text(event.value.toString())
      }
    }
    is OpenAiEvent.Chat.Request -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Request, Budget.Chat),
        tokens = event.tokensFromMessages,
      ) {
        event.value.messages(title = "messages")
        event.value.functions?.let { it(title = "functions") }
      }
    }
    is OpenAiEvent.Chat.Response -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Response, Budget.Chat),
        tokens = event.value.usage?.totalTokens,
      ) {
        event.value.choices.first().message?.content?.let { Text(it) }
        event.value.choices.first().message?.functionCall?.arguments?.let { Text(it) }
      }
    }
    is OpenAiEvent.Chat.WithFunctionRequest -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Request, Budget.ChatWithFunctions, event.value.model.Budget(), event.value.functionCall!!.Budget()),
        tokens = event.tokensFromMessages,
      ) {
        event.value.messages(title = "messages")
        event.value.functions?.let { it(title = "functions") }
      }
    }
    is OpenAiEvent.Chat.WithFunctionResponse -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Response, Budget.ChatWithFunctions, event.value.model.Budget(), event.value.choices.first().message?.functionCall!!.Budget()),
        tokens = event.value.usage?.totalTokens,
      ) {
        event.value.choices.first().message?.content?.let { Text(it) }
        event.value.choices.first().message?.functionCall?.arguments?.let { Text(it) }
      }
    }
    is OpenAiEvent.Image.Request -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Request, Budget.Image),
      ) {
        event.value.prompt.let { Text(it) }
        event.value.size?.let { Text(it.size) }
        event.value.n?.let { Text(it.toString()) }
      }
    }
    is OpenAiEvent.Image.Response -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Response, Budget.Image),
      ) {
        event.value.forEach {
          Text("url : " + it.url)
        }
      }
    }
    is OpenAiEvent.Completion.Request -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Request, Budget.Completion),
        tokens = event.tokensFromMessages,
      ) {
        Text(event.value.toString())
      }
    }
    is OpenAiEvent.Completion.Response -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Response, Budget.Completion),
        tokens = event.value.usage?.totalTokens,
      ) {
        Text(event.value.toString())
      }
    }
    is OpenAiEvent.Embedding.Request -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Request, Budget.Embedding),
      ) {
        Text(event.value.toString())
      }
    }
    is OpenAiEvent.Embedding.Response -> {
      HeadreRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
          .background(Color.Transparent, RoundedCornerShape(8.dp)),
        show = show,
        budgets = listOf(Budget.Llm, Budget.OpenAi, Budget.Response, Budget.Embedding),
      ) {
        Text(event.value.toString())
      }
    }
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
  budgets: List<Budget>,
  tokens: Int? = null,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Row(
    modifier = modifier.padding(20.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (show) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
      contentDescription = null,
      tint = Color.Gray,
    )
    Spacer(dp = 4)
    budgets.forEach { Budget ->
      Budget()
      Spacer(dp = 4)
    }
    Spacer(dp = 20)
    tokens?.let { Tokens(tokens) }
  }
  Divider()
  AnimatedVisibility(show) {
    Column(modifier = Modifier.padding(10.dp)) {
      content()
    }
  }
}

@Composable
fun ModelId.Budget(): Budget =
  Budget(text = TextBudget(id), background = components.Color.Transparent)

@OptIn(BetaOpenAI::class)
@Composable
fun FunctionMode.Budget(): Budget =
  when (this) {
    is FunctionMode.Default -> Budget(text = TextBudget(value), background = components.Color.Transparent)
    is FunctionMode.Named -> Budget(text = TextBudget(name), background = components.Color.Transparent)
  }

@Composable
fun FunctionCall.Budget(): Budget =
  Budget(text = TextBudget(name!!), background = components.Color.Transparent)

@Composable
operator fun Info.Xef.Conversation.Message.invoke() {
  Row {
    when (this@invoke) {
      is Info.Xef.Conversation.Message.Assistant -> budget()
      is Info.Xef.Conversation.Message.User -> budget()
      is Info.Xef.Conversation.Message.System -> budget()
    }
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = content, color = Color.Black)
  }
}