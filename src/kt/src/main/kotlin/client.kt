import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Long.max
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

val MAX_DT_10 = 10    // maximum DT step in NOW

fun client () {
    val socket1 = Socket("localhost", PORT_10001)
    val writer1 = DataOutputStream(socket1.getOutputStream()!!)
    val reader1 = DataInputStream(socket1.getInputStream()!!)

    val msg = reader1.readInt()
    assert(msg == Message.START.ordinal)
    println("[client] started")

    var LATE = Instant.now().toEpochMilli()
    var NOW  = 0.toLong()
    val queue_expecteds: MutableList<Long> = mutableListOf()
    val queue_decideds: MutableList<Pair<Long,Int>> = mutableListOf()

    fun app_output (evt: Int) {
        println("[client] 1 wants $NOW")
        writer1.writeLong(NOW)
        writer1.writeInt(evt)
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
                nxt2 = now + 100 + Random.nextLong(5000)   // TODO: remove +1000
                //println("[app] emit")
                app_output(Random.nextInt(10))
                app_output(Random.nextInt(10))
            }
        }
    }

    thread {
        while (true) {
            val msg = reader1.readInt()
            when (msg) {
                Message.WANTED.ordinal -> {
                    val wanted = reader1.readLong()    // original time
                    val now = NOW
                    println("[client] 3 wanted $wanted, now=$now")
                    //Thread.sleep(5)
                    synchronized(socket1) {
                        queue_expecteds.add(max(now,wanted)+RTT_50)   // possible time + rtt
                    }
                    writer1.writeLong(now)
                }
                Message.DECIDED.ordinal -> {
                    val decided = reader1.readLong()
                    val evt = reader1.readInt()
                    assert(decided >= NOW)
                    println("[client] decided=$decided now=$NOW")
                    synchronized(socket1) {
                        queue_decideds.add(Pair(decided,evt))
                    }
                }
                else -> error("impossible case")
            }
        }
    }

    while (true) {
        synchronized(socket1) {
            while (queue_decideds.isNotEmpty() && NOW>=queue_decideds[0].first) {
                val (now,evt) = queue_decideds.removeAt(0)
                queue_expecteds.removeAt(0)
                app_input(now, evt)
            }
            /*
            if (!(queue_expecteds.isEmpty() || NOW<queue_expecteds.get(0))) {
            }
            assert(queue_expecteds.isEmpty() || NOW<queue_expecteds.get(0))
             */
            if (queue_expecteds.isNotEmpty() && queue_decideds.isEmpty() && NOW>=queue_expecteds.get(0)-MAX_DT_10) {
                LATE += (Instant.now().toEpochMilli() - LATE) - NOW
                println("[client] XXX now=$NOW vs nxt=${queue_expecteds.get(0)}")
                //error("oi")
            } else {
                app_input(NOW, null)
                NOW = Instant.now().toEpochMilli() - LATE
            }
        }
        Thread.sleep(1)
    }
}