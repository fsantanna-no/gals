#!/bin/sh
#set -x

TIME=300
PC=192.168.0.14
I5=192.168.0.13
I7=192.168.0.12
LC=152.92.236.9
SRV=$LC
USER1=user
USER2=user
PASS1=password
PASS2=password

for i in 1 2 3
do
    for N in 25 #2 5 10 25 50 100
    do
        for EVT in 5000 1000 500 250
        do
            for FPS in 10 25 50 100
            do
                DIR=gals-`date +"%s"`/
                echo "$N - $EVT - $FPS - $i"
                echo $DIR
                mkdir $DIR
                ./eval-kill.sh
                sleep 2
                sshpass -p $PASS2 ssh "$USER2@$LC" -f "cd gals/ && rm -f *.log && gals server $N > server.log" &
                sleep 2
                N3=$((N/3))
                echo "$N // $(($N3*0+1))-$(($N3*1)) // $(($N3*1+1))-$(($N3*2)) // $(($N3*2+1))-$N"
                #echo "cd gals/ && ./eval-one.sh $TIME $SRV $N $(($N3*0+1)) $(($N3*1)) $EVT $FPS"
                #echo "cd gals/ && ./eval-one.sh $TIME $SRV $N $(($N3*1+1)) $(($N3*2)) $EVT $FPS" &
                #echo "cd gals/ && ./eval-one.sh $TIME $SRV $N $(($N3*2+1)) $N $EVT $FPS" &
                sshpass -p $PASS1 ssh "$USER1@$I5" -f "cd gals/ && rm -f *.log && ./eval-one.sh $TIME $SRV $N $(($N3*0+1)) $(($N3*1)) $EVT $FPS" &
                sshpass -p $PASS1 ssh "$USER1@$I7" -f "cd gals/ && rm -f *.log && ./eval-one.sh $TIME $SRV $N $(($N3*1+1)) $(($N3*2)) $EVT $FPS" &
                cd gals/ && rm -f *.log &&  ./eval-one.sh $TIME $SRV $N $(($N3*2+1)) $N $EVT $FPS &
                sleep $TIME
                echo "-=-=-=-=-=-=-"
                ./eval-kill.sh
                sshpass -p $PASS1 scp "$USER1@$I5:gals/*.log" $DIR
                sshpass -p $PASS1 scp "$USER1@$I7:gals/*.log" $DIR
                sshpass -p $PASS2 scp "$USER2@$LC:gals/*.log" $DIR
                cp gals/*.log $DIR
                ./eval-log.sh $DIR $TIME $N $EVT $FPS
                echo
                echo
                #exit 0
            done
        done
    done
done
