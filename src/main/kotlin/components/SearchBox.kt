package components

import arrow.optics.optics

@optics data class SearchBox(
  val text: Text,
  val onValueChange: (String) -> Unit,
) {
  companion object
}
