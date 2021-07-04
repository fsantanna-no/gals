import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.concurrent.thread

@TestMethodOrder(Alphanumeric::class)
class Tests {
    @Test
    fun test () {
        thread { server(2) }
        Thread.sleep(1000)
        thread { client(50) }
        thread { client(50) }
        Thread.sleep(100000)
    }
}
