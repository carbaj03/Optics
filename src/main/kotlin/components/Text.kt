package components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arrow.optics.optics

@optics data class Text(
  val text: String,
  val style: TextStyle = TextStyle.Body1,
) {
  companion object
}

fun TextBudget( text: String): Text =
  Text(text, TextStyle.Budget)

fun TextAgent(text: String): Text =
  Text(text, TextStyle.Agent)

@Composable
operator fun Text.invoke(modifier: Modifier = Modifier) {
  androidx.compose.material.Text(
    modifier = modifier,
    text = text,
    style = style()
  )
}
