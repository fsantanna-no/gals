import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.random.Random

val N = 1   // number of app clients

fun server () {
    val lock = java.lang.Object()
    val socket1 = ServerSocket(PORT_10001)
    val socket2 = ServerSocket(PORT_10002)  // TODO: same process as above?

    val clients2: MutableList<Pair<DataInputStream, DataOutputStream>> = mutableListOf()
    val queue: MutableList<Pair<Long, Int>> = mutableListOf()

    for (i in 1..N) {
        val client1 = socket1.accept()      // handles start and new events
        val client2 = socket2.accept()      // handles synchronization
        assert(client1.inetAddress == client2.inetAddress)

        val reader1 = DataInputStream(client1.getInputStream()!!)
        val writer1 = DataOutputStream(client1.getOutputStream()!!)

        //val id2 = client2.inetAddress.hostAddress + ":" + client2.port
        val reader2 = DataInputStream(client2.getInputStream()!!)
        val writer2 = DataOutputStream(client2.getOutputStream()!!)
        clients2.add(Pair(reader2, writer2))

        // sends START to all clients after confirming that all have connected (see at the end)
        thread {
            synchronized(lock) {
                lock.wait()
                writer1.writeInt(Message.START.ordinal)     // send start
            }
        }

        // handles new events
        thread {
            while (true) {
                val now = reader1.readLong()            // desired event timestamp
                val evt = reader1.readInt()
                synchronized(lock) {
                    queue.add(Pair(now, evt))
                    lock.notify()
                }
                //println("[server] 2 want = $now")
            }
        }
    }

    // confirms that all clients have connected
    synchronized(lock) {
        lock.notifyAll()
    }

    // emits
    // avisar a todos e aguardar respostas
    while (true) {
        val want = synchronized(lock) {
            if (queue.isEmpty()) {
                lock.wait()
            }
            queue.removeAt(0)
        }

        var RTT  = 0L
        var TIME = 0L
        clients2.map {
            thread {
                val (reader2,writer2) = it
                val ms1 = Instant.now().toEpochMilli()
                writer2.writeInt(Message.WANTED.ordinal)
                writer2.writeLong(want.first)           // send desired timestamp to all
                val time = reader2.readLong()            // receive local from all
                val ms2 = Instant.now().toEpochMilli()
                synchronized(clients2) {
                    RTT = max(RTT, ms2 - ms1)
                    TIME = max(TIME, time)
                }
            }
        }.map { it.join() }

        // enviar a todos
        for (client2 in clients2) {
            val (_,writer2) = client2
            writer2.writeInt(Message.DECIDED.ordinal)
            //println("[server] dec = ${max_ + RTT_50}")
            Thread.sleep((RTT_50 / 2 + Random.nextInt(RTT_50)).toLong())
            writer2.writeLong(TIME + RTT_50)      // at least MAX, at most MAX+100
            writer2.writeInt(want.second)
        }
    }
}
