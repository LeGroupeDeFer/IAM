#!/usr/bin/env bash

set -eu

if [ -f .env ]; then
  export $(cat .env | sed 's/#.*//g' | xargs)
fi

scp -i "$SSH_KEY" phidgets.py "${SSH_USER}@${RASPBERRY_HOSTNAME}:${DISTANT_WORKING_FOLDER}/phidgets.py"
