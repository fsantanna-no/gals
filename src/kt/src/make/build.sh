#!/bin/sh

# EDIT:
# - Util.kt
# - run Tests.kt
# MAKE:
# - Rebuild / Artifacts
# - sudo make install
# - gals --version
# TEST:
# - make test
# EDIT:
# - build.sh
# - install.sh
# - README.md
#
# BUILD:
# $ ./build.sh
# $ ls -l *.zip
#
# UPLOAD:
# - https://github.com/fsantanna-no/gals/releases/new
# - tag    = <version>
# - title  = <version>
# - Attach = { .zip, install.sh }
#
# TEST
# $ cd /x/gals/bin/
# $ wget https://github.com/fsantanna-no/gals/releases/download/v0.2.0/install-v0.2.0.sh
# $ sudo sh install-v0.2.0.sh /usr/local/bin
# $ gals --version

VER=v0.2.0
DIR=/tmp/gals-build/

rm -Rf $DIR
rm -f  /tmp/gals-$VER.zip
mkdir -p $DIR

cp /usr/local/bin/GALS.jar /usr/local/bin/gals $DIR

cd /tmp/
zip gals-$VER.zip -r gals-build/

cd -
cp /tmp/gals-$VER.zip .
cp install.sh install-$VER.sh

ls -l
