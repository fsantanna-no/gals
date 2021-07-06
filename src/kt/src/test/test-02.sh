#!/bin/sh

pkill -f GALS.jar
sleep 1

gals server 10 &
sleep 1

gals client 50 9999 &
gals client 50 9998 &
gals client 50 9997 &
gals client 50 9996 &
gals client 50 9995 &
gals client 50 9994 &
gals client 50 9993 &
gals client 50 9992 &
gals client 50 9991 &
gals client 50 9990 &
sleep 1

gals app 9999 | head -n 1500 > /tmp/gals-01.txt &
gals app 9998 | head -n 1500 > /tmp/gals-02.txt &
gals app 9997 | head -n 1500 > /tmp/gals-03.txt &
gals app 9996 | head -n 1500 > /tmp/gals-04.txt &
gals app 9995 | head -n 1500 > /tmp/gals-05.txt &
gals app 9994 | head -n 1500 > /tmp/gals-06.txt &
gals app 9993 | head -n 1500 > /tmp/gals-07.txt &
gals app 9992 | head -n 1500 > /tmp/gals-08.txt &
gals app 9991 | head -n 1500 > /tmp/gals-09.txt &
gals app 9990 | head -n 1500 > /tmp/gals-10.txt &

sleep 120
pkill -f GALS.jar
sleep 1

diff /tmp/gals-01.txt /tmp/gals-02.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-03.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-04.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-05.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-06.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-07.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-08.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-09.txt || exit 1
diff /tmp/gals-01.txt /tmp/gals-10.txt || exit 1
