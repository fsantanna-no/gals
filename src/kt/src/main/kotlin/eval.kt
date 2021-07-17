import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.random.Random

fun eval (port: Int, fps: Int, ms_per_evt: Int) {
    // connects with the client on the provided port
    val socket = Socket("localhost", port)
    val writer = DataOutputStream(socket.getOutputStream()!!)
    val reader = DataInputStream(socket.getInputStream()!!)

    // sends the desired FPS and receives client id
    writer.writeInt(fps)
    val self = reader.readInt()
    var evt_ms: Long = 0
    val ms_per_frame = 1000/fps

    // thread that receives the logical ticks from the client
    thread {
        var old: Long? = null
        var ms0: Long? = null
        while (true) {
            val now = reader.readLong()
            val evt = reader.readInt()

            if (ms0 != null) {
                val ms1 = Instant.now().toEpochMilli()
                val late = ms1 - (ms0 + ms_per_frame)
                ms0 = ms1
                if (late > 0) {
                    log("late [$self] $late")
                }
            }

            if (evt == self) {
                log("evt [$self] ${Instant.now().toEpochMilli()-evt_ms}")
            }

            if (old == now) {
                log("freeze [$self]")
                continue
            }
            old = now

            // between 50% - 110%
            val x = (ms_per_frame*0.5 + Random.nextDouble(ms_per_frame*0.6)).toLong()
            Thread.sleep(x)

            /*
            when (evt) {
                0    -> println("now=$now")
                else -> println("now=$now evt=$evt")
            }
             */
        }
    }

    // thread that emits random events back to the client
    thread {
        while (true) {
            // between 50% - 150%
            Thread.sleep((ms_per_evt*0.5 + Random.nextDouble(ms_per_evt*1.0)).toLong())
            writer.writeInt(self)
            evt_ms = Instant.now().toEpochMilli()
        }
    }
}