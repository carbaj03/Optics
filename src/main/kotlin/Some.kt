package c

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

abstract class Action {
    fun sleep(body: Person.() -> Unit) {
        var p = Person("a")
        p.body()
        println(p.name + " is zzzzzzz...")
    }
}

class Person(var name: String = "") {
    companion object : Action()
}

inline fun <reified T> test() {
    val companionObject: KClass<*>? = T::class.companionObject
    if (companionObject != null) {
        val body: Person.() -> Unit = { println("body called on $name!") }
        val companionInstance = T::class.companionObjectInstance
        val functionEx = companionObject.functions.first { it.name.equals("sleep") }
        functionEx.call(companionInstance, body)
    }
}

fun main(args: Array<String>) {
    Person.sleep {
        this.name = "abc"
    }
    test<Person>()
}