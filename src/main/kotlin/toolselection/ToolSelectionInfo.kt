package toolselection

import com.xebia.functional.xef.reasoning.pdf.ReadPDFTracing
import com.xebia.functional.xef.reasoning.text.summarize.*
import com.xebia.functional.xef.reasoning.tools.Completed
import com.xebia.functional.xef.reasoning.tools.TaskEvent
import com.xebia.functional.xef.reasoning.tools.ToolSelectionTracing
import common.Info
import components.Budget
import components.TextAgent
import react.AgentItem

sealed interface ToolSelectionInfo : Info.Agent {
  val content: AgentItem
  val onClick : () -> Unit

  data class Trace(
    override val content: AgentItem,
    override val onClick: () -> Unit = {}
  ) : ToolSelectionInfo
}

fun ToolSelectionTracing.toInfo(): ToolSelectionInfo =
  when (this) {
    is TaskEvent.ApplyingTool -> "🔍 Applying inferred tools for task: $task"
    is TaskEvent.ApplyingPlan -> "🔍 Applying execution plan with reasoning: $reasoning"
    is TaskEvent.ApplyingToolOnStep -> "🔍 Applying tool: $tool for step: $reasoning"
    is TaskEvent.CreatingExecutionPlan -> "🔨 Creating execution plan for task: $task"
    is Completed -> """
        step : $step
        output : $output
      """.trimIndent()
  }.let {
    ToolSelectionInfo.Trace(
      AgentItem(
        content = TextAgent(it),
        budgets = listOf(Budget.Tool)
      )
    )
  }

fun SummarizeTracing.toInfo(): ToolSelectionInfo =
  when (this) {
    is SummarizingChunk -> "📝 Summarizing chunk with prompt tokens $tokens for length $length"
    is SummarizedChunk -> "📝 Summarized chunk in tokens: $tokens"
    is SummarizingText -> "📚 Summarizing large text of tokens $tokens to approximately $length tokens"
    is SplitText -> "📚 Split text into $chunks chunks"
    is SummarizedChunks -> "📚 Summarized $count chunks"
  }.let {
    ToolSelectionInfo.Trace(
      AgentItem(
        content = TextAgent(it),
        budgets = listOf(Budget.Summarize),
      )
    )
  }

fun ReadPDFTracing.toInfo(): ToolSelectionInfo =
  when (this) {
    is ReadPDFTracing.ReadingUrl -> "Reading url $url"
  }.let {
    ToolSelectionInfo.Trace(
      AgentItem(
        content = TextAgent(it),
        budgets = listOf(Budget.ReadPDF)
      )
    )
  }