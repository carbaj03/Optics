package chef

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import chat.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import react.invoke

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
operator fun Home.invoke() {
  Row {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).width(300.dp)) {
      cuisine()
      Spacer(Modifier.height(8.dp))
      ingredients()
      Spacer(Modifier.height(8.dp))
      cookingMethod()
      Spacer(Modifier.height(8.dp))

      TextField(
        value = "",
        onValueChange = {},
        label = { androidx.compose.material.Text("Alergents") }
      )

      TextField(
        value = "",
        onValueChange = {},
        label = { androidx.compose.material.Text("Disliked Ingredients") }
      )

      maxCalories()
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
      image?.let {
        KamelImage(
          asyncPainterResource(data = it),
          modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(8.dp)),
          contentDescription = "Translated description of what the image contains"
        )
      }
      Row {
        generateRecipe()
        cancel()
      }
      if (isLoading) CircularProgressIndicator()
      recipePrompt?.invoke()
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
operator fun Slider.invoke() {
  androidx.compose.material.Slider(
    value = value,
    onValueChange = { onValueChange(it) },
    valueRange = 0f..4000f,
    steps = 1000,
    modifier = Modifier.width(200.dp)
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
operator fun ChipGroup.invoke() {
  Column(
    modifier = Modifier
      .background(Color.Magenta.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
      .fillMaxWidth()
      .padding(8.dp)
  ) {
    androidx.compose.material.Text(text = title, style = MaterialTheme.typography.caption)
    FlowRow {
      chips.forEach {
        it(onSelect = { onChipClick(it) })
        Spacer(Modifier.width(4.dp))
      }
    }
  }
}

@Composable
operator fun RecipePrompt.invoke() {
  Column {
    Text(text = title, style = MaterialTheme.typography.h4)
    Row {
      Text("Prep Time: $prepTime", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
      Text("Cook Time: $cookTime", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
      Text("Servings: $servings", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
//      androidx.compose.material.Text("calories: $calories", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
    }
    Spacer(Modifier.height(8.dp))
    Text(text = "Ingredients", style = MaterialTheme.typography.h6)
    ingredients.forEach {
      androidx.compose.material.Text("${it.name} ${it.quantity} ${it.unit}")
    }
    Spacer(Modifier.height(8.dp))
    Text("Steps", style = MaterialTheme.typography.h6)
    steps.forEach {
      androidx.compose.material.Text("·" + it)
      Spacer(Modifier.height(4.dp))
    }
    Spacer(Modifier.height(8.dp))
    androidx.compose.material.Text("Notes: $notes")
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
operator fun Chip.invoke(onSelect: (Chip) -> Unit) {
  FilterChip(
    selected = selected,
    onClick = { onSelect(this) },
  ) {
    androidx.compose.material.Text(text)
  }
}

fun main() = application {
  Window(
    state = rememberWindowState(size = DpSize(1200.dp, 1200.dp)),
    onCloseRequest = ::exitApplication
  ) {
    val state by app.collectAsState()

    Row {
      Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
        when (val screen = state.screen) {
          is Home -> screen()
          is Splash -> {
            LaunchedEffect(Unit) {
              screen.next()
            }
          }
          null -> androidx.compose.material.Text("Error")
        }
      }
      state.track(modifier = Modifier.width(600.dp))
    }
  }
}

@Composable
operator fun Button.invoke() = androidx.compose.material.Button(onClick = onClick, shape = CircleShape) {
  text()
}

@Composable
operator fun ButtonIcon.invoke() = androidx.compose.material.Button(onClick = onClick, shape = CircleShape) {
  icon()
}

@Composable
operator fun Text.invoke() = androidx.compose.material.Text(text = text)

@Composable
operator fun SideBar.invoke(scaffoldState: ScaffoldState) {

  LaunchedEffect(open) {
    if (open) scaffoldState.drawerState.open()
    else scaffoldState.drawerState.close()
  }

  LaunchedEffect(scaffoldState.drawerState.isOpen) {
    snapshotFlow { scaffoldState.drawerState.currentValue }.collect {
      when (it) {
        DrawerValue.Closed -> onChange(false)
        DrawerValue.Open -> onChange(true)
      }
    }
  }
  Column {
    header()
    items.forEach { it() }
  }
}

@Composable
operator fun SideBarHeader.invoke() = Column {
  icon()
  title()
  subtitle()
  plan()
}

@Composable
operator fun Toolbar.invoke() = androidx.compose.material.TopAppBar(
  title = { title() },
  actions = { itemRight { map { it() } } },
  navigationIcon = itemLeft {
    icon(modifier = Modifier.clickable { onClick() })
  },
)

@Composable
operator fun Input.invoke() = OutlinedTextField(
  modifier = Modifier.padding(8.dp),
  value = text,
  onValueChange = onTextChange,
  leadingIcon = action {
    Icon(
      modifier = Modifier.clickable { onClick() },
      imageVector = Icons.Default.Favorite,
      contentDescription = null
    )
  },
  shape = CircleShape,
  colors = TextFieldDefaults.outlinedTextFieldColors(
    backgroundColor = androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f),
    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
  )
)