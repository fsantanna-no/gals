import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun server () {
    val lock = java.lang.Object()
    val socket1 = ServerSocket(PORT_10011)
    val socket2 = ServerSocket(PORT_10012)

    while (true) {
        val client1 = socket1.accept()
        val client2 = socket2.accept()
        val queue: MutableList<Pair<Long,Int>> = mutableListOf()

        // new events
        thread {
            val reader1 = DataInputStream(client1.getInputStream()!!)
            val writer1 = DataOutputStream(client1.getOutputStream()!!)

            // TODO: wait all clients
            writer1.writeInt(Message.START.ordinal)     // send start
            //println("[server] start")

            while (true) {
                val now = reader1.readLong()            // desired event timestamp
                val evt = reader1.readInt()
                synchronized(lock) {
                    queue.add(Pair(now,evt))
                    lock.notify()
                }
                //println("[server] 2 want = $now")
            }
        }

        // emits
        thread {
            val reader2 = DataInputStream(client2.getInputStream()!!)
            val writer2 = DataOutputStream(client2.getOutputStream()!!)

            while (true) {
                val want = synchronized(lock) {
                    if (queue.isEmpty()) {
                        lock.wait()
                    }
                    queue.removeAt(0)
                }
                // avisar a todos e aguardar respostas
                val ms1 = Instant.now().toEpochMilli()
                writer2.writeInt(Message.WANTED.ordinal)
                writer2.writeLong(want.first)           // send desired timestamp to all
                val rem = reader2.readLong()            // receive local from all
                val ms2 = Instant.now().toEpochMilli()
                //println("[server] rtt = ${ms2 - ms1}")
                //println("[server] rem = $rem")
                val max_ = rem                          // max local from all

                // enviar a todos
                writer2.writeInt(Message.DECIDED.ordinal)
                //println("[server] dec = ${max_ + RTT_50}")
                Thread.sleep((RTT_50 / 2 + Random.nextInt(RTT_50)).toLong())
                writer2.writeLong(max_ + RTT_50)      // at least MAX, at most MAX+100
                writer2.writeInt(want.second)
            }
        }
    }
}