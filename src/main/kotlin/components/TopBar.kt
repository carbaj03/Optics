package components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import arrow.optics.optics

@optics data class TopBar(
  val title: Text,
  val navigationIcon: Button,
) {
  companion object
}

@Composable
operator fun TopBar.invoke() {
  TopAppBar(
    title = { title() },
    navigationIcon = {
      IconButton({ navigationIcon.onClick() }) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = null
        )
      }
    },
  )
}