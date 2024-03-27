#!/usr/bin/env fish

# USAGE:
#   source bin/use-kubeconfig.fish

set -lx KUBECONFIG ~/.lima/develocity/kubeconfig.yaml
kubectl config set-context --current --namespace=develocity
