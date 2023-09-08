package components

import androidx.compose.runtime.Composable
import arrow.optics.optics
import common.*

@optics sealed interface Color {

  data object White : Color
  data object Red : Color
  data object Green : Color
  data object Blue : Color
  data object Black : Color
  data object Pastel : Color
  data object Teal : Color
  data object Yellow : Color
  data object Transparent : Color

  companion object
}

@Composable
operator fun Color.invoke(): androidx.compose.ui.graphics.Color =
  when (this) {
    Color.Blue -> androidx.compose.ui.graphics.Color.BluePastel
    Color.Green -> androidx.compose.ui.graphics.Color.GreenPastel
    Color.Black -> androidx.compose.ui.graphics.Color.Black
    Color.Red -> androidx.compose.ui.graphics.Color.Red
    Color.White -> androidx.compose.ui.graphics.Color.White
    Color.Pastel -> androidx.compose.ui.graphics.Color.Pastel
    Color.Teal -> androidx.compose.ui.graphics.Color.Teal
    Color.Yellow -> androidx.compose.ui.graphics.Color.YellowPastel
    Color.Transparent -> androidx.compose.ui.graphics.Color.Transparent
  }