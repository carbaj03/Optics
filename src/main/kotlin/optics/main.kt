package optics

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import arrow.optics.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import with
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty

//suspend fun main() {
//    modifyFlow()
//    modify()
//    scope.launch {
//        val a = AppWithCompanion()
//        launch { a.collect(::println) }
//    }
//    scope.launch { app() }

//    awaitCancellation()
//}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        val scope = rememberCoroutineScope()
        App(App(), scope = scope) {
            load()

//            launch {
//                state.collect {
//                    println(it)
//                    when (val screen = it.screen) {
//                        is Screen1 -> {
//                            screen.button.onClick()
//                        }
//
//                        is Screen2 -> {
//                            screen.button.onClick()
//                            println(screen.text.value)
//                        }
//
//                        is Splash -> println(it)
//                        null -> println("null")
//                    }
//                }
//            }
            AppComponent()
        }
    }
}


context(Store<App>, App.Companion)
@Composable
fun AppComponent() {
    val app: App by state.collectAsState()

    when (val screen = app.screen) {
        is Screen1 -> Screen1(screen)
        is Screen2 -> Screen2(screen)
        is Splash -> TODO()
        null -> {
            Text("Loading...")
        }
    }
}

context(Store<App>, App.Companion)
@Composable
fun Screen2(screen: Screen2) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = screen.toolbar.title) })
        },
    ) {
        Column {
            Text(text = screen.text.value)
            Button(onClick = screen.button.onClick) {
                Text(text = screen.button.text)
            }
        }
    }
}

context(Store<App>, App.Companion)
@Composable
fun Screen1(screen: Screen1) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = screen.toolbar.title) })
        },
    ) {
        Column {
            Button(onClick = screen.makePrompt.onClick) {
                Text(text = screen.makePrompt.text)
            }

            Button(onClick = screen.cancel.onClick) {
                Text(text = screen.cancel.text)
            }
        }
    }
}

context(CoroutineScope, Store<App>, App.Companion)
fun load() {
    screen set Screen1()
}


context(CoroutineScope, Store<App>, App.Companion)
fun Screen1(): Screen1 {
    val scope = CoroutineScope(SupervisorJob(coroutineContext.job))

    return Screen1(
        toolbar = Toolbar("Hello, World!"),
        makePrompt = Button("Click me") {
            scope.launch {
                delay(2000)
                screen set Screen2()
            }
        },
        cancel = Button("Cancel") {
            scope.coroutineContext.cancelChildren()
        }
    )
}

context(CoroutineScope, Store<App>, App.Companion)
fun Screen2(): Screen2 {
    val scope = CoroutineScope(SupervisorJob(coroutineContext.job))

    return Screen2(
        toolbar = Toolbar(title = "Hello, World!"),
        button = Button(text = "Click me") {
            scope.launch { screen.screen2.text.value set prompt("Hello, World!") }
        },
        input = Input(value = "", onChange = { scope.launch { screen.screen2.input.value set it } }),
        text = Text(value = screen.screen2.input.value.get())
    )
}

@optics
sealed interface Screen {
    companion object
}

operator fun <A : Screen> A.invoke(f: A.() -> Unit) {
    f()
}

@optics
data class App(
    val screen: Screen? = null,
) {
    companion object
}

data object Splash : Screen

@optics
data class Screen1(
    val toolbar: Toolbar,
    val makePrompt: Button,
    val cancel: Button,
) : Screen {
    companion object
}

@optics
data class Screen2(
    val toolbar: Toolbar,
    val button: Button,
    val input: Input,
    val text: Text
) : Screen {
    companion object
}


@optics
data class Toolbar(
    val title: String
) {
    companion object
}

@optics
data class Button(
    val text: String,
    val onClick: () -> Unit = {}
) {
    companion object
}

@optics
data class Text(
    val value: String,
) {
    companion object
}

@optics
data class Input(
    val value: String,
    val onChange: (String) -> Unit,
) {
    companion object
}


context(CoroutineScope)
private suspend fun app() {

    val state = MutableStateFlow(App())

    launch { state.collect { println("Collect: " + it) } }

    with(state, App) {
        screen set Screen1(toolbar = Toolbar("Hello, World!"), makePrompt = Button("Click me"), cancel = Button("Cancel"))
        delay(1000)
        screen.screen1.toolbar set Toolbar("2")

        screen set Screen2(
            toolbar = Toolbar("Hello, World!"),
            text = Text(value = ""),
            input = Input(value = "") { },
            button = Button("Click me") {
                screen set Screen1(
                    toolbar = Toolbar("Hello, World!"),
                    makePrompt = Button("Click me"),
                    cancel = Button("Cancel")
                )
            }
        )

        screen.screen1.toolbar set Toolbar("213213")

        screen.screen2 {
            val response = prompt("test")

            toolbar set Toolbar("test1")
            button set Button("test2")
            text set Text(value = response)
        }

        screen.screen1.makePrompt set Button("test3")
    }
}


context(CoroutineScope)
private suspend fun AppWithCompanion() =
    App(App()) {
        launch {
            delay(1000)
            screen set Screen1(toolbar = Toolbar("Hello, World!"), makePrompt = Button("Click me"), cancel = Button("Cancel"))
            delay(1000)
            screen.screen1.toolbar set Toolbar("2")
            delay(1000)
            screen.screen1 {
                toolbar set Toolbar("test")
            }
        }
    }


private fun modifyFlow() {
    val person = Person("John", 30, Address(Street("Main Street", 42), City("Amsterdam", "Netherlands")))

    val state = MutableStateFlow(person)
    state.value = Person.address.city.set(state.value, City("Amsterdam", "Netherlands"))
    state.value = Person.address.city.name.set(state.value, "Amsterdam")

    state.run {
        with(Person) {
            address.city set City("Amsterdam", "Netherlands")
            address.city.name set "Amsterdam"
            address.city.country set "Amsterdam"
        }
    }
}

private suspend fun modify() {
    val person = Person("John", 30, Address(Street("Main Street", 42), City("Amsterdam", "Netherlands")))

    Person(person) {
        address.city set City(name = "Amsterdam", country = "Netherlands")
        address.city.name set "Bruseles"

        address.city {
            name set "asdfsd"
        }

        name set "safsfd"
    }
}


suspend fun prompt(message: String): String {
    delay(1000)
    println(message)
    return readLine()!!
}

@OptIn(ExperimentalTypeInference::class)
public fun <A, B> Copy<A>.inside(field: Traversal<A, B>, @BuilderInference f: Copy<B>.() -> Unit): Unit =
    field.transform { it.copy(f) }


interface Store<A> : Copy<A> {
    val state: StateFlow<A>
//    fun dispatch(action: Action)
}

interface Action


fun interface Reducer<A> {
    operator fun invoke(state: A, action: Action): A
}


class StoreImpl<A>(
    initialState: A,
//    val reducers: MutableList<Reducer<A>> = mutableListOf()
) : Store<A> {
    val _state = MutableStateFlow(initialState)

    override fun <B> Setter<A, B>.set(b: B) {
        _state.value = set(state.value, b)
    }

    override fun <B> Traversal<A, B>.transform(f: (B) -> B) {
        _state.value = modify(state.value, f)
    }

    override val state: StateFlow<A> = _state

//    override fun dispatch(action: Action) {
//        reducers.fold(_state.value) { acc, reducer ->
//            reducer(acc, action)
//        }.let { _state.value = it }
//    }
}


@OptIn(ExperimentalTypeInference::class)
operator fun <A> City.Companion.invoke(
    initialState: A,
    @BuilderInference f: context(Copy<A>, City.Companion) () -> Unit
): StateFlow<A> =
    StoreImpl(initialState).also { f(it, this) }.state

@OptIn(ExperimentalTypeInference::class)
operator suspend fun <A> Person.Companion.invoke(
    initialState: A,
    @BuilderInference f: context(Copy<A>, Person.Companion) () -> Unit
): StateFlow<A> =
    StoreImpl(initialState).also { f(it, this) }.state

@OptIn(ExperimentalTypeInference::class)
inline operator fun <A> App.Companion.invoke(
    initialState: A,
    scope: CoroutineScope = CoroutineScope(SupervisorJob()),
    @BuilderInference f: context(Store<A>, App.Companion, CoroutineScope) () -> Unit
): StateFlow<A> =
    StoreImpl(initialState).also { f(it, this, scope) }.state

@OptIn(ExperimentalTypeInference::class)
suspend operator fun <A> Screen.Companion.invoke(
    initialState: A,
    @BuilderInference f: suspend context(Copy<A>, Screen.Companion) () -> Unit
): StateFlow<A> =
    StoreImpl(initialState).also { f(it, this) }.state


@optics
data class Person(val name: String, val age: Int, val address: Address) {
    companion object
}

@optics
data class Address(val street: Street, val city: City) {
    companion object
}

@optics
data class Street(val name: String, val number: Int?) {
    companion object
}

@optics
data class City(val name: String, val country: String) {
    companion object
}


context(MutableStateFlow<A>)
@Suppress("NOTHING_TO_INLINE")
inline operator fun <A, B> Optional<A, B>.getValue(thisObj: Any?, property: KProperty<*>): B = getOrNull(value)!!


context(Store<A>)
operator fun <A> Optional<A, () -> Unit>.invoke(): Unit =
    getOrNull(state.value)!!.invoke()

context(Store<A>)
fun <A, B> Optional<A, B>.get(): B =
    getOrNull(state.value)!!

inline operator fun <A, B> Optional<A, B>.invoke(f: Optional<A, B>.() -> Unit) {
    f()
}


context(MutableStateFlow<A>)
infix fun <A, B> Lens<A, B>.set(name: B) {
    value = set(value, name)
}

context(MutableStateFlow<A>)
infix fun <A, B> Prism<A, B>.set(name: B) {
    value = set(value, name)
}

context(MutableStateFlow<A>)
infix fun <A, B> Optional<A, B>.set(name: B) {
    value = set(value, name)
}