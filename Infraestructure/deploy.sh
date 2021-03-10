#!/bin/bash
kubectl apply -f k8s/namespaces
kubectl apply -f k8s/secrets
kubectl apply -f k8s/deployments/influxdb
kubectl apply -f k8s/deployments/grafana-sensors
kubectl apply -f k8s/deployments/mqtt
kubectl apply -f k8s/deployments/openfaas
kubectl apply -f k8s/deployments/kube-prometheus-stack