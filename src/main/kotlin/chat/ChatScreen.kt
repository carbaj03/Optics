package chat

import arrow.core.continuations.Raise
import arrow.optics.optics
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.tracing.createDispatcher
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore
import common.*
import kotlinx.coroutines.*

@optics data class ChatScreen(
  val toolbar: Toolbar,
  val messages: List<Message>,
  val input: Input,
  val sideBar: SideBar,
  val send: ButtonIcon,
) : Screen {
  companion object
}

context(Store<S>)
inline fun <S : State, A : Screen> screen(f: CoroutineScope.() -> A): A {
  val scope = CoroutineScope(SupervisorJob() + parent.coroutineContext)
  return f(scope)
}

interface ChatRepository {
  suspend fun prompt(question: String): Message
}

val chatRepository = object : ChatRepository {
  val model = OpenAI(dispatcher = createDispatcher()).DEFAULT_CHAT
  var conversation: Conversation = Conversation(LocalVectorStore(OpenAIEmbeddings(OpenAI.FromEnvironment.DEFAULT_EMBEDDING)))

  override suspend fun prompt(question: String): Message =
    with(conversation) {
      AssitantMessage(model.promptMessage(Prompt( question), this))
    }
}


fun extractCode(input: String) : String {
  val regex = "```kotlin(.*?)```".toRegex(RegexOption.DOT_MATCHES_ALL)
  val matchResults = regex.findAll(input)
var code = ""
  for (matchResult in matchResults) {
    val codeBlock = matchResult.groupValues[1]
     code = codeBlock
  }
  return  code
}


fun main(args: Array<String>) {
  val input = "Some text before the code block.\n```kotlin\nval s = \"saasdf\"\n```\nSome text after the code block."
  val regex = "```kotlin(.*?)```".toRegex(RegexOption.DOT_MATCHES_ALL)
  val matchResults = regex.findAll(input)

  for (matchResult in matchResults) {
    val codeBlock = matchResult.groupValues[1]
    println("Extracted code block:\n$codeBlock")
  }
}

context(Store<ChatApp>, ChatApp.Companion, Raise<Error>)
fun ChatScreen(
  repository: ChatRepository = chatRepository,
): ChatScreen = screen {
  var input by screen.chatScreen.input.text
  var msg by screen.chatScreen.messages

  ChatScreen(
    toolbar = Toolbar(
      title = Text("Chat"),
      subtitle = Text("Subtitle"),
      itemLeft = ItemLeft(
        icon = Icon.Menu(Color.White),
        onClick = { screen.chatScreen.sideBar.open transform { !it } }
      ),
    ),
    input = Input(
      text = "",
      onTextChange = { input = it },
      action = ButtonIcon(
        icon = Icon.DocumentScearch(Color.White),
        onClick = {
          val old = input
          input = ""
          msg = msg + UserMessage(old)
          launch { msg = msg + repository.prompt(old) }
        }
      )
    ),
    send = ButtonIcon(
      icon = Icon.Send(Color.Red),
      onClick = {
          coroutineContext.cancelChildren()
      }
    ),
    messages = emptyList(),
    sideBar = SideBar(
      header = SideBarHeader(
        title = Text("Title"),
        subtitle = Text("Subtitle"),
        icon = Icon.DocumentScearch(Color.Red),
        plan = Text("Free")
      ),
      items = listOf(),
      onChange = { screen.chatScreen.sideBar.open set it }
    ),
  )
}
