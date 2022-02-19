#!/bin/bash
DOCKER_NAME="saga-ticket-consumer"

docker rm $DOCKER_NAME &&
docker rmi $DOCKER_NAME:1.0
