package common

import com.xebia.functional.xef.tracing.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoggerImpl : Logger {
  private val _event : MutableStateFlow<Event> = MutableStateFlow(Empty)

  override val event: StateFlow<Event> = _event

  override suspend fun log(message: Event) {
    _event.value = message
  }
}