package chat

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val app = ChatApp(
  initialState = ChatApp(),
) {
  screen set Splash(
    next = {
      launch {
        delay(2000)

        screen set HomeScreen(
          loginSocial = Button(
            text = Text("Login Social"),
            onClick = {
              screen set WelcomeScreen(
                next = Button(
                  text = Text("Next"),
                  onClick = { screen set ChatScreen() }
                )
              )
            }
          ),
          loginEmail = Button(
            text = Text("Login Email"),
            onClick = {
              screen set WelcomeScreen(
                next = Button(
                  text = Text("Next"),
                  onClick = {}
                )
              )
            }
          ),
          register = Button(
            text = Text("Register"),
            onClick = {
              screen set WelcomeScreen(
                next = Button(
                  text = Text("Next"),
                  onClick = {
                  }
                )
              )
            }
          ),
          forgotPassword = Button(
            text = Text("Forgot Password"),
            onClick = {
              screen set WelcomeScreen(
                next = Button(
                  text = Text("Next"),
                  onClick = {
                  }
                )
              )
            }
          ),
        )
      }
    }
  )
}
