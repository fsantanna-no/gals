import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.concurrent.thread

@TestMethodOrder(Alphanumeric::class)
class Tests {

    @Test
    fun test () {
        thread { server() }
        Thread.sleep(1000)
        thread { client() }
        thread { client() }
        Thread.sleep(100000)
    }
}
