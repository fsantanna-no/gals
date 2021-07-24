#!/bin/sh

P1=192.168.0.14
I5=192.168.0.13
I7=192.168.0.12
P2=192.168.0.5
LC=152.92.236.9
USER1=xxx
PASS1=xxx
USER2=xxx
PASS2=xxx

pkill -f GALS.jar
sshpass -p $PASS1 ssh "$USER1@$I5" -f "pkill -f GALS.jar"
sshpass -p $PASS1 ssh "$USER1@$I7" -f "pkill -f GALS.jar"
sshpass -p $PASS1 ssh "$USER1@$P2" -f "pkill -f GALS.jar"
sshpass -p $PASS2 ssh "$USER2@$LC" -f "pkill -f GALS.jar"
