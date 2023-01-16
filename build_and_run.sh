#!/bin/bash
set -e
docker image prune -f
sleep 10
docker-compose up -d
sleep 5


pushd client
./test.sh
popd

# Cleanup the build images
docker image prune -f

