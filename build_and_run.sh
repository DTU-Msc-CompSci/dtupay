#!/bin/bash
set -e
docker image prune -f
sleep 10

./build.sh

set +e

#detect docker version
docker-compose version
if [ $? != 0 ]; then
    echo "Executing docker compose up -d"
    docker compose down

    docker compose build --no-cache
    docker compose up -d
else
    echo "Executing docker-compose up -d"
    docker-compose down

    docker-compose build  --no-cache

    docker-compose up -d
fi

sleep 20
set -e

pushd client
./test.sh
popd

# Cleanup the build images
docker image prune -f

