package chat

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import chef.Ingredient

@Composable
fun RecipePrompt(
  title: String,
  ingredients: List<Ingredient>,
  prepTime: Int,
  cookTime: Int,
  servings: Int,
  steps: List<String>,
  notes: String,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.h1,
    )
    Text(
      text = "Ingredients",
      style = MaterialTheme.typography.h2,
    )
    LazyColumn {
      items(ingredients) { ingredient ->
        Text(
          text = "${ingredient.name} (${ingredient.quantity} ${ingredient.unit})",
          style = MaterialTheme.typography.body1,
        )
      }
    }
    Text(
      text = "Instructions",
      style = MaterialTheme.typography.h2,
    )
    LazyColumn {
      items(steps) { step ->
        Text(
          text = step,
          style = MaterialTheme.typography.body1,
        )
      }
    }
    Text(
      text = "Notes",
      style = MaterialTheme.typography.h2,
    )
    Text(
      text = notes,
      style = MaterialTheme.typography.body1,
    )
  }
}

fun main() = application {
  Window(onCloseRequest = ::exitApplication) {
    RecipePromptPreview()
  }
}

@Preview
@Composable
fun RecipePromptPreview() {
  RecipePrompt(
    title = "Grilled Mediterranean Chicken",
    ingredients = listOf(
      Ingredient(name = "Chicken", quantity = "4", unit = "pieces"),
      Ingredient(name = "Olive oil", quantity = "2", unit = "tablespoons"),
      Ingredient(name = "Lemon juice", quantity = "2", unit = "tablespoons"),
      Ingredient(name = "Garlic", quantity = "4", unit = "cloves"),
      Ingredient(name = "Dried oregano", quantity = "1", unit = "teaspoon"),
      Ingredient(name = "Salt", quantity = "1/2", unit = "teaspoon"),
      Ingredient(name = "Black pepper", quantity = "1/4", unit = "teaspoon"),
    ),
    prepTime = 15,
    cookTime = 15,
    servings = 4,
    steps = listOf(
      "In a small bowl, mix together the olive oil, lemon juice, minced garlic, dried oregano, salt, and black pepper.",
      "Place the chicken pieces in a shallow dish and pour the marinade over them. Make sure all the chicken pieces are coated with the marinade. Let it marinate for at least 30 minutes, or up to 24 hours in the refrigerator.",
      "Preheat the grill to medium-high heat. Remove the chicken from the marinade and discard the remaining marinade.",
      "Grill the chicken for about 6-8 minutes per side, or until the internal temperature reaches 165°F (74°C).",
      "Remove the chicken from the grill and let it rest for a few minutes before serving.",
      "Serve the grilled Mediterranean chicken with your favorite side dishes and enjoy!",
    ),
    notes = "If you don't have a grill, you can also cook the chicken in a grill pan or bake it in the oven at 400°F (200°C) for about 20-25 minutes.",
  )
}