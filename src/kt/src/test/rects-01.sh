#!/bin/sh

EXE=/x/gals/src/sdl/xmain
N=2

pkill -f GALS.jar
pkill -9 -f xmain
sleep 1

gals server $N &
sleep 1

for i in $(seq 1 $N)
do
    gals client localhost $((10000-$i)) &
done
sleep 1

for i in $(seq 1 $N)
do
    $EXE $((10000-$i)) &
done

wait
