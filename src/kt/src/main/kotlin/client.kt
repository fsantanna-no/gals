import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random

fun client () {
    val socket = Socket("localhost", PORT_10000)
    val writer = DataOutputStream(socket.getOutputStream()!!)
    val reader = DataInputStream(socket.getInputStream()!!)

    val queue: MutableList<Pair<Long,Int>> = mutableListOf()

    fun app_output (now: Long, evt: Int) {
        writer.writeLong(now)
        writer.writeInt(evt)
    }

    var nxt1: Long = 0
    var nxt2: Long = 0
    fun app_input (now: Long, evt: Int?) {
        when {
            (evt != null) -> println("[app] now=${now/1000} evt=$evt")
            (now >= nxt1) -> {
                println("[app] now=${now/1000} evt=$evt")
                nxt1 += 1000
            }
            (now > nxt2) -> {
                nxt2 = now + Random.nextLong(10000)
                //println("[app] emit")
                app_output(now, Random.nextInt(10))
            }
        }
    }

    val msg = reader.readInt()
    assert(msg == Message.START.ordinal)
    val late = Instant.now().toEpochMilli()
    println("[client] started")

    thread {
        while (true) {
            val msg = reader.readInt()
            assert(msg == Message.EMIT.ordinal)
            val now = reader.readLong()
            val evt = reader.readInt()
            synchronized(socket) {
                //println("[back] now=$now evt=$evt ${now > Instant.now().toEpochMilli() - late}")
                assert(now > Instant.now().toEpochMilli() - late)
                queue.add(Pair(now,evt))
            }
        }
    }

    app_input(0, null)
    while (true) {
        Thread.sleep(50)
        synchronized(socket) {
            val now = Instant.now().toEpochMilli() - late
            while (queue.isNotEmpty() && now>=queue[0].first) {
                val (now_,evt_) = queue.removeAt(0)
                app_input(now_, evt_)
            }
            app_input(now, null)
        }
    }
}