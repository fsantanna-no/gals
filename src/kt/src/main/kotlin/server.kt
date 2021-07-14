import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.random.Random

fun server (N: Int) { // number of app clients
    val socket1 = ServerSocket(PORT_10001)
    val socket2 = ServerSocket(PORT_10002)  // TODO: same process as above?

    val clients1: MutableList<Pair<DataInputStream, DataOutputStream>> = mutableListOf()
    val clients2: MutableList<Pair<DataInputStream, DataOutputStream>> = mutableListOf()

    // connect to all clients
    for (i in 1..N) {
        val client1 = socket1.accept()      // handles start and new events
        val client2 = socket2.accept()      // handles synchronization
        assert(client1.inetAddress == client2.inetAddress)

        val reader1 = DataInputStream(client1.getInputStream()!!)
        val writer1 = DataOutputStream(client1.getOutputStream()!!)
        clients1.add(Pair(reader1, writer1))

        //val id2 = client2.inetAddress.hostAddress + ":" + client2.port
        val reader2 = DataInputStream(client2.getInputStream()!!)
        val writer2 = DataOutputStream(client2.getOutputStream()!!)
        clients2.add(Pair(reader2, writer2))
    }

    val lock = java.lang.Object()
    val queue: MutableList<Pair<Long, Int>> = mutableListOf()
    var RTT = 1L  // max RTT from previous cycle considering all clients

    // sends START and gets initial RTT from all clients
    clients1.map {
        thread {
            val (reader1, writer1) = it
            val ms1 = Instant.now().toEpochMilli()
            if (DEBUG) {
                Thread.sleep(Random.nextLong(100))    // XXX: force delay
            }
            writer1.writeInt(0)     // send start
            val v = reader1.readInt()
            assert(v == 0)
            val ms2 = Instant.now().toEpochMilli()
            RTT = max(RTT, ms2 - ms1)
        }
    }.map { it.join() }

    // handles new events
    for ((reader1,_) in clients1) {
        thread {
            while (true) {
                val now = reader1.readLong()  // desired event timestamp
                val evt = reader1.readInt()
                synchronized(lock) {
                    queue.add(Pair(now, evt))
                    lock.notify()
                }
                //println("[server] 2 want = $now")
            }
        }
    }

    // synchronizes emits
    while (true) {
        // waits for an event
        val want = synchronized(lock) {
            if (queue.isEmpty()) {
                lock.wait()
            }
            queue.removeAt(0)
        }

        var TIME = 0L   // final time with delta

        // WANTED round trip to all clients
        var RTT_nxt = 1L  // max RTT to become next "previous cycle" considering all clients
        val ths = Array<Thread?>(clients2.size){null}
        val tms = Array<Long>(clients2.size){0}
        for (i in 0..clients2.size-1) {
            ths[i] = thread {
                val (reader2,writer2) = clients2[i]
                val ms1 = Instant.now().toEpochMilli()
                if (DEBUG) {
                    Thread.sleep(Random.nextLong(100))    // XXX: force delay
                }
                writer2.writeLong(want.first)           // sends desired timestamp to all
                writer2.writeLong(RTT)                  // sends previous RTT to freeze
                val time = reader2.readLong()           // receives local time from all
                val ms2 = Instant.now().toEpochMilli()
                synchronized(clients2) {
                    val rtt = ms2 - ms1
                    tms[i] = time+rtt/2
                    RTT_nxt = max(RTT_nxt, rtt)
                    TIME = max(TIME, time+RTT)
                }
            }
        }
        ths.forEach { it!!.join() }

        var delay = true
        val maxLocal = tms.maxOrNull()!!
        (0..clients2.size-1).map {
            thread {
                val (_, writer2) = clients2[it]
                if (DEBUG && delay) {
                    delay = false
                    Thread.sleep(RTT / 2 + Random.nextLong(RTT))    // XXX: force delay
                }
                writer2.writeLong(TIME)      // at least MAX, at most MAX+100
                writer2.writeInt(want.second)
                writer2.writeInt((maxLocal - tms[it]).toInt())  // clock drift
            }
        }.map { it.join() }

        RTT = RTT_nxt
    }
}
