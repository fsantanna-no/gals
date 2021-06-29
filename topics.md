Globally-Asynchronous Locally-Synchronous Deterministic Distributed Applications
Deterministic Distributed Applications

- determinism and consistency
- Determinism is a property of individual nodes in a dis-
tributed application and states that a program always produces the same
output when fed with the same input
- Consistency is a property of the whole
system and states that all nodes should have the same view of the order of
events.

Furthermore, they
also indicate that the consistency model enforced by the GALS middleware
Mars guarantees that all nodes always agree upon the order of events in a
distributed presentation.

- For the collaboration and/or control be effective, all devices should have
the same view of the whole system. However, the lack of an accurately synchro-
nized global clock and asynchronous user interactions can hinder that [11]

Limitations
    - centralized
    - permissionedo

As stated by Benveniste and Gary, "there is no
reason the engineer should want his [system] to behave in some unpredictable
manner" [12]

Here we use a definition that is based
on the sequential consistency model defined by Lamport [4], but with an
extension to accommodate the timing aspect—we call it timing-sequential
consistency model.

The sequential consistency model states that a system is
said consistent if "the result of any execution is the same as if the operations of
all the [processes] were executed in some sequential order, and the operationsof each individual [process] appear in this sequence in the order specified by
its program" [4]
However, for multimedia applications just the ordering of
messages is not enough for a consistency model: different processes may reach
completely different states if they execute the same operations at different time
instants. Thus, the consistency model that we adopt adds the constraint that
all operations should not only be executed in the same order, but also at the
same time in all processes. In this work, the consistency property is defined in
terms of this model.

The timing-sequential consistency model implies that: i) there is a total
ordering of events on which all processes agree; ii) all messages sent from a
given process are received in the same order by all others; and iii) all processes
receive messages at the same time. If a system enforces this consistency model,
one does not have to worry about implementing algorithms to ensure that all
processes of a distributed system have the same global view.

For the distributed scenario, we assume network architectures with no
QoS guarantees. Several works approach such networks using the GALS (Glob-
ally Asynchronous, Locally Synchronous) architectural style [1]. In GALS
systems, computations within individual synchronous nodes are determinis-
tic, with the communication latency as the only source of non-determinism.
Mars, the practical result of the second part of this thesis, is a middleware
that follows the GALS style and supports consistent execution (following the
timing-sequential consistency model) of distributed interactive multimedia ap-
plications.

GALS is an alternative for developing systems in which individual
modules take advantage of the synchronous approach and the communication
latency and jitter are usually the only source of non-determinism.

To better frame this discussion, let’s consider the abstract service model
composed of three layers depicted in Figure 2.2.

Within the embedded software community, the TTA (Time-Triggered
Architecture) [52] is one of the most remarkable example of the first category,
followed by a more recent proposal called PALS (Physically Asynchronous
Logically Synchronous) [53]. TTA systems assume a maximum network delay
and rely on the notion of physical time consistently maintained synchronized
throughout nodes. The PALS architecture is similar to TTA, but it makes
stronger assumptions and has the abstraction of perfectly synchronized virtual
clocks that drive computations in individual nodes. These timing guarantees
support the deployment of systems in which all nodes operate in lockstep,
changing their state synchronously [51].

Architectures that can provide timing guarantees rely on very strong re-
quirements on their underlying networks. Some of them are not feasible to
assume in unmanageable environments such as the Internet or even in wire-
less local networks [51, 60]. Network architectures in the second category, that
make no assumption regarding clock synchronization, clock paces or bounded
communication delays (e.g., LTTA (Loosely Time-Triggered Architecture) [60],Ethernet, WiFi, Internet) generally implement the GALS (Globally Asyn-
chronous Locally Synchronous) [1] architectural style.

- Multiclock Esterel [61], CRP [62] and CRSM [63] are
examples of Esterel-like languages that follow this concept.
- SystemJ [65], DSystemJ [66]
and GRL [64] are examples of asynchronous languages for GALS

This can lead to the classical distributed consensus problem 4 [67], with the
peculiarity that the multimedia presentation can halt if the system takes too
many rounds to reach the consensus



Under the sequential consistency model, in each execution of the system
all processes always see the same order of operations, but in successive runs
such ordering may change due to the non-deterministic communication delay. If
one can somehow enforce the same delay in multiple executions, then successive
runs hold the same global ordering.




If different devices see different order
of events, it is likely that some glitches may happen during the game, like the
dead-man shoots situation [94]: an avatar that is dead for some players, but
lives for others, shoots another avatar.


Consider now a scenario of two remote controls and multiple TV sets.
Assume that each of these remote controls may change the channel of all
TVs. If users interact concurrently with both controls and the system does
not guarantee that all TVs see the same order of events, at the end of the
interaction each TV may be on a different channel.


When one moves from local to distributed applications, the properties
that Céu-Media guarantees cannot be maintained due to the violation of
the synchronous hypothesis.


The Mars middleware is part of the result of this investigation. It has a
centralized architecture, it supports the communication of processes running on
different devices and it implements the timing-sequential consistency model.

In
a Mars distributed application there is no notion of global synchronized clock
or assumption regarding maximum communication latency. Each process runs
synchronously and may emit asynchronous events that are received by others.

The middleware guarantees two properties: i) processes receive events in
the same order; and ii) processes receive all events at the same logical time
(different from the logical time at which they were emitted). Thus, Mars
guarantees that all nodes agree not only upon the global ordering of events
(property i) but also upon their timing (property ii).


That is, the application logic has no explicit com-
munication primitives, but rather inter-application communication bindings
are defined by an external script. Next section presents Mars in more details.


Note that this approach ensures the two conditions that a system should
guarantee following Lamport’s sequential consistency definition (page 71):

This problem arises because in distributed interactive multimedia applications
consistency is not only about ensuring total ordering of messages, but also
about guaranteeing that operations are executed at the correct time [95].



For each pair <I i , P i >, the server sends a message to all peers in P i
asking for the current logical time (line 5) and then waits for the responses.
Upon receiving each reply, the server updates the variables T max , which
should store the most advanced logical time reported, and RT T max , which
should store the longest RTT calculated (lines 9–20). Note that the algorithm
initializes RT T max with the value τ , which corresponds to an estimate of the
mean RTT of the underlying network. This ensures a minimum value for the
RT T max variable, whose implication is explained when we discuss the algorithm
executed on the peers for ensuring the consistency.


After the server has received all responses, it can calculate the timestamp
T timestamp of the input event I i , which is the sum of the most advanced logical
time reported (T max ), with the maximum RTT calculated or τ , whichever isChapter 5. Mars: GALS Middleware for Programming Distributed Interactive
Multimedia Applications
82
greater (RT T max ) and with the total time elapsed waiting for the responses
(now() - T 0 ) – line 21. Additionally, we add a ∆ value for compensating eventual
network jitter, which in our current implementation is a tenth of the maximum
RTT (∆ = 0.1 ∗ RT T max ).
