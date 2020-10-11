# IAM
Laboratoire en Informatique Ambiante et Mobile

## Agent development

### Raspberry
TODO

### Arduino
TODO

## Web development

### Getting started

#### With Docker:

Prerequisites:
1. Have [docker](https://docs.docker.com/engine/install/) installed;
2. Have [docker-compose](https://docs.docker.com/compose/install/) installed.

When the prerequisites are satisfied, run:

```bash
docker-compose up -d
```

You're done and may now head to [localhost:8000](http://localhost:8000)!
The migrations, backend and frontend logs can be respectively found in `migrations.log`, `backend.log` and `frontend.log`.

#### Without Docker:

Prerequisites:
1. Have a [Java Runtime Environment](https://www.java.com/en/download/) version 8 or superior;
2. Have [SBT](https://www.scala-sbt.org/download.html) installed;
3. Have [Flyway Community Edition](https://flywaydb.org/download/) installed;
4. Have [NodeJS](https://nodejs.org/en/download/) and NPM installed;
5. Have a [MySQL](https://www.mysql.com/downloads/) or [MariaDB](https://mariadb.org/download/) server available.

##### Running migrations
When the prerequisites are satisfied, you may run the migrations with the following command:
```bash
flyway migrate \
    -url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_DATABASE}"\
    -user=${DB_USER} \
    -password=${DB_PASSWORD} \
    -locations="filesystem:${PROJECT_ROOT}/backend/migrations"
```
Where variables are replaced with the chosen installation details.

##### Running the development server
```bash
cd ${PROJECT_ROOT}/backend
sbt run
```
If everything was setup properly, you should now be able to reach the website at [localhost:8000](http://localhost:8000)

##### Compiling frontend assets
Executing the following
```bash
cd ${PROJECT_ROOT}/frontend
npm ci
npm run build
```
will compile frontend assets and output the result to the `public/build` directory. If you wish to recompile on every change, you may replace `npm run build` by `npm run dev`. When the assets have been prepared, we need to add the resulting files to the backend `resources` folder for server delivery:
```bash
cp -R frontend/public/. backend/src/main/resources/
```
If you wish to automate the process you may either use a watcher such as inotify or simply link the two folders.
