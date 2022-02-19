#!/bin/bash
DOCKER_NAME="appmodern-bff"

docker rm $DOCKER_NAME &&
docker rmi $DOCKER_NAME:1.0
