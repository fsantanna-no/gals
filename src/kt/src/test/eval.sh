#!/bin/sh

for i in 1 2 3
do
    for N in 2 5 10 50 100
    do
        for EVT in 5000 1000 500 250
        do
            for FPS in 10 25 50 100
            do
                    echo "$N - $EVT - $FPS - [$i]"
                    ./eval-01.sh $N $EVT $FPS
                    echo
                    echo
            done
        done
    done
done
