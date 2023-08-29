package common

import arrow.optics.Setter
import arrow.optics.Traversal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StoreImpl<A>(
  initialState: A,
  scope: CoroutineScope
) : Store<A> {
  private val _state = MutableStateFlow(initialState)

  override fun <B> Setter<A, B>.set(b: B) {
    _state.value = set(state.value, b)
  }

  override fun <B> Traversal<A, B>.transform(f: (B) -> B) {
    _state.value = modify(state.value, f)
  }

  override val state: StateFlow<A> = _state

  override val parent: CoroutineScope = scope
}
