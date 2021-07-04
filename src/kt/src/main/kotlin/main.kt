fun main (args: Array<String>) {
    when {
        (args[0] == "server") -> server(args[1].toInt())
        (args[0] == "client") -> client()
        else -> error("impossible case")
    }
}