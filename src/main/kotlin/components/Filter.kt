package components

import arrow.optics.optics
import common.Icon

@optics data class Filter(
  val agent: ChipSelectable = ChipSelectable(
    text = "Agent",
    icon = Icon.Agent,
    isSelected = false,
    onSelected = { it.copy(agent = it.agent.copy(isSelected = !it.agent.isSelected)) }
  ),
  val openAI: ChipSelectable = ChipSelectable(
    text = "OpenAI",
    icon = Icon.OpenAI,
    isSelected = false,
    onSelected = { it.copy(openAI = it.openAI.copy(isSelected = !it.openAI.isSelected)) }
  ),
  val xef: ChipSelectable = ChipSelectable(
    text = "Xef",
    icon = Icon.Xef,
    isSelected = false,
    onSelected = { it.copy(xef = it.xef.copy(isSelected = !it.xef.isSelected)) }
  ),
) {
  val list = listOf(agent, openAI, xef)
  fun noneSelected() = list.none { it.isSelected }

  companion object
}