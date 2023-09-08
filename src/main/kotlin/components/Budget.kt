package components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.optics.optics
import com.xebia.functional.xef.llm.ChatWithFunctions

var primaryColor: Color = Color.Teal
var secondaryColor: Color = Color.Blue
var onPrimaryColor: Color = Color.Black
var onSecondaryColor: Color = Color.White

@optics data class Budget(
  val text: Text,
  val background: Color = primaryColor,
  val borderColor: Color = onPrimaryColor,
  val textColor: Color = onPrimaryColor,
) {
  companion object {
    val Xef = Budget(
      text = TextBudget(text = "Xef"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Context = Budget(
      text = TextBudget(text = "Context"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Images = Budget(
      text = TextBudget(text = "Images"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Llm = Budget(
      text = TextBudget(text = "Llm"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val OpenAi = Budget(
      text = TextBudget(text = "OpenAi"),
      background = Color.Teal,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Request = Budget(
      text = TextBudget(text = "Request"),
      background = Color.Blue,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Response = Budget(
      text = TextBudget(text = "Response"),
      background = Color.Yellow,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Chat = Budget(
      text = TextBudget(text = "Chat"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val ChatWithFunctions = Budget(
      text = TextBudget(text = "ChatWithFunctions"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Image = Budget(
      text = TextBudget(text = "Image"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Embedding = Budget(
      text = TextBudget(text = "Embedding"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Completion = Budget(
      text = TextBudget(text = "Completion"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Chunk = Budget(
      text = TextBudget(text = "Chunk"),
      background = primaryColor,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val System = Budget(
      text = TextBudget(text = "System"),
      background = Color.Teal,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Assistant = Budget(
      text = TextBudget(text = "Assistant"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Function = Budget(
      text = TextBudget(text = "Function"),
      background = Color.Yellow,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val User = Budget(
      text = TextBudget(text = "User"),
      background = Color.Green,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Tool = Budget(
      text = TextBudget(text = "Tool"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Summarize = Budget(
      text = TextBudget(text = "Summarize"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val ReadPDF = Budget(
      text = TextBudget(text = "ReadPdf"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Observation = Budget(
      text = TextBudget(text = "Observation"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val FinalAnswer = Budget(
      text = TextBudget(text = "FinalAnswer"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Search = Budget(
      text = TextBudget(text = "Search"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Thinking = Budget(
      text = TextBudget(text = "Thinking"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )

    val Agent = Budget(
      text = TextBudget(text = "Agent"),
      background = Color.Pastel,
      borderColor = onPrimaryColor,
      textColor = onPrimaryColor,
    )
  }
}

@Composable
operator fun Budget.invoke() {
  text(
    modifier = Modifier
      .border(width = 1.dp, color = borderColor(), shape = CircleShape)
      .background(background(), CircleShape)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  )
}