import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random

fun eval (port: Int, fps: Int, evt_per_min: Int) {
    // connects with the client on the provided port
    val socket = Socket("localhost", port)
    val writer = DataOutputStream(socket.getOutputStream()!!)
    val reader = DataInputStream(socket.getInputStream()!!)

    // sends the desired FPS and receives client id
    writer.writeInt(fps)
    val self = reader.readInt()

    // thread that receives the logical ticks from the client
    thread {
        var old: Long = -1
        var pause_n = 0
        var pause_nn = 0
        var pause_flip = false

        while (true) {
            val now = reader.readLong()
            val evt = reader.readInt()
            val ms_per_frame = 1000/fps

            if (old == now) {
                if (!pause_flip) {
                    pause_n++
                }
                pause_flip = true
                pause_nn++
                println("pause [$self] $pause_nn/$pause_n")
            } else {
                pause_flip = false
            }
            old = now

            // between 70% - 110%
            Thread.sleep((ms_per_frame*0.7 + Random.nextDouble(ms_per_frame*1.4)).toLong())
        }
    }

    // thread that emits random events back to the client
    thread {
        // 1 evt/min = 60000 ms/evt
        val ms_per_evt = evt_per_min * 60000
        while (true) {
            // between 90% - 110%
            Thread.sleep((ms_per_evt*0.9 + Random.nextDouble(ms_per_evt*1.2)).toLong())
            writer.writeInt(1 + Random.nextInt(10))
        }
    }
}