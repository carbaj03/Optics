@file:Suppress("UnusedReceiverParameter")

package components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.Spacer(dp: Int) {
  androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(dp.dp))
}

@Composable
fun RowScope.Spacer(weight: Float) {
  androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(weight))
}

@Composable
fun ColumnScope.Spacer(dp: Int) {
  androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(dp.dp))
}

@Composable
fun ColumnScope.Spacer(weight: Float) {
  androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(weight))
}