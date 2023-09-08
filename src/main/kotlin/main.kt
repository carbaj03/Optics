import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import colors.invoke
import common.*
import home.invoke
import react.invoke
import screen.Home
import screen.ReAct
import screen.ToolSelection
import toolselection.invoke

fun main() = application {
  Window(
    state = rememberWindowState(size = DpSize(1200.dp, 1200.dp)),
    onCloseRequest = ::exitApplication,
  ) {
    val state by app.collectAsState()

    AppTheme(state.mode == Mode.Light) {

      when (val Screen = state.screen) {
        null -> Text("null")
        is ReAct -> Screen()
        is Home -> Screen(state.mode)
        is ToolSelection -> Screen()
        is screen.ColorsScreen -> Screen()
      }
    }
  }
}

@Composable
fun AppTheme(
  isLight: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colors = if (isLight) light else dark,
    typography = MaterialTheme.typography,
    shapes = MaterialTheme.shapes,
    content = content
  )
}

private val light: Colors
  @Composable get() = MaterialTheme.colors.copy(
    primary = md_theme_light_primary,
    primaryVariant = md_theme_light_primary,
    secondary = md_theme_light_secondary,
    secondaryVariant = md_theme_light_secondary,
    surface = md_theme_light_surface,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    onSurface = md_theme_light_onSurface,
    onPrimary = md_theme_light_onPrimary,
    onSecondary = md_theme_light_onSecondary,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    isLight = true,
  )

private val dark: Colors
  @Composable get() = MaterialTheme.colors.copy(
    primary = md_theme_dark_primary,
    primaryVariant = md_theme_dark_primary,
    secondary = md_theme_dark_secondary,
    secondaryVariant = md_theme_dark_secondary,
    surface = md_theme_dark_surface,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    onSurface = md_theme_dark_onSurface,
    onPrimary = md_theme_dark_onPrimary,
    onSecondary = md_theme_dark_onSecondary,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    isLight = false,
  )
