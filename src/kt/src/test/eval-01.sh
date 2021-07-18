#!/bin/sh

TIME=60
N=16
FPS=50
MS_PER_EVT=$((1000*$N))
MS=$(($TIME*$N*1000))
echo "CPU=50-110% - TIME=$TIME - N=$N - FPS=$FPS - MS_PER_EVT=$MS_PER_EVT - MS=$MS"

DIR=/tmp/gals-`date +"%s"`/
mkdir $DIR

pkill -f GALS.jar
sleep 1

gals server $N > $DIR/server.log &
sleep 1

for i in $(seq 1 $N)
do
    gals client $((10000-$i)) >> $DIR/client-$i.log &
done
sleep 1

for i in $(seq 1 $N)
do
    gals eval $((10000-$i)) $FPS $MS_PER_EVT > $DIR/eval-$i.log &
done

sleep $TIME

pkill -f GALS.jar
sleep 1

#echo "========== OK =========="

echo -n "Frames   (n): "
val=`cat $DIR/eval-*.log | grep frame | wc -l`
fmt=`printf "%7d" $val`
echo "$fmt  ($(($val/$N)) vs $(($FPS*$TIME)) expected per machine)"
FRAMES=$val

echo -n "Events   (n): "
val=`cat $DIR/eval-*.log | grep evt | wc -l`
fmt=`printf "%7d" $val`
echo "$fmt  ($(($TIME*1000/$MS_PER_EVENT)) expected)"
EVENTS=$val

echo -n "RTT     (ms): "
nnn=`cat $DIR/server.log | grep rtt | wc -l`
sum=`cat $DIR/server.log | grep rtt | cut -d' ' -f2 | paste -s -d+ - | bc`
sum="${sum:-0}"
val=`echo "$sum/$nnn" | bc -l | sed 's/\./,/' | xargs printf "%7.2f" | sed 's/,/./'`
echo "$val"

echo -n "Latency (ms): "
nnn=$val
sum=`cat $DIR/eval-*.log | grep evt | cut -d' ' -f3 | paste -s -d+ - | bc`
sum="${sum:-0}"
val=$(($sum/$EVENTS))
fmt=`printf "%7d" $val`
echo "$fmt"

echo -n "Drift   (ms): "
val=`cat $DIR/client-*.log | grep drift | cut -d' ' -f3 | paste -s -d+ - | bc`
val="${val:-0}"
fmt=`printf "%7d" $val`
pct=`echo "$val/$MS" | bc -l | sed 's/\./,/' | xargs printf "%.4f" | sed 's/,/./'`
echo "$fmt  $pct%"

echo -n "Freeze   (n): "
val=`cat $DIR/eval-* | grep freeze | wc -l`
fmt=`printf "%7d" $val`
pct=`echo "$val/$FRAMES" | bc -l | sed 's/\./,/' | xargs printf "%.4f" | sed 's/,/./'`
echo "$fmt  $pct%"

echo -n "Late    (ms): "
val=`cat $DIR/eval-*.log | grep late | cut -d' ' -f3 | paste -s -d+ - | bc`
val="${val:-0}"
fmt=`printf "%7d" $val`
pct=`echo "$val/$MS" | bc -l | sed 's/\./,/' | xargs printf "%.4f" | sed 's/,/./'`
echo "$fmt  $pct%"
