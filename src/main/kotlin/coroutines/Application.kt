package coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.w3c.dom.Node
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.random.Random

data class RSS(
    val title: String,
    val date: Date,
    val summary: String,
) {
    companion object {
        val mutex = Mutex()
        var mainList: MutableList<RSS> = mutableListOf()
            set(value) {
                synchronized(this) {
                    if (field != value) {
                        compare(field, value)
                        field = value
                    }
                }
            }

        fun deleteRandom() {
            synchronized(mainList) {
                mainList.removeAt(Random.nextInt(mainList.size))
            }
        }

        fun filtering(filter: String) {
            var count = 0
            synchronized(mainList) {
                for (list in mainList) {
                    if (filter in list.title) {
                        count++
                        list.printRSS(count)
                        if (count == 10) {
                            break
                        }
                    }
                }
            }
        }

        fun tryLock() {
            while (!mutex.tryLock()) {
                Thread.sleep(1)
            }
        }

        fun unLock() {
            mutex.unlock()
        }
    }

    fun printRSS(count: Int) {
        val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val formattedDate = outputFormat.format(date)
        println("[$count] $title ($formattedDate) - $summary")
    }

    fun printNewRSS() {
        val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val formattedDate = outputFormat.format(date)
        println("[NEW] $title ($formattedDate) - $summary")
    }
}

val urls: MutableMap<String, String> =
    mutableMapOf()

var updateTime = 600000

fun compare(
    old: MutableList<RSS>,
    new: MutableList<RSS>,
) {
    if (old.isNotEmpty()) {
        new.removeAll(old)
        new.forEach {
            it.printNewRSS()
        }
    }
}

fun main() {
    init()
    getRssScope2()
    runBlocking {
        while (isActive) {
            println("검색어를 입력하세요")
            val input = readln()
            RSS.filtering(input)
        }
    }
}

fun init() {
    urls["D2 Blog"] = "https://d2.naver.com/d2.atom"
    urls["자바캔(Java Can Do IT) (최범균)"] = "https://javacan.tistory.com/rss"
    urls["기억보단 기록을 (이동욱)"] = "https://jojoldu.tistory.com/rss"
    urls["개린이의 일기장"] = "https://hyune-c.tistory.com/rss"
    urls["우아한형제들 기술 블로그"] = "https://techblog.woowahan.com/feed/"
    urls["현대"] = "https://developers.hyundaimotorgroup.com/blog/rss"
//    urls["컬리 기술 블로그"] = "https://helloworld.kurly.com/feed.xml"
//    urls["NHN Cloud Meetup"] = "https://meetup.toast.com/rss"
//    urls["MUSINSA tech"] = "https://medium.com/feed/musinsa-tech"
//    urls["에디의 기술 블로그"] = "https://brunch.co.kr/rss/@@2MrI"
//    urls["Eternity's Chit-Chat (조영호)"] = "http://rss.egloos.com/blog/aeternum"
//    urls["포스타입 팀"] = "https://team.postype.com/rss"
}

fun getRss(text: String): MutableList<RSS> {
    val retList: MutableList<RSS> = mutableListOf()
    val factory = DocumentBuilderFactory.newInstance()
    val xml =
        factory.newDocumentBuilder().parse(text)
    val channel = xml.getElementsByTagName("title")
    val date = xml.getElementsByTagName("pubDate")
    val summary = xml.getElementsByTagName("link")
    for (i in 0..<channel.length) {
        val node: Node? = channel.item(i)
        val temp: Node? = node?.firstChild
        val value: String? = temp?.nodeValue
        if (value == null) {
            continue
        }

        val node1: Node? = date.item(i)
        val temp1: Node? = node1?.firstChild
        val value1: String = temp1?.nodeValue ?: ""
        if (value1 == "") {
            continue
        }
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        val date: Date = format.parse(value1)!!

        val node2: Node? = summary.item(i)
        val temp2: Node? = node2?.firstChild
        val value2: String = temp2?.nodeValue ?: ""

        retList.add(RSS(value, date, value2))
    }
    return retList
}

suspend fun getRssScope(): MutableList<RSS> {
    val retMap: MutableList<RSS> =
        mutableListOf()
    val jabs: MutableList<Job> = mutableListOf()
    urls.forEach { (_, value) ->
        jabs.add(
            CoroutineScope(Dispatchers.Default).launch {
                retMap.addSyncAll(getRss(value))
            },
        )
    }
    jabs.forEach {
        it.join()
    }
    return retMap
}

suspend fun getRssScope1() {
    withContext(Dispatchers.Default) {
        while (isActive) {
            val retMap: MutableList<RSS> =
                mutableListOf()
            val jabs: MutableList<Job> = mutableListOf()
            urls.forEach { (_, value) ->
                jabs.add(
                    CoroutineScope(Dispatchers.Default).launch {
                        retMap.addSyncAll(getRss(value))
                    },
                )
            }
            jabs.forEach {
                it.join()
            }
            RSS.mainList = retMap
            delay(10000)
        }
    }
}

fun getRssScope2(): Job {
    return CoroutineScope(Dispatchers.Default).launch {
        while (isActive) {
            val retMap: MutableList<RSS> =
                mutableListOf()
            val jabs: MutableList<Job> = mutableListOf()
            urls.forEach { (_, value) ->
                jabs.add(
                    CoroutineScope(Dispatchers.Default).launch {
                        retMap.addSyncAll(getRss(value))
                    },
                )
            }
            jabs.forEach {
                it.join()
            }
            RSS.mainList = retMap
            delay(600000)
        }
    }
}

fun getRssScope3() {
    var preTime: Long = 0
    CoroutineScope(Dispatchers.Default).launch {
        while (isActive) {
            val currentTime = System.currentTimeMillis()
            if (preTime + updateTime < currentTime) {
                preTime = currentTime
                val retMap: MutableList<RSS> =
                    mutableListOf()
                val jabs: MutableList<Job> = mutableListOf()
                urls.forEach { (_, value) ->
                    jabs.add(
                        CoroutineScope(Dispatchers.Default).launch {
                            retMap.addSyncAll(getRss(value))
                        },
                    )
                }
                jabs.forEach {
                    it.join()
                }
                RSS.mainList = retMap
            }
        }
    }
}

private fun MutableList<RSS>.addSyncAll(rss: MutableList<RSS>) {
    synchronized(this) {
        this.addAll(rss)
    }
}
