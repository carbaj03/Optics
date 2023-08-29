package common

import arrow.optics.Copy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface State

interface Store<A> : Copy<A> {
    val state: StateFlow<A>
    val parent : CoroutineScope
}