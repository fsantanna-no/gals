fun main (args: Array<String>) {
    when {
        (args[0] == "server") -> server(args[1].toInt())
        (args[0] == "client") -> client(50)
        else -> error("impossible case")
    }
}

/*
 * - passar para clientes desired DT
 * - assert se falhar
 * - sdl
 * - tcp port
 */
