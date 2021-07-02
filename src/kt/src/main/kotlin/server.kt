import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun server () {
    val socket = ServerSocket(PORT_10000)

    while (true) {
        val client = socket.accept()
        System.err.println("remote connect: ${client.inetAddress.hostAddress}")
        thread {
            val reader = DataInputStream(client.getInputStream()!!)
            val writer = DataOutputStream(client.getOutputStream()!!)

            // TODO: wait all clients
            writer.writeInt(Message.START.ordinal)     // send start
            //println("[server] start")

            while (true) {
                // novo evento
                val want = reader.readLong()            // desired event timestamp
                val evt = reader.readInt()
                println("[server] 2 want = $want")

                // avisar a todos e aguardar respostas
                val ms1 = Instant.now().toEpochMilli()
                writer.writeInt(Message.WANTED.ordinal)
                writer.writeLong(want)                  // send desired timestamp to all
                val rem = reader.readLong()             // receive local from all
                val ms2 = Instant.now().toEpochMilli()
                println("[server] rtt = ${ms2-ms1}")
                println("[server] rem = $rem")
                val max_ = rem                          // max local from all

                // enviar a todos
                writer.writeInt(Message.DECIDED.ordinal)
                println("[server] dec = ${max_+RTT_100}")
                Thread.sleep((RTT_100/2 + Random.nextInt(RTT_100)).toLong())
                writer.writeLong(max_+RTT_100)      // at least MAX, at most MAX+100
                writer.writeInt(evt)
            }
        }
    }
}