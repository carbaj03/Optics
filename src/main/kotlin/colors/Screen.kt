package colors

import androidx.compose.runtime.Composable
import common.Show
import screen.ColorsScreen

@Composable
operator fun ColorsScreen.invoke() {
  Show()
}