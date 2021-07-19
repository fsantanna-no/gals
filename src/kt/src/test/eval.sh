#!/bin/sh

for N in 2 5 10 100
do
    for EVT in 10000 5000 1000 100  # 10s 5s 1s 100ms
    do
        for FPS in 10 20 40 50 100
        do
            for i in 1 2 3 4 5
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
