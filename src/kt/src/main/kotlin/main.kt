fun main (args: Array<String>) {
    when {
        (args[0] == "server") -> server(args[1].toInt())
        (args[0] == "client") -> client(args[1].toLong(), if (args.size == 3) args[2].toInt() else PORT_10000)
        (args[0] == "app")    -> app(args[1].toInt())
        else -> error("impossible case")
    }
}

/*
 * - verificar delays no SDL
 *   - mostrar freezes e max rtt
 * - fazer sync de tempo
 * - delays em todos os lugares
 * - --version
 * - server port
 * - ressaltar questão da interatividade, inclusive no título
 *   - single application, multiple views, may restrict events per node
 */
