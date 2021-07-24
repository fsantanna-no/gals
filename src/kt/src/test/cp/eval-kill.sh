#!/bin/sh

P1=192.168.0.14
I5=192.168.0.13
I7=192.168.0.12
P2=192.168.0.5
LC=152.92.236.9
USER=chico
PASS=password

pkill -f GALS.jar
sshpass -p $PASS ssh "$USER@$I5" -f "pkill -f GALS.jar"
sshpass -p $PASS ssh "$USER@$I7" -f "pkill -f GALS.jar"
sshpass -p $PASS ssh "$USER@$P2" -f "pkill -f GALS.jar"
sshpass -p $PASS ssh "$USER@$LC" -f "pkill -f GALS.jar"
