#!/bin/sh

I7=192.168.0.22
USER=chico
PASS=adgjlL1

sshpass -p $PASS ssh "$USER@$I7" -f "mkdir gals/"
sshpass -p $PASS scp install-test.sh gals-test.zip eval-wifi.sh eval-one.sh "$USER@$I7:gals/"
sshpass -p $PASS ssh "$USER@$I7" -f "cd gals/ && echo $PASS | sudo -S sh install-test.sh /usr/local/bin"
