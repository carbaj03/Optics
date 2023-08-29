package chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DrawerValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import chef.invoke
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.lang.Infer
import kotlinx.serialization.Serializable


@Composable
operator fun ChatScreen.invoke() {
  val scaffoldState = rememberScaffoldState()

  Scaffold(
    scaffoldState = scaffoldState,
    topBar = { toolbar() },
    bottomBar = {
      Row {
        input()
        send()
      }
    },
    drawerContent = { sideBar(scaffoldState) }
  ) {
    LazyColumn {
      items(messages) { message ->
        when (message) {
          is UserMessage -> Text(message.text, color = Color.Blue())
          is AssitantMessage -> {
            Text(text = message.text, color = Color.Green())
            Box(modifier = Modifier.background(color = Color.Blue())) {
              Text(text = extractCode(message.text), color = Color.Green())
            }
          }
        }
      }
    }
  }
}