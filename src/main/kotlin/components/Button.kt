package components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import arrow.optics.optics
import common.Icon

@optics data class Button(
  val text: Text,
  val onClick: () -> Unit,
) {
  companion object
}

@Composable
operator fun Button.invoke() = androidx.compose.material.Button(onClick = onClick, shape = CircleShape) {
  text()
}