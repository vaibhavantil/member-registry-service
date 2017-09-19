#!/usr/bin/env bash

aws s3 cp s3://dev-com-hedvig-cluster-ett-data/kube ~/.kube --recursive

kubectl set image deployment/must-rename-deployment must-rename-deployment=$REMOTE_IMAGE_URL:$TRAVIS_BUILD_NUMBER
