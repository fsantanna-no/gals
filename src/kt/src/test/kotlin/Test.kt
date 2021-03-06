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
        thread { client("localhost", PORT_10000) }
        Thread.sleep(1000)

        val socket = Socket("localhost", PORT_10000)
        val writer = DataOutputStream(socket.getOutputStream()!!)
        val reader = DataInputStream(socket.getInputStream()!!)
        writer.writeInt(10)     // FPS
        reader.readInt()            // started

        thread {
            while (true) {
                val now = reader.readLong()
                val evt = reader.readInt()
                val pay1 = reader.readInt()
                val pay2 = reader.readInt()
                val pay3 = reader.readInt()
                when (evt) {
                    0    -> println("[app] now=$now")
                    else -> println("[app] now=$now evt=$evt pay=($pay1,$pay2,$pay3)")
                }
            }
        }
        thread {
            var pay = 1
            while (true) {
                Thread.sleep(Random.nextLong(3000))
                writer.writeInt(1 + Random.nextInt(10))
                writer.writeInt(pay)
                writer.writeInt(pay+10)
                writer.writeInt(pay+100)
                pay++
            }
        }
        Thread.sleep(100000)
    }

    @Test  // see now=... and sporadic evt=... (but twice and synchronized)
    fun test_02 () {
        thread { server(2) }
        Thread.sleep(1000)
        thread { client("localhost", PORT_10000-0) }
        thread { client("localhost", PORT_10000-1) }
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
            thread { client("localhost", PORT_10000 - i) }
        }
        Thread.sleep(1000)
        for (i in 0..9) {
            thread { app(PORT_10000 - i, 20) }
        }
        Thread.sleep(10000000)
    }

    @Test  // see now=... and sporadic evt=... (but twice and synchronized)
    fun eval_01 () {
        val N = 2
        val FPS = 10
        val MS_PER_EVT = 100*N

        thread { server(N) }
        Thread.sleep(1000)
        for (i in 0..N-1) {
            thread { client("localhost", PORT_10000 - i) }
        }
        Thread.sleep(1000)
        for (i in 0..N-1) {
            thread { eval(PORT_10000 - i, FPS, MS_PER_EVT) }
        }
        Thread.sleep(200000)
    }
}
