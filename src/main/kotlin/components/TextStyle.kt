package components

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import arrow.optics.optics

@optics sealed interface TextStyle {
  data object H1 : TextStyle
  data object H4 : TextStyle
  data object Body1 : TextStyle
  data object Budget : TextStyle
  data object Agent : TextStyle

  companion object
}


@Composable
operator fun TextStyle.invoke(): androidx.compose.ui.text.TextStyle =
  when (this) {
    TextStyle.Body1 -> MaterialTheme.typography.body1
    TextStyle.H1 -> MaterialTheme.typography.h1
    TextStyle.H4 -> MaterialTheme.typography.h4
    TextStyle.Budget -> MaterialTheme.typography.caption
    TextStyle.Agent -> MaterialTheme.typography.body2
  }