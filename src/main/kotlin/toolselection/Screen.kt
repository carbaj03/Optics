package toolselection

import androidx.compose.runtime.Composable
import common.Show
import screen.ToolSelection

@Composable
operator fun ToolSelection.invoke() {
  Show()
}