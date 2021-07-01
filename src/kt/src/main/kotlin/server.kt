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
                val now = reader.readLong()
                val evt = reader.readInt()
                //println("[server] now=$now evt=$evt")

                // avisar a todos e aguardar respostas
                val ms1 = Instant.now().toEpochMilli()
                writer.writeInt(Message.QUERY.ordinal)
                val rem = reader.readLong()
                val ms2 = Instant.now().toEpochMilli()
                println("rtt = ${ms2-ms1}")

                // enviar a todos
                writer.writeInt(Message.EMIT.ordinal)
                writer.writeLong(rem+RTT_100)
                writer.writeInt(evt)
            }
        }
    }
}