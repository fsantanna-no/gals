import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.time.Instant
import kotlin.concurrent.thread

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
                //println("[server] now=$now evt=$evt")

                // avisar a todos e aguardar respostas
                val ms1 = Instant.now().toEpochMilli()
                writer.writeInt(Message.WANTED.ordinal)
                writer.writeLong(want)                  // send desired timestamp to all
                val rem = reader.readLong()             // receive local from all
                val ms2 = Instant.now().toEpochMilli()
                println("rtt = ${ms2-ms1}")
                val max_ = rem                          // max local from all

                // enviar a todos
                writer.writeInt(Message.DECIDED.ordinal)
                writer.writeLong(max_+RTT_100)      // at least MAX, at most MAX+100
                writer.writeInt(evt)
            }
        }
    }
}