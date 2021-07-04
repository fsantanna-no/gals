#!/bin/sh

pkill -f GALS.jar
sleep 1

gals server 2 &
sleep 1

gals client 50 9999 &
gals client 50 9998 &
sleep 1

gals app 9999 > /tmp/gals-01.txt &
gals app 9998 > /tmp/gals-02.txt &

sleep 30
pkill -f GALS.jar
sleep 1

diff /tmp/gals-01.txt /tmp/gals-02.txt || exit 1
