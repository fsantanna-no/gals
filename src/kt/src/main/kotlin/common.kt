val PORT_10000 = 10000
val RTT_100 = 50 // below 50ms error in client, probably b/c of minimum thread preemption time?

enum class Message {
    START, WANTED, DECIDED
}