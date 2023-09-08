package components

import arrow.optics.optics
import common.Icon

@optics data class ChipSelectable(
  val text: String,
  val icon: Icon,
  val isSelected: Boolean,
  val onSelected: (Filter) -> Filter
) {
  companion object
}