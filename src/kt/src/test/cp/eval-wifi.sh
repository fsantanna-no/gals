#!/bin/sh

TIME=300
SRV=192.168.0.17
I5=192.168.0.27
I7=192.168.0.22
DL=192.168.0.28
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
                DIR=gals-`date +"%s"`/
                echo "$N - $EVT - $FPS - $i"
                echo $DIR
                mkdir $DIR
                pkill -f GALS.jar
                sleep 2
                gals server $N > $DIR/server.log &
                sleep 2
                N3=$((N/3))
                echo "$N // $(($N3*0+1))-$(($N3*1)) // $(($N3*1+1))-$(($N3*2)) // $(($N3*2+1))-$N"
                #echo "cd gals/ && ./eval-one.sh $TIME $SRV $N $(($N3*0+1)) $(($N3*1)) $EVT $FPS"
                #echo "cd gals/ && ./eval-one.sh $TIME $SRV $N $(($N3*1+1)) $(($N3*2)) $EVT $FPS" &
                #echo "cd gals/ && ./eval-one.sh $TIME $SRV $N $(($N3*2+1)) $N $EVT $FPS" &
                sshpass -p $PASS ssh "$USER@$I5" -f "cd gals/ && rm -f *.log && ./eval-one.sh $TIME $SRV $N $(($N3*0+1)) $(($N3*1)) $EVT $FPS" &
                sshpass -p $PASS ssh "$USER@$I7" -f "cd gals/ && rm -f *.log && ./eval-one.sh $TIME $SRV $N $(($N3*1+1)) $(($N3*2)) $EVT $FPS" &
                sshpass -p $PASS ssh "$USER@$DL" -f "cd gals/ && rm -f *.log && ./eval-one.sh $TIME $SRV $N $(($N3*2+1)) $N $EVT $FPS" &
                sleep $TIME
                pkill -f GALS.jar
                sshpass -p $PASS scp "$USER@$I5:gals/*.log" $DIR
                sshpass -p $PASS scp "$USER@$I7:gals/*.log" $DIR
                sshpass -p $PASS scp "$USER@$DL:gals/*.log" $DIR
                ./eval-log.sh $DIR $TIME $N $EVT $FPS
                echo
                echo
                #exit 0
            done
        done
    done
done
