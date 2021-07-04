import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
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
                0    -> println("[app.$port] now=$now")
                else -> println("[app.$port] now=$now evt=$evt")
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

@TestMethodOrder(Alphanumeric::class)
class Tests {
    @Test
    fun test_01 () {
        thread { server(1) }
        Thread.sleep(1000)
        thread { client(PORT_10000, 100) }
        Thread.sleep(1000)

        val socket = Socket("localhost", PORT_10000)
        val writer = DataOutputStream(socket.getOutputStream()!!)
        val reader = DataInputStream(socket.getInputStream()!!)
        writer.writeInt(0)

        thread {
            while (true) {
                val now = reader.readLong()
                val evt = reader.readInt()
                when (evt) {
                    0    -> println("[app] now=$now")
                    else -> println("[app] now=$now evt=$evt")
                }

            }
        }
        thread {
            while (true) {
                Thread.sleep(Random.nextLong(5000))
                writer.writeInt(1 + Random.nextInt(10))
            }
        }
        Thread.sleep(100000)
    }

    @Test
    fun test_02 () {
        thread { server(2) }
        Thread.sleep(1000)
        thread { client(PORT_10000-0, 100) }
        thread { client(PORT_10000-1, 100) }
        Thread.sleep(1000)
        thread { app(PORT_10000-0) }
        thread { app(PORT_10000-1) }
        Thread.sleep(100000)
    }
}
