package common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import components.Filter
import components.invoke
import react.ChipGroup
import react.ReactInfo
import react.invoke
import screen.GenericScreen
import toolselection.ToolSelectionInfo

@Composable
fun GenericScreen.Show() {
  val lazyListState =  rememberLazyListState()

  LaunchedEffect(rows.size) {
    if(rows.isNotEmpty()) lazyListState.animateScrollToItem(rows.size -1)
  }

  Scaffold(
    topBar = { topBar() }
  ) {
    Column(
      modifier = Modifier.padding(8.dp)
    ) {
      Row {
        load()
        cancel()
      }

      var search by remember { mutableStateOf("") }
      var leadingIcon: @Composable (() -> Unit)? by remember { mutableStateOf(null) }

      if(false) {
        OutlinedTextField(
          modifier = Modifier.onFocusChanged {
            leadingIcon = if (!it.hasFocus) null else @Composable {
              {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                )
              }
            }
          },
          value = search,
          onValueChange = { search = it },
          shape = CircleShape,
          leadingIcon = leadingIcon,
        )
      }

      var filter: Filter by remember { mutableStateOf(Filter()) }
      ChipGroup(filter = filter, filterChange = { filter = it.onSelected(filter) })

      rows.filter {
        filter.noneSelected()
                || filter.xef.isSelected && it.value is Info.Xef
                || filter.openAI.isSelected && it.value is Info.OpenAI
                || filter.agent.isSelected && it.value is Info.Agent
      }(modifier = Modifier.weight(1f), state = lazyListState, isLoading = isLoading)
    }
  }
}