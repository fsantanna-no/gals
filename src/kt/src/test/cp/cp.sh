#!/bin/sh

I5=192.168.0.13
I7=192.168.0.12
P2=192.168.0.5
LC=152.92.236.9
USER1=chico
USER2=francisco
PASS1=password
PASS2=password

for host in $I5 $I7
do
    echo ">>> $host"
    #sshpass -p $PASS1 ssh "$USER1@$host" -f "mkdir gals/"
    sshpass -p $PASS1 scp eval-wifi.sh eval-one.sh "$USER1@$host:gals/"
    sshpass -p $PASS1 scp install-test.sh gals-test.zip eval-wifi.sh eval-one.sh "$USER1@$host:gals/"
    #sshpass -p $PASS1 scp xmain "$USER1@$host:gals/"
    #sshpass -p $PASS1 ssh "$USER1@$host" -f "echo $PASS1 | sudo -S apt install libsdl2-2.0-0 libsdl2-net-2.0-0"
    sshpass -p $PASS1 ssh "$USER1@$host" -f "cd gals/ && echo $PASS1 | sudo -S sh install-test.sh /usr/local/bin"
    sleep 5
done

for host in $LC
do
    echo ">>> $host"
    #sshpass -p $PASS2 ssh "$USER2@$host" -f "mkdir gals/"
    sshpass -p $PASS2 scp eval-wifi.sh eval-one.sh "$USER2@$host:gals/"
    sshpass -p $PASS2 scp install-test.sh gals-test.zip eval-wifi.sh eval-one.sh "$USER2@$host:gals/"
    #sshpass -p $PASS2 scp xmain "$USER2@$host:gals/"
    #sshpass -p $PASS2 ssh "$USER2@$host" -f "echo $PASS2 | sudo -S apt install libsdl2-2.0-0 libsdl2-net-2.0-0"
    sshpass -p $PASS2 ssh "$USER2@$host" -f "cd gals/ && echo $PASS2 | sudo -S sh install-test.sh /usr/local/bin"
    sleep 5
done
