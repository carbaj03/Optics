package react

import arrow.optics.optics
import chat.Button
import chef.Info
import common.State

@optics data class ReActApp(
  val screen: Screen? = null,
  val user: String = "",
  val track: List<Info> = emptyList(),
) : State {
  companion object
}

@optics sealed interface Screen {
  companion object
}

@optics data class Home(
  val load: Button,
  val cancel : Button
) : Screen {
  companion object
}
