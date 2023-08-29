package chat

import common.get
import common.getValue
import common.invoke
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import optc.asserEquals
import org.junit.Assert
import org.junit.Test


class ChatTest {

    val mockChatRepository = object  : ChatRepository {
        override suspend fun prompt(question: String): Message {
          delay(200)
            return AssitantMessage("asfsadf")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `app should update deep data`(): TestResult = runTest {
        ChatApp(
            initialState = ChatApp(),
            scope = this
        ){
                                screen set ChatScreen(mockChatRepository)
advanceUntilIdle()
          screen.chatScreen.input.action.onClick()
              advanceUntilIdle()
          val msg by screen.chatScreen.messages
              advanceUntilIdle()

          val (a, b) = msg
        when(a){
            is AssitantMessage -> Assert.assertEquals("asfsadf", a.text)
            is UserMessage -> Assert.assertEquals("", a.text)
            }

          when(b){
            is AssitantMessage -> Assert.assertEquals("asfsadf", b.text)
            is UserMessage -> Assert.assertEquals("", b.text)
          }

          screen.chatScreen.input.action.onClick()
          advanceTimeBy(100)
          screen.chatScreen.send.onClick()
          advanceTimeBy(200)
          screen.chatScreen.input.action.onClick()
          advanceUntilIdle()

          Assert.assertEquals(5, msg.size)
        }
    }
}