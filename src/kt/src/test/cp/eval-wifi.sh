#!/bin/sh

SRV=192.168.0.17
I7=192.168.0.22
USER=chico
PASS=password

for N in 2 5 10 100
do
    for EVT in 10000 5000 1000 100  # 10s 5s 1s 100ms
    do
        for FPS in 10 20 40 50 100
        do
            for i in 1 2 3 4 5
            do
                echo "$N - $EVT - $FPS - $i"
                DIR=gals-`date +"%s"`/
                mkdir $DIR
                pkill -f GALS.jar
                sleep 1
                gals server $N > $DIR/server.log &
                sleep 1
                sshpass -p $PASS ssh "$USER@$I7" -f "cd gals/ && ./eval-one.sh $SRV $N 1 $N $EVT $FPS" &
                sleep 600
                sshpass -p $PASS scp "$USER@$I7:gals/*.log" $DIR
                ./eval-log.sh $DIR 600 $N $EVT $FPS
                echo
                echo
                exit 0
            done
        done
    done
done
