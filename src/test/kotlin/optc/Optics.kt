package optc

import arrow.optics.Optional
import common.Store
import common.get
import common.invoke
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class Optics {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `app should update deep data`(): TestResult = runTest {
        App(
            initialState = App(),
            scope = this
        ) {
            theme set Dark

            when (user.get()) {
                is Anonymous -> screen set Screen1()
                is Logged -> screen set Screen2()
            }

            screen set Screen2()

            screen.screen2 {
                toolbar.title.value set "Login"
                counter.value set "324"
                text transform { it + Text("Hola") }
            }

            screen.screen2.toolbar.title.value asserEquals "Login"

            screen set Screen1()

            screen.screen1.login.onClick()

            screen.screen2.toolbar.title.value set "Logout"

            screen.screen2.toolbar.title.value asserEquals "Logout"

            screen.screen2.toolbar.itemLeft()

            screen.screen1.toolbar.title.value asserEquals "Screen 1"
        }
    }
}

context(Store<A>)
infix fun <A, B> Optional<A, B>.asserEquals(b: B){
    Assert.assertEquals(b, get())
}
