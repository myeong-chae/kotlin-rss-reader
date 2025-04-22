package coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CoroutineTest {
    @Test
    fun test1() =
        runBlocking {
            init()
            getRssScope2()
            delay(4000)
            RSS.filtering("가")
            println(RSS.mainList.size)
        }

    @Test
    fun test2() =
        runBlocking {
            init()
            updateTime = 10000
            getRssScope3()
            delay(4000)
            RSS.deleteRandom()
            RSS.deleteRandom()
            RSS.deleteRandom()
            delay(10000)
        }
}
