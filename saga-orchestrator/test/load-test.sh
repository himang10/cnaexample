#!/usr/bin/env bash

while true ; do curl "localhost:9090/cbtest?delay=4&faultPercent=50"; sleep $1; done
