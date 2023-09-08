package screen

import App
import arrow.optics.Optional
import common.LoggerImpl
import common.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job

context(Store<App>, App.Companion)
operator fun <A : Screen> Optional<App, A>.invoke(f: context(Store<App>, App.Companion, CoroutineScope, LoggerImpl, Optional<App, A>) () -> A): A {
  val scope = CoroutineScope(SupervisorJob(parent.coroutineContext.job))
  val logger = LoggerImpl()

  return f(this@Store, this@Companion, scope, logger, this@invoke)
}
