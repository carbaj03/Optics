package screen

import App
import Mode.*
import arrow.optics.optics
import common.Store
import common.get
import components.Button
import components.Text
import mode
import screen

@optics data class Home(
  val reAct: Button,
  val toolChain: Button,
  val colors: Button,
  val mode: Button,
) : Screen {
  companion object
}

context(Store<App>, App.Companion)
fun createHome(): Home = screen.home {

  Home(
    reAct = Button(
      onClick = { screen set createReAct() },
      text = Text("ReAct", style = components.TextStyle.H4)
    ),
    toolChain = Button(
      onClick = { screen set createToolSelection() },
      text = Text("Tool Selection", style = components.TextStyle.H4)
    ),
    colors = Button(
      onClick = { screen set createColors() },
      text = Text("Colors", style = components.TextStyle.H4)
    ),
    mode = Button(
      onClick = {
        when (mode.get()) {
          Dark -> mode set Light
          Light -> mode set Dark
          Dynamic -> TODO()
        }
      },
      text = Text("Mode")
    ),
  )
}