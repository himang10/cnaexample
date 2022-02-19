#!/usr/bin/env bash

while true ; do curl localhost:9091/actuator/health | jq; sleep $1; done
