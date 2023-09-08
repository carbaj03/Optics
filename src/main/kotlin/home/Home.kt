package home

import Mode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.invoke
import screen.Home

@OptIn(ExperimentalMaterialApi::class)
@Composable
operator fun Home.invoke(isLight: Mode) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Home") },
        actions = {
          IconButton(onClick = { mode.onClick() }) {
            Icon(
              imageVector = if (isLight == Mode.Light) Icons.Default.Check else Icons.Default.Clear,
              contentDescription = null,
            )
          }
        })
    }
  ) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalArrangement =  Arrangement.spacedBy(10.dp),
    ) {
      item {
        Card(
          onClick = { reAct.onClick() },
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
          ) {
            reAct.text()
            Text("Show case of a ReAct", style = MaterialTheme.typography.body1)
          }
        }
      }

      item {
        Card(
          onClick = { toolChain.onClick() }
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            toolChain.text()
            Text("Show case for tool chain", style = MaterialTheme.typography.body1)
          }
        }
      }

      item {
        Card(
          onClick = { colors.onClick() }
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            colors.text()
            Text("Create a palette of colours", style = MaterialTheme.typography.body1)
          }
        }
      }
    }
  }
}