package react

import com.xebia.functional.xef.reasoning.tools.ReactAgentEvents
import common.Info
import components.Budget
import components.Text
import components.TextAgent

sealed interface ReactInfo : Info.Agent {
  val content: AgentItem
  val onClick: () -> Unit

  data class Thinking(override val content: AgentItem, override val onClick: () -> Unit = {}) : ReactInfo
  data class Search(override val content: AgentItem, override val onClick: () -> Unit = {}) : ReactInfo
  data class Observation(override val content: AgentItem, override val onClick: () -> Unit = {}) : ReactInfo
  data class FinalAnswer(override val content: AgentItem, override val onClick: () -> Unit = {}) : ReactInfo
}

fun ReactAgentEvents.toInfo(): ReactInfo =
  when (this) {
    is ReactAgentEvents.FinalAnswer -> ReactInfo.FinalAnswer(
      AgentItem(
        content = TextAgent( answer),
        budgets = listOf(Budget.Agent, Budget.FinalAnswer)
      )
    )
    is ReactAgentEvents.MaxIterationsReached -> TODO()
    is ReactAgentEvents.Observation -> ReactInfo.Observation(
      AgentItem(
        content = TextAgent(value),
        budgets = listOf(Budget.Agent, Budget.Observation)
      )
    )
    is ReactAgentEvents.SearchingTool -> ReactInfo.Search(
      AgentItem(
        content = TextAgent(tool + "[${input}]"),
        budgets = listOf(Budget.Agent, Budget.Search)
      )
    )
    is ReactAgentEvents.Thinking -> ReactInfo.Thinking(
      AgentItem(
        content = TextAgent(though),
        budgets = listOf(Budget.Agent, Budget.Thinking)
      )
    )
    is ReactAgentEvents.ToolNotFound -> TODO()
  }
