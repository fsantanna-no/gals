import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Long.max
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun client (DT: Long, port: Int = PORT_10000) {
    val lock = java.lang.Object()

    val socket0 = ServerSocket(port)
    val client0 = socket0.accept()
    val writer0 = DataOutputStream(client0.getOutputStream()!!)
    val reader0 = DataInputStream(client0.getInputStream()!!)

    val socket1 = Socket("localhost", PORT_10001)
    val writer1 = DataOutputStream(socket1.getOutputStream()!!)
    val reader1 = DataInputStream(socket1.getInputStream()!!)

    val socket2 = Socket("localhost", PORT_10002)
    val writer2 = DataOutputStream(socket2.getOutputStream()!!)
    val reader2 = DataInputStream(socket2.getInputStream()!!)

    val msg1 = reader1.readInt()
    assert(msg1 == 0)
    if (DEBUG) {
        Thread.sleep(Random.nextLong(100))    // XXX: force delay
    }
    writer1.writeInt(0)
    //println("[client] server connected")

    var LATE = Instant.now().toEpochMilli()
    var NXT  = 0.toLong()

    val queue_expecteds: MutableList<Long> = mutableListOf()
    val queue_finals: MutableList<Pair<Long,Int>> = mutableListOf()

    thread {
        while (true) {
            val evt = reader0.readInt()
            if (DEBUG) {
                Thread.sleep(Random.nextLong(100))    // XXX: force delay
            }
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
            if (DEBUG) {
                Thread.sleep(Random.nextLong(100))    // XXX: force delay
            }
            writer2.writeLong(now)

            val decided = reader2.readLong()
            val evt = reader2.readInt()
            //println("[client] decided=$decided + DT=$DT >= NXT=$NXT")
            assert(decided+DT >= NXT)
            synchronized(lock) {
                queue_finals.add(Pair(decided,evt))
            }
        }
    }

    while (true) {
        val now1 = Instant.now().toEpochMilli()
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
                LATE += now1 - LATE - NXT
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
            val now2 = Instant.now().toEpochMilli()
            val dt = NXT + LATE - now2
            if (dt < 0) { println("NXT=$NXT + LATE=$LATE - now=$now2 = $dt >= 0") }
            //assert(dt >= 0)
            if (dt > 0) {
                Thread.sleep(dt)
            }
        }
    }
}