#!/bin/bash
DOCKER_NAME="saga-orchestrators"
docker build --tag $DOCKER_NAME:1.0 . &&
docker tag $DOCKER_NAME:1.0 eks-dev-zdb-registry.cloudzcp.io/mydev-ywyi/$DOCKER_NAME:1.0 &&
docker push eks-dev-zdb-registry.cloudzcp.io/mydev-ywyi/$DOCKER_NAME:1.0
