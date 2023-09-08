package common

import arrow.optics.optics
import com.xebia.functional.xef.conversation.llm.openai.OpenAiEvent
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.tracing.ImagesRequest
import com.xebia.functional.xef.tracing.ImagesResponse
import com.xebia.functional.xef.tracing.Messages
import components.Budget

interface Info {

  interface Agent : Info

  @optics sealed interface Xef : Info {

    @optics sealed interface Images : Xef {

      @optics data class Request(
        val prompt: List<Conversation.Message>,
        val numberImages: Int,
        val size: String,
      ) : Images {
        companion object
      }

      @optics data class Response(
        val request: ImagesResponse,
      ) : Images {
        companion object
      }

      companion object
    }

    @optics data class Conversation(
      val context: List<Message>,
      val history: List<Message>,
      val message: List<Message>,
      val totalTokens: Int,
    ) : Xef {

      @optics sealed interface Message {
        val content: String
        val budget: Budget

        @optics data class User(
          override val content: String,
          override val budget: Budget
        ) : Message {
          companion object
        }

        @optics data class Assistant(
          override val content: String,
          override val budget: Budget
        ) : Message {
          companion object
        }

        @optics data class System(
          override val content: String,
          override val budget: Budget
        ) : Message {
          companion object
        }

        companion object
      }

      companion object
    }

    companion object
  }

  @optics data class OpenAI(
    val event: OpenAiEvent,
  ) : Info {
    companion object
  }
}

fun ImagesRequest.toInfo(): Info.Xef.Images =
  Info.Xef.Images.Request(
    prompt = prompt.map { it.toInfo() },
    numberImages = numberImages,
    size = size,
  )

fun ImagesResponse.toInfo(): Info.Xef.Images =
  Info.Xef.Images.Response(
    request = this,
  )

fun OpenAiEvent.toInfo(): Info.OpenAI =
  when (this) {
    is OpenAiEvent.Chat.Chunk -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Chat.Request -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Chat.Response -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Chat.WithFunctionRequest -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Chat.WithFunctionResponse -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Completion.Request -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Completion.Response -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Image.Request -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Image.Response -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Embedding.Request -> {
      Info.OpenAI(this)
    }
    is OpenAiEvent.Embedding.Response -> {
      Info.OpenAI(this)
    }
  }

fun Messages.toInfo(): Info.Xef.Conversation =
  Info.Xef.Conversation(
    message = prompt.map { it.toInfo() },
    context = contextAllowed.map { it.toInfo() },
    history = historyAllowed.map { it.toInfo() },
    totalTokens = countTokens,
  )

fun Message.toInfo(): Info.Xef.Conversation.Message =
  when (role) {
    Role.SYSTEM -> Info.Xef.Conversation.Message.System(content, Budget.System)
    Role.USER -> Info.Xef.Conversation.Message.User(content, Budget.User)
    Role.ASSISTANT -> Info.Xef.Conversation.Message.Assistant(content, Budget.Assistant)
  }