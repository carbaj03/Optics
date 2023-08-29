package react

import arrow.core.continuations.Raise
import arrow.core.continuations.effect
import arrow.core.continuations.fold
import chat.Button
import chat.Text
import chef.Info
import chef.LoggerImpl
import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent
import com.xebia.functional.xef.reasoning.tools.ReactAgentEvents
import com.xebia.functional.xef.tracing.Messages
import com.xebia.functional.xef.tracing.Tracker
import com.xebia.functional.xef.tracing.createDispatcher
import common.Error
import common.State
import common.Store
import common.StoreImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
inline operator fun <A : State> ReActApp.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference crossinline f: context(Store<A>, ReActApp.Companion, CoroutineScope, Raise<Error>) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also {
    scope.launch { effect { f(it, this@invoke, scope, this) }.fold({ println(it) }, { println(it) }) }
  }.state

@OptIn(BetaOpenAI::class)
val app = ReActApp(
  initialState = ReActApp(),
) {

  val screenContext = CoroutineScope(SupervisorJob(coroutineContext.job))
  screen set Home(
    load = Button(
      text = Text(text = "Load"),
      onClick = {
        track set emptyList()

        val logger = LoggerImpl()

        screenContext.launch {
          logger.state.collect { event ->
            track transform {
              when (event) {
                is ReactAgentEvents -> it + event.toInfo()
                is Messages -> it + event.toInfo()
                is OpenAiEvent -> it + event.toInfo()
                else -> it
              }
            }
          }
        }

        val msg = Tracker<Messages> {
          screenContext.launch { logger.log(this@Tracker) }
        }

        val react = Tracker<ReactAgentEvents> {
          screenContext.launch { logger.log(this@Tracker) }
        }

        val openAI = Tracker<OpenAiEvent> {
          screenContext.launch { logger.log(this@Tracker) }
        }

        screenContext.launch {
          OpenAI.conversation(dispatcher = createDispatcher(msg, react, openAI)) {
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
            println(result)
          }
        }
      }
    ),
    cancel = Button(
      text = Text("Cancel"),
      onClick = { screenContext.coroutineContext.cancelChildren() }
    )
  )
}

@OptIn(BetaOpenAI::class)
fun OpenAiEvent.toInfo(): Info.OpenAI = when (this) {
  is OpenAiEvent.Chat.Chunk -> TODO()
  is OpenAiEvent.Chat.Request -> TODO()
  is OpenAiEvent.Chat.Response -> TODO()
  is OpenAiEvent.Chat.WithFunctionRequest -> {
    val response = response
    Info.OpenAI(this)
//              it + Info.OpenAI(mapOf("function" to response.functions.toString()))
//                """
//                function : ${response.functions}
//                functionCall : ${response.functionCall}
//                messages : ${response.messages}
//                model : ${response.model}
//                maxTokens :  ${response.maxTokens}
//                temperature : ${response.temperature}
//                topP : ${response.topP}
//                n : ${response.n}
//                stop : ${response.stop}
//                presencePenalty : ${response.presencePenalty}
//                frequencyPenalty : ${response.frequencyPenalty}
//                """.trimIndent()
  }
  is OpenAiEvent.Chat.WithFunctionResponse -> {
    Info.OpenAI(this)
//              it + Info.OpenAI(mapOf( "WithFunctionResponse" to event.response.toString()))
  }
  is OpenAiEvent.Completion.Request -> TODO()
  is OpenAiEvent.Completion.Response -> TODO()
  is OpenAiEvent.Image.Request -> TODO()
  is OpenAiEvent.Image.Response -> TODO()
}

fun Messages.toInfo(): Info.Conversation =
  Info.Conversation(
    name.map {
      when (it.role) {
        Role.SYSTEM -> Info.Conversation.Message.System(it.content)
        Role.USER -> Info.Conversation.Message.User(it.content)
        Role.ASSISTANT -> Info.Conversation.Message.Assistant(it.content)
      }
    }
  )

fun ReactAgentEvents.toInfo(): ReactInfo =
  when (this) {
    is ReactAgentEvents.FinalAnswer -> TODO()
    is ReactAgentEvents.MaxIterationsReached -> TODO()
    is ReactAgentEvents.Observation -> ReactInfo.Observation(value)
    is ReactAgentEvents.SearchingTool -> ReactInfo.Search(tool + "[${input}]")
    is ReactAgentEvents.Thinking -> ReactInfo.Thinking(though)
    is ReactAgentEvents.ToolNotFound -> TODO()
  }