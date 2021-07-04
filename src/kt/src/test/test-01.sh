#!/bin/sh

pkill -f GALS.jar
sleep 1

gals server 2 &
sleep 1

gals client | head -n 45 > /tmp/gals-01.txt &
gals client | head -n 45 > /tmp/gals-02.txt &

sleep 30
pkill -f GALS.jar
sleep 1

diff /tmp/gals-01.txt /tmp/gals-02.txt || exit 1
