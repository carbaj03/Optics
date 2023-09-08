package common

import com.xebia.functional.xef.tracing.Event
import kotlinx.coroutines.flow.StateFlow

interface Logger {
  val event: StateFlow<Event>
  suspend fun log(message: Event)
}

object Empty : Event