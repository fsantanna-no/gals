#!/bin/sh

pkill -f GALS.jar
sleep 1

gals.sh server 2 &
sleep 1

gals.sh client | head -n 45 > /tmp/gals-01.txt &
gals.sh client | head -n 45 > /tmp/gals-02.txt &

sleep 30
pkill -f GALS.jar
sleep 1

diff /tmp/gals-01.txt /tmp/gals-02.txt || exit 1
