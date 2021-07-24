#!/bin/sh

pkill -f GALS.jar
sleep 1

gals server 10 &
sleep 1

gals client localhost 9999 &
gals client localhost 9998 &
gals client localhost 9997 &
gals client localhost 9996 &
gals client localhost 9995 &
gals client localhost 9994 &
gals client localhost 9993 &
gals client localhost 9992 &
gals client localhost 9991 &
gals client localhost 9990 &
sleep 1

gals app 9999 20 | uniq | head -n 1200 > /tmp/gals-01.txt &
gals app 9998 20 | uniq | head -n 1200 > /tmp/gals-02.txt &
gals app 9997 20 | uniq | head -n 1200 > /tmp/gals-03.txt &
gals app 9996 20 | uniq | head -n 1200 > /tmp/gals-04.txt &
gals app 9995 20 | uniq | head -n 1200 > /tmp/gals-05.txt &
gals app 9994 20 | uniq | head -n 1200 > /tmp/gals-06.txt &
gals app 9993 20 | uniq | head -n 1200 > /tmp/gals-07.txt &
gals app 9992 20 | uniq | head -n 1200 > /tmp/gals-08.txt &
gals app 9991 20 | uniq | head -n 1200 > /tmp/gals-09.txt &
gals app 9990 20 | uniq | head -n 1200 > /tmp/gals-10.txt &

sleep 120
pkill -f GALS.jar
sleep 1

diff -q /tmp/gals-01.txt /tmp/gals-02.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-03.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-04.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-05.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-06.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-07.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-08.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-09.txt || exit 1
diff -q /tmp/gals-01.txt /tmp/gals-10.txt || exit 1
