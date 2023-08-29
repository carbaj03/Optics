package counter

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import common.State


fun interface Reducer<S : State> {
    operator fun invoke(state: S, action: Action): S
}

inline fun <S : State, reified A : Action> ReducerType(
    crossinline f: S.(action: A) -> S
): Reducer<S> =
    Reducer<S> { state, action ->
        if (action is A) f(state, action)
        else state
    }

inline fun <S, reified C : Component, reified A : Action> ReducerComponent(
    crossinline f: C.(state: S, action: A) -> C
): Reducer<S> where S : State, S : WithComponent<S> =
    Reducer<S> { state, action ->
        if (action is A && state.component is C) state.replace(f(state.component as C, state, action))
        else state
    }

val loadReducer = ReducerType<App, Load> { action ->
    copy(component = Screen1())
}

val reducer = ReducerType<App, CounterAction> { action ->
    when (action) {
        Increment -> copy(counter = counter + 1)
        Decrement -> copy(counter = counter - 1)
        is Set -> copy(counter = action.counter)
    }
}

val reducer20 = ReducerComponent<App, Screen1, Screen1Action> { _, action ->
    when (action) {
        is LoadScreen1 -> copy(counter = action.counter)
        is ChangeCounter -> copy(counter = action.counter)
    }
}

val reducer21 = ReducerComponent<App, Screen1, CounterAction> { state, action ->
    when (action) {
        Increment -> copy(counter = counter + 1)
        Decrement -> copy(counter = counter - 1)
        is Set -> copy(counter = action.counter)
    }
}

val reducer3 = ReducerType<App, LoginAction> { action ->
    when (action) {
        is Login -> copy()
        is Logout -> copy()
    }
}


val reducers = listOf( reducer20, reducer21, reducer3, loadReducer)

val store: Store<App> = store(App(), reducers)

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        with(store) {
            val state by state.collectAsState()

            var s by mutableStateOf("")

            s = "saf"

            LaunchedEffect(Unit) {
                delay(2000)
                dispatch(Load)
            }

            when (val component = state.component) {
                is Screen1 -> Screen1(dispatch = store::dispatch, state = component, counter = state.counter)
                is Screen2 -> Screen2(dispatch = store::dispatch, state = component)
            }
        }
    }
}

@Composable
fun Screen1(
    dispatch: (Action) -> Unit,
    state: Screen1,
    counter : Int,
) {
    Scaffold(
        topBar = {
            TopAppBar { Text("Screen 1") }
        }
    ) {
        Column {
            Button(onClick = { dispatch(Increment) }) {
                Text("Increment")
            }
            Button(onClick = { dispatch(Decrement) }) {
                Text("Decrement")
            }
            TextField(
                value = state.counter.toString(),
                onValueChange = { dispatch(Set(it.toInt())) }
            )
            Button(onClick = { dispatch(Set(0)) }) {
                Text("Set")
            }
            Text("Counter: ${state.counter}")
            Text("Global Counter: ${counter}")
        }
    }
}

@Composable
fun Screen2(
    dispatch: (Action) -> Unit,
    state: Screen2,
) {
    Scaffold(
        topBar = {
            TopAppBar { Text("Screen 2") }
        }
    ) {
        Column {
            Button(onClick = { dispatch(Increment) }) {
                Text("Increment")
            }
            Button(onClick = { dispatch(Decrement) }) {
                Text("Decrement")
            }
            TextField(
                value = state.counter.toString(),
                onValueChange = { dispatch(Set(it.toInt())) }
            )
            Button(onClick = { dispatch(Set(0)) }) {
                Text("Set")
            }
            Text("Counter: ${state.counter}")
        }
    }
}

sealed interface Theme {
    data object Custom : Theme
    data object Dark : Theme
    data object Light : Theme
}


interface WithComponent<S : State> {
    val component: Component
    fun replace(component: Component): S
}

interface Component

data object Initial : Component

data class App(
    val theme: Theme = Theme.Custom,
    val user: String = "",
    val counter: Int = 0,
    override val component: Component = Initial,
) : State, WithComponent<App> {
    override fun replace(component: Component): App =
        copy(component = component)
}

interface Store<S : State> {
    val state: StateFlow<S>
    fun dispatch(action: Action)
}


fun <S : State> store(initialState: S, reducers: List<Reducer<S>>): Store<S> =
    object : Store<S> {
        override val state: MutableStateFlow<S> = MutableStateFlow(initialState)

        override fun dispatch(action: Action) {
            state.value = reducers.fold(state.value) { state, reducer -> reducer(state, action) }
        }
    }

inline fun <S : State> store(initialState: S, reducers: List<(Action, S) -> S>, f: Store<S>.() -> Unit) {
    object : Store<S> {
        override val state: MutableStateFlow<S> = MutableStateFlow(initialState)

        override fun dispatch(action: Action) {
            state.value = reducers.fold(state.value) { state, reducer -> reducer(action, state) }
        }

    }.let(f)
}

interface Action

data object Load : Action

sealed interface CounterAction : Action
data object Increment : CounterAction
data object Decrement : CounterAction
data class Set(val counter: Int) : CounterAction

sealed interface LoginAction : Action
data class Login(val username: String, val password: String) : LoginAction
data class Logout(val username: String) : LoginAction


data class Screen1(
    val counter: Int = 0,
) : Component


sealed interface Screen1Action : Action
data class LoadScreen1(val counter: Int) : Screen1Action
data class ChangeCounter(val counter: Int) : Screen1Action

data class Screen2(
    val counter: Int = 0,
) : Component




