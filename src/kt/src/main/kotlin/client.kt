import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Long.max
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.random.Random

fun client (port: Int = PORT_10000) {
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

    val fps = reader0.readInt()
    assert(1000%fps == 0)
    val DT = 1000 / fps

    val msg1 = reader1.readInt()
    assert(msg1 == 0)
    if (DEBUG) {
        Thread.sleep(Random.nextLong(100))    // XXX: force delay
    }
    writer1.writeInt(0)
    //println("[client] server connected")

    var app_nxt = 0.toLong()

    val queue_expecteds: MutableList<Long> = mutableListOf()
    val queue_finals: MutableList<Pair<Long,Int>> = mutableListOf()

    thread {
        while (true) {
            val evt = reader0.readInt()
            if (DEBUG) {
                Thread.sleep(Random.nextLong(100))    // XXX: force delay
            }
            writer1.writeLong(app_nxt)
            writer1.writeInt(evt)
        }
    }

    var DRIFT = 0
    thread {
        while (true) {
            val wanted = reader2.readLong()     // original time
            val rtt = reader2.readLong()        // previous rtt to consider in freeze
            val app_cur = app_nxt
            synchronized(lock) {
                DRIFT = 0
                val t1 = max(app_cur,wanted) + 2*rtt
                val t2 = if (queue_expecteds.isEmpty()) t1 else max(t1, queue_expecteds.maxOrNull()!!)
                queue_expecteds.add(t2)   // possible time + 2*rtt
            }
            if (DEBUG) {
                Thread.sleep(Random.nextLong(100))    // XXX: force delay
            }
            writer2.writeLong(app_cur)          // returns current time for final value and drift compensation

            val decided = reader2.readLong()
            val evt = reader2.readInt()
            val drift = reader2.readInt()
            if (decided+DT < app_nxt) { println("[client] decided=$decided + DT=$DT >= NXT=$app_nxt") }
            assert(decided+DT >= app_nxt)
            synchronized(lock) {
                DRIFT = drift
                queue_finals.add(Pair(decided,evt))
            }
        }
    }

    while (true) {
        val cli_now = Instant.now().toEpochMilli()
        val app_cur = app_nxt
        var evt = 0

        synchronized(lock) {
            app_nxt += DT
            if (queue_finals.isEmpty()) {
                if (queue_expecteds.isNotEmpty() && app_nxt>=queue_expecteds[0]) {
                    // cannot advance time to prevent missing expected event
                    println("[WRN] freeze")
                    app_nxt -= DT
                }
            } else if (app_cur>=queue_finals[0].first) {
                val (now_,evt_) = queue_finals.removeAt(0)
                queue_expecteds.removeAt(0)
                evt = evt_
            }
        }

        writer0.writeLong(app_cur)
        writer0.writeInt(evt)

        val cli_nxt = Instant.now().toEpochMilli()
        val dt = cli_now + DT - cli_nxt
        if (dt <= 0) { println("[WRN] now=$cli_now + DT=$DT - nxt=$cli_nxt = $dt > 0") }
        assert(dt > 0)
        val x = min(DRIFT,DT/5)  // if drift is over a full frame, recover 20% each frame
        //if (DRIFT > 0) { println("DRIFT=$DRIFT") }
        val drift = if (dt.toInt()==0 || DRIFT==0) 0 else { DRIFT-=x ; x }
        /*
        if (port%2 == 0) {
            Thread.sleep(((dt - drift)*0.8).toLong())
        } else {
         */
        Thread.sleep(dt - drift)
        //}
    }
}