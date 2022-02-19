#!/bin/sh

# port를 변경하고 싶을 경우에는 8080:80 8443:443 으로 조정 가능
kubectl port-forward service/mydev-db-mytest-kafka 9092 -n mydev-db 
