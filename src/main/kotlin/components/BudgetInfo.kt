package components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BudgetInfo(
  text: String,
  textColor: Color = MaterialTheme.colors.onPrimary,
  color: Color = MaterialTheme.colors.primary,
  border: Color = MaterialTheme.colors.onPrimary,
) {
  Text(
    modifier = Modifier
      .border(1.dp, border, CircleShape)
      .background(color, CircleShape)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    text = text,
    style = MaterialTheme.typography.caption,
    color = textColor,
  )
}

@Composable
fun Tokens(tokens: Int) {
  BudgetInfo(
    text = "$tokens tokens",
    color = MaterialTheme.colors.background,
    textColor = MaterialTheme.colors.onBackground,
    border = MaterialTheme.colors.onBackground,
  )
}