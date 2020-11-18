#!/usr/bin/env bash

set -eu

SCALA_MAJOR_VERSION=$(echo $SCALA_VERSION | sed -E "s/([0-9]+)\.([0-9]+)\.([0-9]+)/\1.\2/")
SBT_JAR_DIR="/usr/src/app/backend/target/scala-${SCALA_MAJOR_VERSION}/"
ASSETS_DIR="${SBT_JAR_DIR}/assets"

rm -f /tmp/backend.pid
rm -f /tmp/frontend.pid
cd /usr/src/app/

# UTILS
log() {
  echo "$(date) - $@"
}

safe_kill() {
  PID=$1
  ps -o pid | grep -q $PID
  if [ ! $? -eq 0 ]; then
    log "WARNING: Process was not shutdown properly"
  else
    kill -9 $PID
  fi
}

# Migrations
migrate() {
  log "Starting migrations..."

  cd backend
  flyway migrate \
   -url="jdbc:${DB_DRIVER}://${DB_HOST}:${DB_PORT}/${DB_DATABASE}"\
   -user=${DB_USER} \
   -password=${DB_PASSWORD} \
   -locations="filesystem:/usr/src/app/backend/migrations" > ../migrations.log 2>&1
  
  cd ..
}

# SBT
backend() {
  log "Starting SBT watch..."
  
  if [ -f /tmp/backend.pid ]; then
    safe_kill $(cat /tmp/backend.pid)
    rm /tmp/backend.pid
  fi

  cd backend

  # Export the classpath
  sbt ~reStart > ../backend.log 2>&1 &
  echo $! > /tmp/backend.pid
  cd ..
}

# ROLLUP
frontend() {
  log "Starting JS watch..."
  mkdir -p $ASSETS_DIR

  if [ -f /tmp/frontend.pid ]; then
    safe_kill $(cat /tmp/frontend.pid)
    rm /tmp/frontend.pid
  fi

  cd frontend
  npm ci
  npm run dev > ../frontend.log 2>&1 &
  echo $! > /tmp/frontend.pid
  cd ..
  cp -R frontend/public/. ${ASSETS_DIR}/
}

# RELOADER
reload() {
  log "Starting reloader..."
  inotifywait -m -r -e create -e modify -e move -e delete --format "%w %f %e" frontend/public \
  | while read DIR FILE EVENT; do
    log "Change (${EVENT}) to ${DIR}${FILE} detected..."
    cp -R frontend/public/. ${ASSETS_DIR}/
  done
}

# TRAPS
cleanup() {
  if [ -f /tmp/backend.pid ]; then
    safe_kill -9 $(cat /tmp/backend.pid)
    rm /tmp/backend.pid
  fi
  if [ -f /tmp/frontend.pid ]; then
    safe_kill -9 $(cat /tmp/frontend.pid)
    rm /tmp/frontend.pid
  fi
  exit 0
}

# MAIN
main() {
  migrate
  backend
  frontend
  reload
}

trap cleanup SIGINT
trap cleanup SIGQUIT
trap cleanup SIGTERM

main
