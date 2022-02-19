#!/bin/bash

kubectl delete cm saga-orchestrators-config -n mydev-ywyi
kubectl create configmap saga-orchestrators-config --from-file=/Users/himang10/mydev/cnadata/examples/saga-orchestrator/src/main/resources.dev -n mydev-ywyi
