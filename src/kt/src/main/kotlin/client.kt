import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun client () {
    val socket = Socket("localhost", PORT_10000)
    val writer = DataOutputStream(socket.getOutputStream()!!)
    val reader = DataInputStream(socket.getInputStream()!!)

    fun app_output (now: Long, evt: Int) {
        writer.writeLong(now)
        writer.writeInt(evt)
    }

    var nxt: Long = 0
    fun app_input (now: Long, evt: Int?) {
        println("[app]    now=$now     evt=$evt")
        if (now > nxt) {
            nxt = now + Random.nextLong(10000)
            println("[app] emit")
            app_output(now, Random.nextInt(10))
        }
    }

    val start = reader.readInt()
    assert(start == 0)
    val late = Instant.now().toEpochMilli()
    println("[client] started")

    app_input(0, null)
    while (true) {
        Thread.sleep(1000)
        val now = Instant.now().toEpochMilli() - late
        app_input(now, null)
    }
}