#!/bin/bash
DOCKER_NAME="saga-orchestrators"

docker build --tag $DOCKER_NAME:1.0 . &&
docker run -i -t --name $DOCKER_NAME -p 8080:8080 $DOCKER_NAME:1.0
