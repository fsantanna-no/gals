import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
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
            writer.writeInt(0)     // send start
            //println("[server] start")

            while (true) {
                val now = reader.readLong()
                val evt = reader.readInt()
                //println("[server] now=$now evt=$evt")
                writer.writeLong(now+100)
                writer.writeInt(evt)
            }
        }
    }
}