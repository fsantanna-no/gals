#!/bin/sh

cat local.21-07.log local.27-07.log | grep XXX > local.xxx
lua5.4 x.lua local.xxx > local.x
lua5.4 y.lua local.x > local.csv


cat ether.24-07.log ether.28-07.log | grep XXX > ether.xxx
lua5.4 x.lua ether.xxx > ether.x
lua5.4 y.lua ether.x > ether.csv


