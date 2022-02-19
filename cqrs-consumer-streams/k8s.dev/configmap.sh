#!/bin/bash

kubectl delete cm cqrs-consumer-streams-config -n mydev-ywyi
kubectl create cm cqrs-consumer-streams-config --from-file=/Users/himang10/mydev/cnadata/examples/cqrs-consumer-streams/src/main/resources.dev -n mydev-ywyi
