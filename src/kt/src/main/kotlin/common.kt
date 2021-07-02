val PORT_10011 = 10011
val PORT_10012 = 10012
val RTT_50 = 50 // below 50ms error in client, probably b/c of minimum thread preemption time?

enum class Message {
    START, WANTED, DECIDED
}