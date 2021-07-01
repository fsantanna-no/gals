import kotlin.concurrent.thread

fun main(args: Array<String>) {
    println("Hello World!")
    thread { server() }
    Thread.sleep(1000)
    client()
}