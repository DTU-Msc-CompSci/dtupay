#!/bin/bash
set -e
docker image prune -f
sleep 10

./build.sh

#detect docker verison
docker-compose up -d
sleep 20


pushd client
./test.sh
popd

# Cleanup the build images
docker image prune -f

