import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun server () {
    val socket1 = ServerSocket(PORT_10001)
    val socket2 = ServerSocket(PORT_10002)

    while (true) {
        val client1 = socket1.accept()
        System.err.println("remote connect: ${client1.inetAddress.hostAddress}")
        thread {
            val reader1 = DataInputStream(client1.getInputStream()!!)
            val writer1 = DataOutputStream(client1.getOutputStream()!!)

            // TODO: wait all clients
            writer1.writeInt(Message.START.ordinal)     // send start
            //println("[server] start")

            while (true) {
                // novo evento
                val want = reader1.readLong()            // desired event timestamp
                val evt = reader1.readInt()
                println("[server] 2 want = $want")

                // avisar a todos e aguardar respostas
                val ms1 = Instant.now().toEpochMilli()
                writer1.writeInt(Message.WANTED.ordinal)
                writer1.writeLong(want)                  // send desired timestamp to all
                val rem = reader1.readLong()             // receive local from all
                val ms2 = Instant.now().toEpochMilli()
                println("[server] rtt = ${ms2-ms1}")
                println("[server] rem = $rem")
                val max_ = rem                          // max local from all

                // enviar a todos
                writer1.writeInt(Message.DECIDED.ordinal)
                println("[server] dec = ${max_+RTT_50}")
                Thread.sleep((RTT_50/2 + Random.nextInt(RTT_50)).toLong())
                writer1.writeLong(max_+RTT_50)      // at least MAX, at most MAX+100
                writer1.writeInt(evt)
            }
        }
    }
}