import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

fun client (server: String, port: Int = PORT_10000) {
    log("[client] started")
    val lock = java.lang.Object()

    val socket0 = ServerSocket(port)
    val client0 = socket0.accept()
    val writer0 = DataOutputStream(client0.getOutputStream()!!)
    val reader0 = DataInputStream(client0.getInputStream()!!)
    log("[client] connected with local dapp")

    val fps = reader0.readInt()
    assert(1000%fps == 0)
    val DT = 1000 / fps
    log("[client] fps = $fps")

    val socket1 = Socket(server, PORT_10001)
    val writer1 = DataOutputStream(socket1.getOutputStream()!!)
    val reader1 = DataInputStream(socket1.getInputStream()!!)
    val socket2 = Socket(server, PORT_10002)
    val writer2 = DataOutputStream(socket2.getOutputStream()!!)
    val reader2 = DataInputStream(socket2.getInputStream()!!)
    log("[client] connected with server")

    val self = reader1.readInt()
    val N    = reader1.readInt()
    if (DEBUG) {
        Thread.sleep(Random.nextLong(100))    // XXX: force delay
    }
    log("[client] self = $self")

    val started = reader1.readInt()
    assert(started == 1)
    writer1.writeInt(DT)     // sends started ACK
    writer0.writeInt(self)   // starts local dapp
    log("[client] started")

    var app_nxt = 0.toLong()

    val queue_expecteds: MutableList<Long> = mutableListOf()
    val queue_finals: MutableList<Triple<Long,Int,Int>> = mutableListOf()

    // first client generates null event every 5s
    if (self == 1) {
        thread {
            while (true) {
                Thread.sleep(5000)
                //Thread.sleep(1000) // uncomment to recover drift
                synchronized(lock) {
                    writer1.writeLong(app_nxt)
                    writer1.writeInt(0)
                    writer1.writeInt(111)       // payload 1
                    writer1.writeInt(222)       // payload 2
                }
            }
        }
    }

    // receives async from local dapp and forwards to server
    ///val evt_fr = mutableListOf<Long>()
    thread {
        while (true) {
            val evt = reader0.readInt()
            val pay = reader0.readInt()
            val nxt = app_nxt
            ///evt_fr.add(nxt)
            if (DEBUG) {
                Thread.sleep(Random.nextLong(100))    // XXX: force delay
            }
            synchronized(lock) {
                writer1.writeLong(nxt+DT)
                writer1.writeInt(evt)
                writer1.writeInt(pay)       // payload 1
                writer1.writeInt(222)       // payload 2
            }
        }
    }

    var DRIFT = 0
    thread {
        while (true) {
            val wanted = reader2.readLong()     // original time
            val rtt = reader2.readLong()        // previous rtt to consider in freeze
            if (DEBUG) {
                Thread.sleep(Random.nextLong(100))    // XXX: force delay
            }
            synchronized(lock) {
                DRIFT = 0
                writer2.writeLong(app_nxt+DT)  // returns current time for final value and drift compensation
                // +DT because maybe I'm about to turn to it
                val t1 = max(app_nxt+DT,wanted) + 2*rtt + max(5,N/5)
                // maybe a previous event will already expire after this one
                val t2 = if (queue_expecteds.isEmpty()) t1 else max(t1, queue_expecteds.maxOrNull()!!)
                queue_expecteds.add(t2)     // possible time + 2*rtt
            }

            val decided = reader2.readLong()
            val evt = reader2.readInt()
            val pay = reader2.readInt()     // payload 1
            val yyy = reader2.readInt()     // payload 2
            //assert(xxx==111 && yyy==222)
            val drift = reader2.readInt()
            synchronized(lock) {
                //if (decided+DT < app_nxt) { log("[client] decided=$decided + DT=$DT >= NXT=$app_nxt") }
                //assert(decided+DT >= app_nxt)
                if (drift>DRIFT) { log("drift [$self] $drift") }
                DRIFT = drift
                queue_finals.add(Triple(decided,evt,pay))
            }
        }
    }

    var cli_nxt = Instant.now().toEpochMilli()
    var old = 0
    while (true) {
        var app_cur: Long? = null
        var evt = 0
        var pay = 0

        synchronized(lock) {
            app_cur = app_nxt
            app_nxt += DT
            if (queue_finals.isEmpty()) {
                if (queue_expecteds.isNotEmpty() && app_nxt>=queue_expecteds[0]) {
                    // cannot advance time to prevent missing expected event
                    //println("[WRN] freeze")
                    app_nxt -= DT
                    app_cur = app_cur!! - DT
                    evt = old
                }
            } else if (app_cur!!>=queue_finals[0].first) {
                val (now_,evt_,pay_) = queue_finals.removeAt(0)
                queue_expecteds.removeAt(0)
                evt = evt_
                pay = pay_
                /*
                if (evt == self) {
                    val v = evt_fr.removeAt(0)
                    assert((app_cur!!-v).toInt() % DT == 0)
                    //println(">>> $app_cur - $v")
                    log("event [$self] ${(app_cur!!-v)/DT}")
                }
                 */
            }
        }

        writer0.writeLong(app_cur!!)
        writer0.writeInt(evt)
        writer0.writeInt(pay)
        old = evt

        // uncomment to force large drift (see also emit above and DT/2 below)
        //if (self == 1) {
        //    cli_nxt += 8*DT/10
        //} else {
            cli_nxt += DT
        //}
        val dt = cli_nxt - Instant.now().toEpochMilli()
        //assert(dt > 0)  // falha quando testo no shell c/ processos em vez de threads
        if (dt <= 0) { continue }

        // if drift is over a full frame, recover 20% each frame
        val x = min((dt-1).toInt(), min(DRIFT, DT / 5))
        //val x = min((dt-1).toInt(), min(DRIFT, DT / 2)) // uncomment recover drift

        val drift = if (dt.toInt() == 0 || DRIFT == 0) 0 else {
            DRIFT -= x; x
        }
        cli_nxt -= drift
        Thread.sleep(dt - drift)
    }
}