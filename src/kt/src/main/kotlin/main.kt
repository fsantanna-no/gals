import kotlin.concurrent.thread

fun main(args: Array<String>) {
    thread { server() }
    Thread.sleep(1000)
    thread { client() }
    thread { client() }
    //client()
}