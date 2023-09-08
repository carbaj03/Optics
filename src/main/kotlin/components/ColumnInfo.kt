package components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun ColumnInfo(
  label: String,
  text: String,
  color: Color = MaterialTheme.colors.surface
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(text = label, style = MaterialTheme.typography.subtitle2)
    Spacer(dp = 8)
    BudgetInfo(text = text, color = color, border = MaterialTheme.colors.onPrimary)
  }
}
