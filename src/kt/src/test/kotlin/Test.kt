import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random

@TestMethodOrder(Alphanumeric::class)
class Tests {
    @Test  // see now=... and sporadic evt=...
    fun test_01 () {
        thread { server(1) }
        Thread.sleep(1000)
        thread { client(PORT_10000) }
        Thread.sleep(1000)

        val socket = Socket("localhost", PORT_10000)
        val writer = DataOutputStream(socket.getOutputStream()!!)
        val reader = DataInputStream(socket.getInputStream()!!)
        writer.writeInt(10)
        reader.readInt()

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

    @Test  // see now=... and sporadic evt=... (but twice and synchronized)
    fun test_02 () {
        thread { server(2) }
        Thread.sleep(1000)
        thread { client(PORT_10000-0) }
        thread { client(PORT_10000-1) }
        Thread.sleep(1000)
        thread { app(PORT_10000-0, 1) }
        thread { app(PORT_10000-1, 1) }
        Thread.sleep(100000)
    }

    @Test  // see now=... and sporadic evt=... (but twice and synchronized)
    fun test_03 () {
        thread { server(2) }
        Thread.sleep(1000)
        for (i in 0..9) {
            thread { client(PORT_10000 - i) }
        }
        Thread.sleep(1000)
        for (i in 0..9) {
            thread { app(PORT_10000 - i, 20) }
        }
        Thread.sleep(10000000)
    }

    @Test  // see now=... and sporadic evt=... (but twice and synchronized)
    fun eval_01 () {
        val N = 100
        val FPS = 50
        val EVT_PER_MIN = 1

        thread { server(N) }
        Thread.sleep(1000)
        for (i in 0..N-1) {
            thread { client(PORT_10000 - i) }
        }
        Thread.sleep(1000)
        for (i in 0..N-1) {
            thread { eval(PORT_10000 - i, FPS, EVT_PER_MIN) }
        }
        Thread.sleep(100000)
    }
}
