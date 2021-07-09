fun main (args: Array<String>) {
    when {
        (args[0] == "server") -> server(args[1].toInt())
        (args[0] == "client") -> client(if (args.size == 2) args[1].toInt() else PORT_10000)
        (args[0] == "app")    -> app(args[1].toInt(), args[2].toInt())
        else -> error("impossible case")
    }
}

/*
 * - fazer sync de tempo
 * - --version
 * - server port
 */
