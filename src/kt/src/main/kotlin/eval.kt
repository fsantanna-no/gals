import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Long.max
import java.lang.Long.min
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
                val dt = ms1 - ms0
                ms0 += min(dt,ms_per_evt.toLong())
                val late = max(0, dt - ms_per_frame)
                if (late > 0) {
                    log("late [$self] $late")
                }
            } else {
                ms0 = Instant.now().toEpochMilli()
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
            //Thread.sleep(x)

            log("frame [$self] $now $evt")
        }
    }

    // thread that emits random events back to the client
    thread {
        while (true) {
            // between 0% - 200%
            Thread.sleep(Random.nextInt(2*ms_per_evt).toLong())
            writer.writeInt(self)
            evt_ms = Instant.now().toEpochMilli()
        }
    }
}