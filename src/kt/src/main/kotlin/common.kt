val PORT_10001 = 10001
val PORT_10002 = 10002
val RTT_50 = 50 // below 50ms error in client, probably b/c of minimum thread preemption time?

enum class Message {
    START, WANTED, DECIDED
}