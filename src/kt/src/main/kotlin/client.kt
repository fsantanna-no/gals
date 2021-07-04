import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Long.max
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

val MAX_DT_10 = 10    // maximum DT step in NOW

fun client (DT: Long) {
    val lock = java.lang.Object()

    val socket1 = Socket("localhost", PORT_10001)
    val writer1 = DataOutputStream(socket1.getOutputStream()!!)
    val reader1 = DataInputStream(socket1.getInputStream()!!)

    val socket2 = Socket("localhost", PORT_10002)
    val writer2 = DataOutputStream(socket2.getOutputStream()!!)
    val reader2 = DataInputStream(socket2.getInputStream()!!)

    val msg = reader1.readInt()
    assert(msg == 0)
    writer1.writeInt(0)
    println("[client] started")

    var LATE = Instant.now().toEpochMilli()
    var NXT  = 0.toLong()

    val queue_expecteds: MutableList<Long> = mutableListOf()
    val queue_finals: MutableList<Pair<Long,Int>> = mutableListOf()

    fun NOW (): Long {
        return Instant.now().toEpochMilli() - LATE
    }

    fun app_output (evt: Int) {
        //println("[client] 1 wants $NOW")
        writer1.writeLong(NXT)
        writer1.writeInt(evt)
    }

    var nxt1: Long = 0
    var nxt2: Long = NXT + Random.nextLong(10000)
    fun app_input (now: Long, evt: Int?) {
        when {
            (evt != null) -> println("[app] now=${now/1000} evt=$evt")
            (now >= nxt1) -> {
                println("[app] now=${now/1000} evt=$evt")
                nxt1 += 1000
            }
            (now > nxt2) -> {
                nxt2 = now + 100 + Random.nextLong(5000)   // TODO: remove +1000
                //println("[app] emit")
                app_output(Random.nextInt(10))
                //app_output(Random.nextInt(10))
            }
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
        synchronized(lock) {
            while (queue_finals.isNotEmpty() && NXT>=queue_finals[0].first) {
                val (now,evt) = queue_finals.removeAt(0)
                queue_expecteds.removeAt(0)
                app_input(now, evt)
            }
            /*
            if (!(queue_expecteds.isEmpty() || NOW<queue_expecteds.get(0))) {
            }
            assert(queue_expecteds.isEmpty() || NOW<queue_expecteds.get(0))
             */
            if (queue_expecteds.isNotEmpty() && queue_finals.isEmpty() && NXT>=queue_expecteds.get(0)-MAX_DT_10) {
                LATE += NOW() - NXT
                //println("[client] XXX now=$NOW vs nxt=${queue_expecteds.get(0)}")
                //error("oi")
            } else {
                app_input(NXT, null)
                NXT += DT
            }
        }
        val dt = NXT + LATE - Instant.now().toEpochMilli()
        assert(dt >= 0)
        Thread.sleep(dt)
    }
}