package components

import arrow.optics.optics
import common.Icon

@optics data class Card(
  val title: Text,
  val subtitle: Text,
  val icon: Icon,
  val plan: Text,
  val onClick: () -> Unit,
) {
  companion object
}