#!/bin/bash
DOCKER_NAME="saga-consumer"

docker rm $DOCKER_NAME &&
docker rmi $DOCKER_NAME:1.0
