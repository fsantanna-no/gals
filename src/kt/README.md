# GALS - Distributed Deterministic Applications

"Globally asynchronous locally synchronous GALS)" is an architecture to
integrate multiple independent synchronous processes as a single distributed
application.

A synchronous process executes in steps (or *logical ticks*) as successive
reactions dictated by the external environment.
A logical tick may represent an event, such as a mouse click, or simply the
passage of time.
Since execution is guided from outside, the main advantage of the synchronous
model is that we can reproduce the exact behavior of a program by providing the
same sequence steps.

Our goal is to have an application executing with the
*very same exact behavior* in multiples machines.
The idea is to extend the synchronous execution model to a distributed setting.
If we can provide the same sequence of steps in all machines in real time, then
we can guarantee that all instances will behave exactly the same.

However, distributed processes execute under different environments, and thus
we cannot guarantee that logical ticks will be delivered in the exact same
order and time.
For instance, if the user clicks the mouse in one machine, this event needs to
travel to other machines, which will inevitably receive it in the future and at
different times.

The GALS architecture acknowledges that distributed processes are not
synchronized and that communication between them takes time.
Our approach is to delay the occurrence of events so that all processes receive
them in time and can reproduce the logical ticks in the exact same way.

Our solution, entitled `gals`, relies on a central server to coordinate clients
that represent the distributed processes.
The client and server are always the same and are shipped with the `gals`
software distribution.
The actual application must be implemented by the user of `gals` and
communicates with the clients through a simple TCP API to receive and generate
events.
We also ship a sample application that generates random events and dumps the
logical ticks in the screen.

## Install

First, you need to install `java`:

```
$ sudo apt install default-jre
```

Then, you are ready to install `gals`:

```
$ wget https://github.com/fsantanna-no/gals/releases/download/v0.1.0/install-v0.1.0.sh

# choose one:
$ sh install-v0.1.0.sh .                    # either unzip to current directory (must be in the PATH)
$ sudo sh install-v0.1.0.sh /usr/local/bin  # or     unzip to system  directory
```

## Basics

- Execute a `server` that expects `2` clients.
- Execute each `client` to generate ticks every `50ms` and communicate with
  the `app` through ports `9999` and `9998`.

```
$ gals server 2 &
$ gals client 50 9999 &
$ gals client 50 9998 &
```

- Open two other terminals to execute the default `app`.

```
$ gals app 9999
```

```
$ gals app 9998
```

- Observe that the apps behave exactly in the same way.
