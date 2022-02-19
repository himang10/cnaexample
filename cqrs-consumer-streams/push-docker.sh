#!/bin/bash
DOCKER_NAME="cqrs-consumer-streams"
docker build --tag $DOCKER_NAME:1.0 . &&
docker tag $DOCKER_NAME:1.0 eks-dev-zdb-registry.cloudzcp.io/mydev-ywyi/$DOCKER_NAME:1.0 &&
docker push eks-dev-zdb-registry.cloudzcp.io/mydev-ywyi/$DOCKER_NAME:1.0
