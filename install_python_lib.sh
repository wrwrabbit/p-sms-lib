#!/bin/bash
cp ./build/bin/python/releaseShared/libp_sms.so /usr/local/lib/
ldconfig
python3 ./src/pythonMain/python/setup.py install
