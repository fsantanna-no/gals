#!/bin/sh

SRV=192.168.0.14
I5=192.168.0.13
I7=192.168.0.12
DL=192.168.0.5
USER=chico
PASS=password

pkill -f GALS.jar
sshpass -p $PASS ssh "$USER@$I5" -f "pkill -f GALS.jar"
sshpass -p $PASS ssh "$USER@$I7" -f "pkill -f GALS.jar"
sshpass -p $PASS ssh "$USER@$DL" -f "pkill -f GALS.jar"
