#!/bin/sh

I5=192.168.0.27
I7=192.168.0.22
DL=192.168.0.28
USER=chico
PASS=password

for host in $I5 $I7 $DL
do
    echo ">>> $host"
    sshpass -p $PASS ssh "$USER@$host" -f "mkdir gals/"
    sshpass -p $PASS scp eval-wifi.sh eval-one.sh "$USER@$host:gals/"
    sshpass -p $PASS scp install-test.sh gals-test.zip eval-wifi.sh eval-one.sh "$USER@$host:gals/"
    #sshpass -p $PASS scp xmain "$USER@$host:gals/"
    #sshpass -p $PASS ssh "$USER@$host" -f "echo $PASS | sudo -S apt install libsdl2-2.0-0 libsdl2-net-2.0-0"
    sshpass -p $PASS ssh "$USER@$host" -f "cd gals/ && echo $PASS | sudo -S sh install-test.sh /usr/local/bin"
    sleep 5
done
