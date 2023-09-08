package react

import androidx.compose.runtime.Composable
import common.Show
import screen.ReAct

@Composable
operator fun ReAct.invoke() {
  Show()
}