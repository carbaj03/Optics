import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import arrow.optics.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import optics.*
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty

context(MutableStateFlow<App>)
@Composable
fun App() {
    MaterialTheme {
        val navigation by collectAsState()

        when (val s = navigation.screen) {
            is Screen1 -> with(MutableStateFlow(Screen1(Toolbar(""), Button(""),  Button("")))) { s.render() }
            is Screen2 -> {

            }

            is Splash -> TODO()
            null -> {}
        }
    }
}

context(MutableStateFlow<Screen1>)
@Composable
fun Screen1.render() {
    val screen: Screen1 by collectAsState()

    Column {
        Text(screen.toolbar.title)
        Button(onClick = { screen.toolbar.Title = "33" }) {
            Text("Click me")
        }
    }
}


//        val example = Example(Example1(Example2(Example3("Hello, World!"))))
//
//        val copy = example.copy {
//            Example.example1 set Example1(Example2(Example3("Hello, Desktop!")))
//            Example.example1.example2.example3.example4 set "Hello, Desktop!"
//        }
//
//        val set: Example = Example.example1.set(example, example.example1.copy())


context(MutableStateFlow<Example>)
var example: Example
    get() = value
    set(new) {
        value = new
    }

context(MutableStateFlow<Example>)
var Example.Example1: Example1
    get() = example1
    set(new) {
        value = value.copy(example1 = new)
    }

context(MutableStateFlow<Example>)
var Example.Counter: Int
    get() = counter
    set(new) {
        value = value.copy(counter = new)
    }

context(MutableStateFlow<Example>)
var Example1.Example2: Example2
    get() = example2
    set(new) {
        value = value.copy(example1 = copy(example2 = new))
    }

context(MutableStateFlow<Example>)
var Example1.Counter: Int
    get() = counter
    set(new) {
        value = value.copy(example1 = copy(counter = new))
    }

context(MutableStateFlow<Example>)
var Example2.Example3: Example3
    get() = example3
    set(new) {
        value = value.copy(example1 = value.example1.copy(example2 = copy(example3 = new)))
    }

context(MutableStateFlow<Example>)
var Example2.Counter: Int
    get() = counter
    set(new) {
        value = value.copy(example1 = value.example1.copy(example2 = copy(counter = new)))
    }

context(MutableStateFlow<Example>)
var Example3.Counter: Int
    get() = counter
    set(new) {
        value = value.copy(example1 = value.example1.copy(example2 = value.example1.example2.copy(example3 = copy(counter = new))))
    }

operator fun Example.invoke(f: Example.() -> Unit) {
    f()
}

@optics
data class Example(
    val example1: Example1,
    val counter: Int,
) {
    companion object
}

@optics
data class Example1(
    val example2: Example2,
    val counter: Int,
) {
    companion object
}

@optics
data class Example2(
    val example3: Example3,
    val counter: Int,
) {
    companion object
}

@optics
data class Example3(
    val counter: Int,
) {
    companion object
}



@optics
data class State(
    val screen: Screen?
) {
    companion object
}


//context(Copy<A>)
//@OptIn(ExperimentalTypeInference::class)
//operator fun <A, B> Traversal<A, B>.invoke(@BuilderInference f: context(Copy<B>, City.Companion) () -> Unit): Unit =
//    transform { City.Companion.invoke(it, f) }


@OptIn(ExperimentalTypeInference::class)
public fun <A> A.state(@BuilderInference f: Component<A>.() -> Unit): Unit {
    Component(this).also(f).get
}

@JvmInline
value class Component<A>(val get: A)


context(MutableStateFlow<Person>)
public infix fun <A> Component<A>.set(b: A) {

}


@Composable
fun DecoratedTextField(
    value: String,
    length: Int,
    modifier: Modifier = Modifier,
    boxWidth: Dp = 38.dp,
    boxHeight: Dp = 38.dp,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    keyboardActions: KeyboardActions = KeyboardActions(),
    onValueChange: (String) -> Unit,
) {

    var a by remember { mutableStateOf(0) }
    val spaceBetweenBoxes = 8.dp
    BasicTextField(modifier = modifier,
        value = value,
        singleLine = true,
        onValueChange = {
            if (it.length <= length) {
                onValueChange(it)
            }
        },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = {
            Row(
                Modifier.size(width = (boxWidth + spaceBetweenBoxes) * length, height = boxHeight),
                horizontalArrangement = Arrangement.spacedBy(spaceBetweenBoxes),
            ) {
                repeat(length) { index ->
                    Box(
                        modifier = Modifier
                            .size(boxWidth, boxHeight)
                            .border(
                                1.dp,
                                color = MaterialTheme.colors.primary,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.getOrNull(index)?.toString() ?: "",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.h6
                        )
                    }
                }
            }
        })
}

suspend fun main23(): Unit {
    val appScope = CoroutineScope(SupervisorJob())
    val app: MutableStateFlow<App> = MutableStateFlow(App(Splash))

    appScope.launch {
        app.collect {
            when (app.value.screen) {
                is Screen1 -> {
                    val scope = CoroutineScope(SupervisorJob() + appScope.coroutineContext)
                    val screen1 = MutableStateFlow(app.value.screen as Screen1)

                    scope.launch {
                        delay(100)
                        app.value = app.value.copy(screen = Screen1(Toolbar("Hello, World!2"), Button("Hello, World!2"), Button("Hello, World!2")))
                    }

                    with(screen1) {
                        screen1 {
                            scope.launch {
                                toolbar.Title = "Hello, Desktop!"
                                println(value)
                                makePrompt.Text = "Hello, Desktop!"
                                println(value)

                                delay(1000)
                                app.value = app.value.copy(screen = Screen2(Toolbar("Hello, World!3"), text = Text(value = ""), button = Button(text = "Hello, World!3")))
//                                scope.cancel()
                                delay(1000)
                                makePrompt.Text = "Hello, Desktop!"
                                println(value)
                            }
                        }
                    }
                }

                is Screen2 -> {
                    val scope = CoroutineScope(SupervisorJob() + appScope.coroutineContext)
                    val screen2 = MutableStateFlow(Screen2(Toolbar(title = "Hello, World!"), text = Text(value = ""), button = Button("Hello, World!")))
                    with(screen2) {
                        screen2 {
                            toolbar.Title = "Hello, Desktop!"
                            println(value)
                            button.Text = "Hello, Desktop!"
                            println(value)
                            scope.launch {
                                delay(1000)
                                button.Text = "Hello, Desktop!"
                                println(value)
                            }
                        }
                    }
                }

                is Splash -> {
                    val state = MutableStateFlow(Example(Example1(Example2(Example3(0), 0), 0), 0))
                    with(state) {
                        example {
                            Counter++
                            println(value)
                            example1.Counter++
                            println(value)
                            example1.example2.Counter++
                            println(value)
                            Counter = 4
                            println(value)
                            example1.example2.example3.Counter++
                            println(value)
                            Example1 = Example1(Example2(Example3(0), 0), 0)
                            println(value)
                        }
                    }
                    delay(1000)
                    app.value = app.value.copy(screen = Screen1(Toolbar("Hello, World!"), Button("Hello, World!"), Button("Hello, World!")))
                }

                null -> {}
            }
        }
    }

    delay(4000)
    appScope.launch { println("safddfa") }
    awaitCancellation()
//    application {
//        Window(onCloseRequest = ::exitApplication) {
//            with(app) { App() }
//        }
//    }
}



context(MutableStateFlow<Screen1>)
var screen1: Screen1
    get() = value
    set(new) {
        value = new
    }

context(MutableStateFlow<Screen1>)
var Screen1.Toolbar: Toolbar
    get() = toolbar
    set(new) {
        value = value.copy(toolbar = new)
    }

context(MutableStateFlow<Screen1>)
var Toolbar.Title: String
    @JvmName("sadfdsa") get() = title
    @JvmName("sadfdsa2") set(new) {
        value = value.copy(toolbar = copy(title = new))
    }

context(MutableStateFlow<Screen1>)
var Screen1.Button: Button
    get() = makePrompt
    set(new) {
        value = value.copy(makePrompt = new)
    }

context(MutableStateFlow<Screen1>)
var Button.Text: String
    @JvmName("sad2fdsa") get() = text
    @JvmName("sad3fdsa") set(new) {
        value = value.copy(makePrompt = copy(text = new))
    }

context(MutableStateFlow<Screen2>)
var screen2: Screen2
    get() = value
    set(new) {
        value = new
    }

context(MutableStateFlow<Screen2>)
var Screen2.Toolbar: Toolbar
    get() = toolbar
    set(new) {
        value = value.copy(toolbar = new)
    }

context(MutableStateFlow<Screen2>)
var Toolbar.Title: String
    get() = title
    set(new) {
        value = value.copy(toolbar = copy(title = new))
    }

context(MutableStateFlow<Screen2>)
var Screen2.Button: Button
    get() = button
    set(new) {
        value = value.copy(button = new)
    }

context(MutableStateFlow<Screen2>)
var Button.Text: String
    get() = text
    set(new) {
        value = value.copy(button = copy(text = new))
    }
