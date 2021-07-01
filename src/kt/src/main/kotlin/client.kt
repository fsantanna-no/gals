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

    val msg = reader.readInt()
    assert(msg == Message.START.ordinal)
    println("[client] started")

    val late = Instant.now().toEpochMilli()
    var now  = 0.toLong()
    val queue: MutableList<Pair<Long,Int>> = mutableListOf()

    fun app_output (evt: Int) {
        writer.writeLong(now)
        writer.writeInt(evt)
    }

    var nxt1: Long = 0
    var nxt2: Long = now + Random.nextLong(10000)
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
                app_output(Random.nextInt(10))
            }
        }
    }

    thread {
        while (true) {
            val msg = reader.readInt()
            when (msg) {
                Message.EMIT.ordinal -> {
                    val now = reader.readLong()
                    val evt = reader.readInt()
                    synchronized(socket) {
                        //println("[back] now=$now evt=$evt ${now > Instant.now().toEpochMilli() - late}")
                        assert(now > Instant.now().toEpochMilli() - late)
                        queue.add(Pair(now,evt))
                    }
                }
                Message.QUERY.ordinal -> {
                    //writer.writeLong()
                }
                else -> error("impossible case")
            }
        }
    }

    while (true) {
        app_input(now, null)
        Thread.sleep(50)
        now = Instant.now().toEpochMilli() - late
        synchronized(socket) {
            while (queue.isNotEmpty() && now>=queue[0].first) {
                val (now_,evt_) = queue.removeAt(0)
                app_input(now_, evt_)
            }
            app_input(now, null)
        }
    }
}