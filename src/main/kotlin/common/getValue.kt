package common

import arrow.core.continuations.Raise
import arrow.optics.Fold
import arrow.optics.Lens
import arrow.optics.Optional
import arrow.optics.Prism
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlin.reflect.KProperty

context(Store<A>, Raise<Error>)
operator fun <A, B> Optional<A, B>.getValue(thisObj: Any?, property: KProperty<*>): B =
    getOrNull(state.value) ?: raise(UnknownError("Optional is not defined"))

context(Store<A>, Raise<Error>)
operator fun <A, B> Optional<A, B>.component1(): B =
    getOrNull(state.value)?: raise(UnknownError("Optional is not defined"))

context(Store<A>, Raise<Error>)
operator fun <A, B> Optional<A, B>.component2(): B =
    getOrNull(state.value)?: raise(UnknownError("Optional is not defined"))

context(Store<A>)
operator fun <A, B> Optional<A, B>.setValue(thisObj: Any?, property: KProperty<*>, value: B) {
    this set value
}

context(Store<A>)
operator fun <A> Optional<A, () -> Unit>.invoke(): Unit =
    getOrNull(state.value)!!.invoke()

context(Store<A>)
fun <A, B> Optional<A, B>.get(): B =
    getOrNull(state.value)!!

context(Store<A>)
fun <A, B> Lens<A, B>.get(): B =
    getOrNull(state.value)!!


context(Store<A>)
fun <A, B> Optional<A, B>.getOrNull(): B? =
    getOrNull(state.value)

context(Store<A>)
inline operator fun <A, B> Optional<A, B>.invoke(f: context(CoroutineScope, Optional<A, B>) () -> B): B {
    val scope = CoroutineScope(SupervisorJob(parent.coroutineContext.job) )
    return f(scope,this)
}

inline  fun <A, B> Optional<A, B>.scope(f:  Optional<A, B>.() -> Unit){
     f()
}

context(MutableStateFlow<A>)
infix fun <A, B> Lens<A, B>.set(name: B) {
    value = set(value, name)
}

context(MutableStateFlow<A>)
infix fun <A, B> Prism<A, B>.set(name: B) {
    value = set(value, name)
}

context(MutableStateFlow<A>)
infix fun <A, B> Optional<A, B>.set(name: B) {
    value = set(value, name)
}

context(MutableStateFlow<A>)
infix fun <A, B> Fold<A, B>.all(predicate: (focus: B) -> Boolean) {
    all(value, predicate)
}