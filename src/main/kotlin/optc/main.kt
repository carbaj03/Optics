package optc

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import arrow.optics.*
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import common.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import with
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty
import androidx.compose.material.Text as TextCompose

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

    App(
      initialState = App(),
      scope = scope
    ) {
      screen.screen2.scope {
        toolbar set Toolbar(title = Text("test1"))
        button set Button("test2")
        text set listOf(Text(value = "asdfs"))
      }

      load()

      AppComponent()
    }
  }
}

context(CoroutineScope, Store<App>, App.Companion)
fun load() {
  screen set Screen1()
}

context(Store<App>, App.Companion)
@Composable
fun AppComponent() {
  val app: App by state.collectAsState()

  when (val screen = app.screen) {
    is Screen1 -> screen()
    is Screen2 -> {
      Screen2(screen)
    }

    is Splash -> TODO()
    null -> {
      Text("Loading...")
    }
  }
}

context(CoroutineScope, Store<App>, App.Companion)
fun Screen1(): Screen1 {

  return Screen1(
    toolbar = Toolbar(title = Text("Screen 1")),
    login = Button("Login") {
      user set Logged("John")
      screen set Screen2()
    },
    cancel = Button("Cancel") {
      coroutineContext.cancelChildren()
    }
  )
}

context(Store<App>, App.Companion)
@Composable
operator fun Screen1.invoke() {
  Scaffold(
    topBar = {
      TopAppBar(title = { TextCompose(text = toolbar.title.value) })
    },
  ) {
    Column {
      Button(onClick = login.onClick) {
        TextCompose(text = login.text)
      }

      Button(onClick = cancel.onClick) {
        TextCompose(text = cancel.text)
      }
    }
  }
}

context(CoroutineScope, Store<App>, App.Companion)
fun Screen2(): Screen2 = with(screen.screen2) {

  return Screen2(
    toolbar = Toolbar(
      title = Text("Hello, ${user.logged.name}}}"),
      itemLeft = {
        screen set Screen1()
        coroutineContext.cancelChildren()
      }
    ),
    button = Button(text = "Click me") {
      launch {
        text set text.get().plus(Text(value = input.value.get()))
        input.value set ""
      }
    },
    input = Input(value = "", onChange = { input.value set it }),
    text = emptyList(),
    counter = Text(value = "0")
  )
}

context(Store<App>, App.Companion)
@Composable
fun Screen2(screen: Screen2) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { screen.toolbar.title.composable() },
        navigationIcon = {
          IconButton(onClick = screen.toolbar.itemLeft) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
          }
        }
      )
    },
  ) {
    Column {
      screen.text.forEach {
        it.composable()
      }
      screen.counter.composable()
      TextField(value = screen.input.value, onValueChange = screen.input.onChange)
      Button(onClick = screen.button.onClick) {
        TextCompose(text = screen.button.text)
      }
    }
  }
}

@Composable
fun Text.composable() {
  TextCompose(text = value)
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
  val user: User = Anonymous,
  val theme: Theme = Dark,
) {
  companion object
}

@optics sealed interface User {
  companion object
}

@optics data class Logged(val name: String) : User {
  companion object
}

data object Anonymous : User

@optics sealed interface Theme {
  companion object
}

data object Dark : Theme
data object Light : Theme

data object Splash : Screen

@optics
data class Screen1(
  val toolbar: Toolbar,
  val login: Button,
  val cancel: Button,
) : Screen {
  companion object
}

@optics
data class Screen2(
  val toolbar: Toolbar,
  val button: Button,
  val input: Input,
  val text: List<Text>,
  val counter: Text,
) : Screen {
  companion object
}

@optics
data class Toolbar(
  val itemLeft: () -> Unit = {},
  val title: Text,
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
    screen set Screen1(
      toolbar = Toolbar(title = Text("Hello, World!")),
      login = Button("Click me"),
      cancel = Button("Cancel")
    )

    delay(1000)
    screen.screen1.toolbar set Toolbar(title = Text("2"))

    screen set Screen2(
      toolbar = Toolbar(title = Text("Hello, World!")),
      text = emptyList(),
      input = Input(value = "") { },
      button = Button("Click me") {
        screen set Screen1(
          toolbar = Toolbar(title = Text("Hello, World!")),
          login = Button("Click me"),
          cancel = Button("Cancel")
        )
      },
      counter = Text(value = "0")
    )

    screen.screen1.toolbar set Toolbar(title = Text("213213"))

    screen.screen2.scope {
      val response = prompt("test")

      toolbar set Toolbar(title = Text("test1"))
      button set Button("test2")
      text set listOf(Text(value = response))
    }

    screen.screen1.login set Button("test3")
  }
}

context(CoroutineScope)
private suspend fun AppWithCompanion() =
  App(initialState = App()) {
    launch {
      delay(1000)
      screen set Screen1(toolbar = Toolbar(title = Text("Hello, World!")), login = Button("Click me"), cancel = Button("Cancel"))
      delay(1000)
      screen.screen1.toolbar set Toolbar(title = Text("2"))
      delay(1000)
      screen.screen1.scope {
        toolbar set Toolbar(title = Text("test"))
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

    address.city.scope {
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

interface Action

fun interface Reducer<A> {
  operator fun invoke(state: A, action: Action): A
}

@OptIn(ExperimentalTypeInference::class)
operator fun <A> City.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference f: context(Copy<A>, City.Companion) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also { f(it, this) }.state

@OptIn(ExperimentalTypeInference::class)
operator suspend fun <A> Person.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference f: context(Copy<A>, Person.Companion) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also { f(it, this) }.state

@OptIn(ExperimentalTypeInference::class)
inline operator fun <A> App.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference f: context(Store<A>, App.Companion, CoroutineScope) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also { f(it, this, scope) }.state

@OptIn(ExperimentalTypeInference::class)
suspend operator fun <A> Screen.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference f: suspend context(Copy<A>, Screen.Companion) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also { f(it, this) }.state

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

