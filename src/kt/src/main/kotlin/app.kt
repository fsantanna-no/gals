import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random

fun app (port: Int, fps: Int) {
    // connects with the client on the provided port
    val socket = Socket("localhost", port)
    val writer = DataOutputStream(socket.getOutputStream()!!)
    val reader = DataInputStream(socket.getInputStream()!!)

    // sends the desired FPS and receives client id
    writer.writeInt(fps)
    val self = reader.readInt()
    log("[dapp] $self")

    // thread that receives the logical ticks from the client
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

    // thread that emits random events back to the client
    thread {
        while (true) {
            Thread.sleep(Random.nextLong(5000))
            writer.writeInt(1 + Random.nextInt(10))
        }
    }
}