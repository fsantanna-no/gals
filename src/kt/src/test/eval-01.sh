#!/bin/sh

TIME=600
N=16
FPS=50
MS_PER_EVT=$((1000*$N))
MS=$(($TIME*$N*1000))
echo "CPU=0% - TIME=$TIME - N=$N - FPS=$FPS - MS_PER_EVT=$MS_PER_EVT - MS=$MS"

rm -Rf /tmp/gals-*.log
pkill -f GALS.jar
sleep 1

gals server $N > /tmp/gals-server.log &
sleep 1

for i in $(seq 1 $N)
do
    gals client $((10000-$i)) >> /tmp/gals-client-$i.log &
done
sleep 1

for i in $(seq 1 $N)
do
    gals eval $((10000-$i)) $FPS $MS_PER_EVT > /tmp/gals-eval-$i.log &
done

sleep $TIME

#echo "========== OK =========="

echo -n "RTT     (ms): "
nnn=`cat /tmp/gals-server.log | grep rtt | wc -l`
sum=`cat /tmp/gals-server.log | grep rtt | cut -d' ' -f2 | paste -s -d+ - | bc`
val=$(($sum/$nnn))
fmt=`printf "%6d" $val`
echo "$fmt"

echo -n "Events   (n): "
val=`cat /tmp/gals-eval-*.log | grep evt | wc -l`
fmt=`printf "%6d" $val`
echo "$fmt"

echo -n "Latency (ms): "
nnn=$val
sum=`cat /tmp/gals-eval-*.log | grep evt | cut -d' ' -f3 | paste -s -d+ - | bc`
val=$(($sum/$nnn))
fmt=`printf "%6d" $val`
echo "$fmt"

echo -n "Drift   (ms): "
val=`cat /tmp/gals-client-*.log | grep drift | cut -d' ' -f3 | paste -s -d+ - | bc`
fmt=`printf "%6d" $val`
num=$(($val*100*100/$MS))
pct=`echo "$num/100" | bc -l | sed 's/0*$//'`
echo "$fmt  $pct%"

echo -n "Freeze  (ms): "
val=`cat /tmp/gals-eval-* | grep freeze | cut -d' ' -f3 | paste -s -d+ - | bc`
fmt=`printf "%6d" $val`
num=$(($val*100*100/$MS))
pct=`echo "$num/100" | bc -l | sed 's/0*$//'`
echo "$fmt  $pct%"

pkill -f GALS.jar
