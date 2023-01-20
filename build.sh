#!/bin/bash
set -e

# Build and install the libraries
# abstracting away from using the
# RabbitMq message queue
pushd libs

pushd bank-service
./build.sh
popd

pushd messaging-utilities-3.3
./build.sh
popd

popd


pushd apps

pushd account
./build.sh
popd

pushd dtupay
./build.sh
popd

pushd transaction
./build.sh
popd

pushd token
./build.sh
popd

popd
