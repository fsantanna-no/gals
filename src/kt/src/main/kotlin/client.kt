import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Long.max
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun client (DT: Long) {
    val lock = java.lang.Object()

    val socket0 = ServerSocket(PORT_10000)
    val client0 = socket0.accept()
    val writer0 = DataOutputStream(client0.getOutputStream()!!)
    val reader0 = DataInputStream(client0.getInputStream()!!)

    val msg0 = reader0.readInt()
    assert(msg0 == 0)
    println("[client] app connected")

    val socket1 = Socket("localhost", PORT_10001)
    val writer1 = DataOutputStream(socket1.getOutputStream()!!)
    val reader1 = DataInputStream(socket1.getInputStream()!!)

    val socket2 = Socket("localhost", PORT_10002)
    val writer2 = DataOutputStream(socket2.getOutputStream()!!)
    val reader2 = DataInputStream(socket2.getInputStream()!!)

    val msg1 = reader1.readInt()
    assert(msg1 == 0)
    writer1.writeInt(0)
    println("[client] server connected")

    var LATE = Instant.now().toEpochMilli()
    var NXT  = 0.toLong()

    val queue_expecteds: MutableList<Long> = mutableListOf()
    val queue_finals: MutableList<Pair<Long,Int>> = mutableListOf()

    fun NOW (): Long {
        return Instant.now().toEpochMilli() - LATE
    }

    thread {
        while (true) {
            val evt = reader0.readInt()
            writer1.writeLong(NXT)
            writer1.writeInt(evt)
        }
    }

    thread {
        while (true) {
            val wanted = reader2.readLong()    // original time
            val rtt = reader2.readLong()
            val now = NXT
            synchronized(lock) {
                queue_expecteds.add(max(now,wanted)+rtt)   // possible time + rtt
            }
            //Thread.sleep(Random.nextLong(100))    // XXX: force delay
            writer2.writeLong(now)

            val decided = reader2.readLong()
            val evt = reader2.readInt()
            assert(decided >= NXT)
            //println("[client] decided=$decided now=$NOW")
            synchronized(lock) {
                queue_finals.add(Pair(decided,evt))
            }
        }
    }

    while (true) {
        val ok = synchronized(lock) {
            while (queue_finals.isNotEmpty() && NXT>=queue_finals[0].first) {
                val (now,evt) = queue_finals.removeAt(0)
                queue_expecteds.removeAt(0)
                writer0.writeLong(NXT)
                writer0.writeInt(evt)
            }
            /*
            if (!(queue_expecteds.isEmpty() || NOW<queue_expecteds.get(0))) {
            }
            assert(queue_expecteds.isEmpty() || NOW<queue_expecteds.get(0))
             */
            if (queue_expecteds.isNotEmpty() && queue_finals.isEmpty()) {
                LATE += NOW() - NXT
                false
                //println("[client] XXX now=$NOW vs nxt=${queue_expecteds.get(0)}")
                //println("oi")
            } else {
                writer0.writeLong(NXT)
                writer0.writeInt(0)
                NXT += DT
                true
            }
        }
        if (ok) {
            val dt = NXT + LATE - Instant.now().toEpochMilli()
            assert(dt >= 0)
            Thread.sleep(dt)
        }
    }
}