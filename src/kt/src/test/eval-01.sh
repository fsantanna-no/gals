#!/bin/sh

# descobri pq latency tá tao alto (>80ms)
#   - estamos com +5ms constante
#   - testamos 16/25/100 clientes
#   - mudamos pra 4 agora
#       - continua >70ms
#   - mudamos pra 2 agora
#       - continua >65ms
#   - agora mudei: N/5 que vai ser +0ms p/ 1,2,3,4
#   - O minimo seria 2RTT+DT/2 por conta do protocolo
#   - O minimo teorico seria RTT/2 em media, que é o tempo de bcast de um evento

TIME=600
N=100
FPS=50
MSPF=$((1000/$FPS))
MS_PER_EVT=$((2000*$N))
MS=$(($TIME*$N*1000))

DIR=/tmp/gals-`date +"%s"`/
mkdir $DIR

echo "CPU=50-110% - TIME=$TIME - N=$N - FPS=$FPS - MS_PER_EVT=$MS_PER_EVT - MS=$MS"
echo $DIR

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
val=`cat $DIR/client-*.log | grep event | wc -l`
fmt=`printf "%7d" $val`
echo "$fmt  ($(($TIME*1000*$N/$MS_PER_EVT)) expected)"
EVENTS=$val

echo -n "RTT     (ms): "
nnn=`cat $DIR/server.log | grep rtt | wc -l`
sum=`cat $DIR/server.log | grep rtt | cut -d' ' -f2 | paste -s -d+ - | bc`
sum="${sum:-0}"
val=`echo "$sum/$nnn" | bc -l | sed 's/\./,/' | xargs printf "%7.2f" | sed 's/,/./'`
echo "$val"

echo -n "Latency (fr): "
nnn=$val
sum=`cat $DIR/client-*.log | grep event | cut -d' ' -f3 | paste -s -d+ - | bc`
sum="${sum:-0}"
val=$(($sum/$EVENTS))
fmt=`printf "%7d" $val`
echo "$fmt"

echo -n "Drift   (ms): "
val=`cat $DIR/client-*.log | grep drift | cut -d' ' -f3 | paste -s -d+ - | bc`
val="${val:-0}"
fmt=`printf "%7d" $val`
pct=`echo "$val*100/$MS" | bc -l | sed 's/\./,/' | xargs printf "%.2f" | sed 's/,/./'`
echo "$fmt  $pct%"

echo -n "Freeze  (fr): "
val=`cat $DIR/eval-* | grep freeze | wc -l`
fmt=`printf "%7d" $val`
pct=`echo "$val*100/$FRAMES" | bc -l | sed 's/\./,/' | xargs printf "%.2f" | sed 's/,/./'`
echo "$fmt  $pct%"

echo -n "Late    (ms): "
val=`cat $DIR/eval-*.log | grep late | cut -d' ' -f3 | paste -s -d+ - | bc`
val="${val:-0}"
fmt=`printf "%7d" $val`
pct=`echo "$val*100/$MS" | bc -l | sed 's/\./,/' | xargs printf "%.2f" | sed 's/,/./'`
echo "$fmt  $pct%"

for i in $(seq 1 $N)
do
    cat $DIR/eval-$i.log | grep frame | cut -d' ' --complement -f2 | uniq | head -n $(($FRAMES*8/$N/10)) > $DIR/frames-$i.txt
done

for i in $(seq 1 $N)
do
    diff -q $DIR/frames-1.txt $DIR/frames-$i.txt
    diff $DIR/frames-1.txt $DIR/frames-$i.txt || exit 1
done
