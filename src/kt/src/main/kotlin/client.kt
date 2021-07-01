import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun client () {
    val socket = Socket("localhost", PORT_10000)
    val writer = DataOutputStream(socket.getOutputStream()!!)
    val reader = DataInputStream(socket.getInputStream()!!)

    val msg = reader.readInt()
    assert(msg == Message.START.ordinal)
    println("[client] started")

    var LATE = Instant.now().toEpochMilli()
    var NOW  = 0.toLong()
    val queue_nxts: MutableList<Long> = mutableListOf()
    val queue_evts: MutableList<Pair<Long,Int>> = mutableListOf()

    fun app_output (evt: Int) {
        writer.writeLong(NOW)
        writer.writeInt(evt)
    }

    var nxt1: Long = 0
    var nxt2: Long = NOW + Random.nextLong(10000)
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
                    assert(now > NOW)
                    synchronized(socket) {
                        //println("[back] now=$now evt=$evt ${now > Instant.now().toEpochMilli() - late}")
                        queue_evts.add(Pair(now,evt))
                    }
                }
                Message.QUERY.ordinal -> {
                    val now = NOW
                    //Thread.sleep(5)
                    synchronized(socket) {
                        queue_nxts.add(now+RTT_100)
                    }
                    writer.writeLong(now)
                }
                else -> error("impossible case")
            }
        }
    }

    while (true) {
        synchronized(socket) {
            while (queue_evts.isNotEmpty() && NOW>=queue_evts[0].first) {
                val (now,evt) = queue_evts.removeAt(0)
                queue_nxts.removeAt(0)
                app_input(now, evt)
            }
            if (!(queue_nxts.isEmpty() || NOW<queue_nxts.get(0))) {
                println("now=$NOW vs nxt=${queue_nxts.get(0)}")
            }
            assert(queue_nxts.isEmpty() || NOW<queue_nxts.get(0))
            app_input(NOW, null)
            NOW = Instant.now().toEpochMilli() - LATE
        }
        Thread.sleep(1)
    }
}