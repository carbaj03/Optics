package colors

import common.Info
import react.AgentItem

sealed interface ColorsInfo : Info {
  val content: AgentItem
  val onClick: () -> Unit

  data class Result(
    override val content: AgentItem,
    override val onClick: () -> Unit = {}
  ) : ColorsInfo
}