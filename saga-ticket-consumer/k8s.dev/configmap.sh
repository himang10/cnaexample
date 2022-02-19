#!/bin/bash

kubectl delete cm saga-ticket-consumers-config -n mydev-ywyi
kubectl create cm saga-ticket-consumers-config --from-file=/Users/himang10/mydev/cnadata/examples/saga-ticket-consumer/src/main/resources.dev -n mydev-ywyi
