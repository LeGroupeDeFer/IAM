#!/usr/bin/env bash

set -eu

cd /usr/src/app/

# UTILS
log() {
  echo "$(date) - $@"
}

safe_kill() {
    PID=$1
    ps -o pid | grep "$PID" >/dev/null
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
    -locations="filesystem:/usr/src/app/backend/migrations" > migrations.log 2>&1

  cd ..
}

# Scala
backend() {
  log "Starting Finch server..."

  cd backend
  sbt run > ../backend.log 2>&1 &
  echo $! > /tmp/backend.pid
  cd ..
}

# JS
frontend() {
  log "Starting JS watch..."
  
  cd frontend
  npm ci
  npm run dev > frontend.log 2>&1 &
  echo $! > /tmp/frontend.pid
  cd ..
  cp -R frontend/public/. backend/src/main/resources/
}

# TRAPS
cleanup() {
    if [ -f /tmp/backend.pid ]; then
      kill -9 $(cat /tmp/backend.pid)
      rm /tmp/backend.pid
    fi
    if [ -f /tmp/frontend.pid ]; then
      kill -9 $(cat /tmp/frontend.pid)
      rm /tmp/frontend.pid
    fi
    exit 0
}

trap cleanup SIGINT
trap cleanup SIGQUIT
trap cleanup SIGTERM

# RELOADER
reload() {
  log "Starting reloader..."
  inotifywait -m -r -e create -e modify -e move -e delete --format "%w %f %e" . \
  | while read DIR FILE EVENT; do

    log "Change (${EVENT}) to ${DIR}${FILE} detected..."
    
    # If there is a change to backend files, recompile
    if [[ $DIR =~ ^\./backend/src/main/scala/.* ]]; then
      if [ -f /tmp/backend.pid ]; then
        log "Terminating backend..."
        PID=$(cat /tmp/backend.pid)
        safe_kill $PID
      fi
      backend
    # If there is a change to frontend assets, copy those for backend access
    elif [[ $DIR =~ ^\./frontend/public/.* ]]; then
      cp -R frontend/public/. backend/src/main/resources/
    fi

  done
}

# MAIN
main() {
  migrate
  backend
  frontend
  reload
}

main