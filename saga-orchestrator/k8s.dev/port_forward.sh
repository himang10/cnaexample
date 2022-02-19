#!/bin/sh

# port를 변경하고 싶을 경우에는 8080:80 8443:443 으로 조정 가능
kubectl port-forward service/bff-service 9090:8080 9091:8081 -n mydev-ywyi 
