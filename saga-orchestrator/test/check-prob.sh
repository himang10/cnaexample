#!/usr/bin/env bash

while true; do curl localhost:9000/actuator/health/liveness | jq; curl localhost:9000/actuator/health/readiness | jq; sleep $1; done
