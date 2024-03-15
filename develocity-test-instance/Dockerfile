FROM ubuntu:latest

RUN apt-get update \
  && apt-get install -y curl \
  && apt-get clean
RUN curl -sfL https://get.k3s.io | sh -
RUN sudo chown $UID /etc/rancher/k3s/k3s.yaml
RUN mkdir -p "${HOME}/.kube"
RUN ln -sf /etc/rancher/k3s/k3s.yaml "${HOME}/.kube/config"
RUN kubectl get ns
