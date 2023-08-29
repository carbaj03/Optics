import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChipShowCase() {

  val x = ChipDefaults.filterChipColors(selectedContentColor = Color.White, selectedBackgroundColor = Color.Black)
  val s = MaterialTheme.shapes.small.copy(CornerSize(percent = 50))

  var selected by remember { mutableStateOf(false) }
  var shape by remember { mutableStateOf(s) }
  var border by remember { mutableStateOf(BorderStroke(1.dp, Color.Black)) }
  var colors by remember { mutableStateOf(x) }
  var leadingIcon by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }
  var selectedIcon by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }
  var trailingIcon by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }

  Row {

    FilterChip(
      modifier = Modifier,
      selected = selected,
      onClick = { selected = !selected; border = if (selected) BorderStroke(1.dp, Color.Cyan) else BorderStroke(1.dp, Color.Black) },
      enabled = true,
      interactionSource = remember { MutableInteractionSource() },
      shape = shape,
      border = border,
//      colors = colors,
      leadingIcon = leadingIcon,
      selectedIcon = selectedIcon,
      trailingIcon = trailingIcon,
    ) {
      Text("example")
    }

    Column {
      Row {
        Text("shape")
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
          value = shape.toString(),
          onValueChange = { shape = s },
          label = { Text("shape") },
          modifier = Modifier.padding(8.dp)
        )
      }
      Row {
        Text("border")
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
          value = border.toString(),
          onValueChange = { border = BorderStroke(1.dp, Color.Black) },
          label = { Text("border") },
          modifier = Modifier.padding(8.dp)
        )
      }
      Row {
        Text("colors")
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
          value = colors.toString(),
          onValueChange = { colors = x },
          label = { Text("colors") },
          modifier = Modifier.padding(8.dp)
        )
      }
      Row {
        Text("leadingIcon")
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
          value = leadingIcon.toString(),
          onValueChange = { leadingIcon = null },
          label = { Text("leadingIcon") },
          modifier = Modifier.padding(8.dp)
        )
      }
      Row {
        Text("selectedIcon")
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
          value = selectedIcon.toString(),
          onValueChange = { selectedIcon = null },
          label = { Text("selectedIcon") },
          modifier = Modifier.padding(8.dp)
        )
      }
      Row {
        Text("trailingIcon")
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
          value = trailingIcon.toString(),
          onValueChange = { trailingIcon = null },
          label = { Text("trailingIcon") },
          modifier = Modifier.padding(8.dp)
        )
      }
    }
  }
}
