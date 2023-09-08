package screen

import arrow.optics.optics
import common.Info
import components.Button
import components.Filter
import components.SearchBox
import components.TopBar

@optics sealed interface GenericScreen : Screen {
  val topBar: TopBar
  val load: Button
  val cancel: Button
  val back: Button
  val search: SearchBox
  val filter: Filter
  val rows: List<Row<Info>>
  val info: List<Boolean>
  val isLoading : Boolean

  companion object
}