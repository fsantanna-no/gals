#!/bin/sh

X=x2.lua
Y=y2.lua

cat local.21-07.log local.27-07.log | grep XXX > local.xxx
lua5.4 $X local.xxx > local.x
lua5.4 $Y local.x > local.csv


cat ether.24-07.log ether.28-07.log | grep XXX > ether.xxx
lua5.4 $X ether.xxx > ether.x
lua5.4 $Y ether.x > ether.csv


cat inet.24-07.log inet.28-07.log | grep XXX > inet.xxx
lua5.4 $X inet.xxx > inet.x
lua5.4 $Y inet.x > inet.csv


