#!/usr/bin/env bash
set -euxo pipefail

docker build -t member-service:latest .
