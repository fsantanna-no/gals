import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random

fun app (port: Int) {
    val socket = Socket("localhost", port)
    val writer = DataOutputStream(socket.getOutputStream()!!)
    val reader = DataInputStream(socket.getInputStream()!!)
    writer.writeInt(0)

    thread {
        while (true) {
            val now = reader.readLong()
            val evt = reader.readInt()
            when (evt) {
                0    -> println("now=$now")
                else -> println("now=$now evt=$evt")
            }

        }
    }
    thread {
        while (true) {
            Thread.sleep(Random.nextLong(5000))
            writer.writeInt(1 + Random.nextInt(10))
        }
    }
}