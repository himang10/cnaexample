#!/bin/bash

kubectl delete cm saga-consumers-config -n mydev-ywyi
kubectl create cm saga-consumers-config --from-file=/Users/himang10/mydev/cnadata/examples/saga-consumer/src/main/resources.dev -n mydev-ywyi
