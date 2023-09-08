import arrow.core.continuations.Raise
import arrow.core.continuations.effect
import arrow.core.continuations.fold
import arrow.optics.optics
import common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import screen.Screen
import screen.createHome
import kotlin.experimental.ExperimentalTypeInference

@optics data class App(
  val screen: Screen? = null,
  val user: String = "",
  val mode : Mode = Mode.Light,
  val track: List<Info> = emptyList(),
) : State {
  companion object
}

enum class Mode {
  Dark,
  Light,
  Dynamic,
}

val app: StateFlow<App> =
  App(
    initialState = App(),
  ) {
    screen set createHome()
  }

@OptIn(ExperimentalTypeInference::class)
inline operator fun <A : State> App.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference crossinline f: context(Store<A>, App.Companion, CoroutineScope, Raise<Error>) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also {
    scope.launch { effect { f(it, this@invoke, scope, this) }.fold({ println(it) }, { println(it) }) }
  }.state
