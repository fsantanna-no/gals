#!/bin/sh

TIME=60
N=16
FPS=50
MS_PER_EVT=$((1000*$N))
MS=$(($TIME*$N*1000))
echo "CPU=70-120% - TIME=$TIME - N=$N - FPS=$FPS - MS_PER_EVT=$MS_PER_EVT - MS=$MS"

DIR=/tmp/gals-`date +"%s"`/
mkdir $DIR

pkill -f GALS.jar
sleep 1

gals server $N > /tmp/server.log &
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

#echo "========== OK =========="

echo -n "RTT     (ms): "
nnn=`cat /tmp/server.log | grep rtt | wc -l`
sum=`cat /tmp/server.log | grep rtt | cut -d' ' -f2 | paste -s -d+ - | bc`
sum="${sum:-0}"
val=$(($sum/$nnn))
fmt=`printf "%7d" $val`
echo "$fmt"

echo -n "Events   (n): "
val=`cat $DIR/eval-*.log | grep evt | wc -l`
fmt=`printf "%7d" $val`
echo "$fmt"

echo -n "Latency (ms): "
nnn=$val
sum=`cat $DIR/eval-*.log | grep evt | cut -d' ' -f3 | paste -s -d+ - | bc`
sum="${sum:-0}"
val=$(($sum/$nnn))
fmt=`printf "%7d" $val`
echo "$fmt"

echo -n "Drift   (ms): "
val=`cat $DIR/client-*.log | grep drift | cut -d' ' -f3 | paste -s -d+ - | bc`
val="${val:-0}"
fmt=`printf "%7d" $val`
num=$(($val*100*100/$MS))
pct=`echo "$num/100" | bc -l | sed 's/0*$//'`
echo "$fmt  $pct%"

echo -n "Freeze  (ms): "
#val=`cat $DIR/eval-* | grep freeze | cut -d' ' -f3 | paste -s -d+ - | bc`
nnn=`cat $DIR/eval-* | grep freeze | wc -l`
val=$(($nnn*1000/$FPS))
fmt=`printf "%7d" $val`
num=$(($val*100*100/$MS))
pct=`echo "$num/100" | bc -l | sed 's/0*$//'`
echo "$fmt  $pct%"

echo -n "Late    (ms): "
val=`cat $DIR/client-*.log | grep late | cut -d' ' -f3 | paste -s -d+ - | bc`
val="${val:-0}"
fmt=`printf "%7d" $val`
num=$(($val*100*100/$MS))
pct=`echo "$num/100" | bc -l | sed 's/0*$//'`
echo "$fmt  $pct%"

pkill -f GALS.jar
