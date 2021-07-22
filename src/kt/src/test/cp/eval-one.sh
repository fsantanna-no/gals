#!/bin/sh

TIME=$1
SRV=$2
N=$3
N1=$4
N2=$5
EVT=$6
FPS=$7

#N=100
#FPS=50
#EVT=1000
MSPF=$((1000/$FPS))
MS_PER_EVT=$(($EVT*$N))
MS=$(($TIME*$N*1000))

pkill -f GALS.jar
sleep 1

for i in $(seq $N1 $N2)
do
    gals client $SRV $((10000-$i)) >> client-$i.log &
done
sleep 1

for i in $(seq $N1 $N2)
do
    gals eval $((10000-$i)) $FPS $MS_PER_EVT > eval-$i.log &
done

sleep $TIME

pkill -f GALS.jar
sleep 1
