#!/bin/sh

pkill -f GALS.jar
sleep 1

gals server 10 &
sleep 1

gals client | head -n 400 > /tmp/gals-01.txt &
gals client | head -n 400 > /tmp/gals-02.txt &
gals client | head -n 400 > /tmp/gals-03.txt &
gals client | head -n 400 > /tmp/gals-04.txt &
gals client | head -n 400 > /tmp/gals-05.txt &
gals client | head -n 400 > /tmp/gals-06.txt &
gals client | head -n 400 > /tmp/gals-07.txt &
gals client | head -n 400 > /tmp/gals-08.txt &
gals client | head -n 400 > /tmp/gals-09.txt &
gals client | head -n 400 > /tmp/gals-10.txt &

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
