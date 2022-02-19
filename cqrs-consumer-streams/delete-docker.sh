#!/bin/bash
DOCKER_NAME="cqrs-consumer-streams"

docker rm $DOCKER_NAME &&
docker rmi $DOCKER_NAME:1.0
