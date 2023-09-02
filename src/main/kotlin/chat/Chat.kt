package chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import arrow.core.continuations.Raise
import arrow.core.continuations.effect
import arrow.core.continuations.fold
import arrow.optics.optics
import chef.invoke
import common.*
import common.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference

fun main() = application {
  Window(onCloseRequest = ::exitApplication) {
    Column {

      val state by app.collectAsState()

      when (val screen = state.screen) {
        null -> androidx.compose.material.Text("Init")
        is HomeScreen -> {
          screen.loginSocial()
          screen.loginEmail()
          screen.register()
          screen.forgotPassword()
        }
        is Splash -> {
          LaunchedEffect(Unit) {
            screen.next()
          }
          androidx.compose.material.Text("Splash")
        }
        is WelcomeScreen -> {
          screen.title()
          screen.subtittle()
          screen.warningInnacuraccy()
          screen.warningSensitiveInfo()
          screen.history()
          screen.next()
        }
        is ChatScreen -> screen()
      }
    }
  }
}

@OptIn(ExperimentalTypeInference::class)
inline operator fun <A : State> ChatApp.Companion.invoke(
  initialState: A,
  scope: CoroutineScope = CoroutineScope(SupervisorJob()),
  @BuilderInference crossinline f: context(Store<A>, ChatApp.Companion, CoroutineScope, Raise<Error>) () -> Unit
): StateFlow<A> =
  StoreImpl(initialState, scope).also {
    scope.launch { effect { f(it, this@invoke, scope, this) }.fold({ println(it) }, { println(it) }) }
  }.state

@optics sealed interface Screen {
  companion object
}

@optics data class Splash(val next: () -> Unit) : Screen {
  companion object
}

@optics data class HomeScreen(
  val loginSocial: Button,
  val loginEmail: Button,
  val register: Button,
  val forgotPassword: Button,
) : Screen {
  companion object
}

@optics data class WelcomeScreen(
  val title: Text = Text("Welcome", TextStyle.H1),
  val subtittle: Text = Text("The official is free, syncs your history across devices, and brings you the lastest model improvements from OpenAI"),
  val warningInnacuraccy: InfoItem = InfoItem(
    title = Text("Warning:"),
    description = Text("This model is not perfect. Please keep this in mind when using it."),
    icon = Icon.DocumentScearch(Color.Red)
  ),
  val warningSensitiveInfo: InfoItem = InfoItem(
    title = Text("Warning:"),
    description = Text("This model may generate offensive content. Please keep this in mind when using it."),
    icon = Icon.DocumentScearch(Color.Red)
  ),
  val history: InfoItem = InfoItem(
    title = Text("History:"),
    description = Text("This model was trained on the following data:"),
    icon = Icon.DocumentScearch(Color.Blue)
  ),
  val next: Button
) : Screen {
  companion object
}

@optics data class InfoItem(
  val title: Text,
  val description: Text,
  val icon: Icon,
) {
  companion object
}

@Composable
operator fun InfoItem.invoke() = Row {
  icon()
  Column {
    title()
    description()
  }
}

sealed interface Icon {
  val color: Color

  data class DocumentScearch(override val color: Color) : Icon
  data class Send(override val color: Color) : Icon
  data class Menu(override val color: Color) : Icon

  companion object
}

@Composable
operator fun Icon.invoke(modifier: Modifier = Modifier) = androidx.compose.material.Icon(
  modifier = modifier,
  tint = color(),
  imageVector = when (this) {
    is Icon.DocumentScearch -> Icons.Default.Search
    is Icon.Send -> Icons.Default.Send
    is Icon.Menu -> Icons.Default.Menu
  },
  contentDescription = null
)

@optics sealed interface Color {

  data object White : Color
  data object Red : Color
  data object Green : Color
  data object Blue : Color
  data object Pastel : Color
  data object Teal : Color

  companion object
}

@Composable
operator fun Color.invoke(): androidx.compose.ui.graphics.Color = when (this) {
  Color.Blue -> androidx.compose.ui.graphics.Color.Blue
  Color.Green -> androidx.compose.ui.graphics.Color.Green
  Color.Red -> androidx.compose.ui.graphics.Color.Red
  Color.White -> androidx.compose.ui.graphics.Color.White
  Color.Pastel -> Pastel
  Color.Teal -> Teal
}

@optics data class Button(
  val text: Text,
  val onClick: () -> Unit,
) {
  companion object
}

@optics data class ButtonIcon(
  val icon: Icon,
  val onClick: () -> Unit,
) {
  companion object
}

@optics data class Text(
  val text: String,
  val style: TextStyle = TextStyle.Body,
) {
  companion object
}

@optics sealed interface TextStyle {
  data object H1 : TextStyle
  data object Body : TextStyle

  companion object
}

@optics data class Toolbar(
  val title: Text,
  val subtitle: Text?,
  val itemLeft: ItemLeft? = null,
  val itemRight: List<Icon>? = null,
) {
  companion object
}

@optics data class ItemLeft(
  val icon: Icon,
  val onClick: () -> Unit,
) {
  companion object
}

@optics data class SideBar(
  val header: SideBarHeader,
  val items: List<Icon>,
  val open: Boolean = false,
  val onChange: (Boolean) -> Unit,
) {
  companion object
}

@optics data class SideBarHeader(
  val title: Text,
  val subtitle: Text,
  val icon: Icon,
  val plan: Text,
) {
  companion object
}

@optics data class Input(
  val text: String,
  val onTextChange: (String) -> Unit,
  val action: ButtonIcon?,
) {
  companion object
}

operator fun <A> A?.invoke(f: @Composable A.() -> Unit): (@Composable () -> Unit)? = this?.let { { f(it) } }

@optics sealed interface Message {
  val text: String

  companion object
}

@optics data class UserMessage(override val text: String) : Message {
  companion object
}

@optics data class AssitantMessage(override val text: String) : Message {
  companion object
}

@optics data class ChatApp(
  val screen: Screen? = null,
  val user: User = Anonymous,
  val theme: Theme = Dark,
) : State {
  companion object
}

@optics
sealed interface User {
  companion object
}

@optics
data class Logged(val name: String) : User {
  companion object
}

data object Anonymous : User

@optics
sealed interface Theme {
  companion object
}

data object Dark : Theme
data object Light : Theme
