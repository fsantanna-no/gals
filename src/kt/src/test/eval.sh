#!/bin/sh

for i in 1 2 3
do
    for N in 2 5 10 50 100
    do
        for EVT in 10000 5000 1000 500 250 100
        do
            for FPS in 10 20 40 50 100
            do
                    # Tamanho de payload: 0b, 100b, 10kb, 1M
                    export N
                    export EVT
                    export FPS
                    ./eval-01.sh
                    echo
                    echo
            done
        done
    done
done
