# IAM
A tool designed to help public waste management services monitor and empty public trash cans.

This project is project is being developed for the INFOM453 class (Laboratoire en **I**nformatique **A**mbiante et **M**obile) at [UNamur](https://www.unamur.be/en) (BE).

## Getting started

### Agent development

**IAM** uses the concept of agent to monitor trash cans. An agent is a device whose purpose is to monitor a single trash can state and to notify the server when changes occur. Insofar as this implementation goes, you may either go for the Raspberry route or the Arduino route. Both paths are now detailed.

#### Raspberry
##### Installation

Prerequisites:
1. Get a [Raspberry Pi](https://www.raspberrypi.org/), any version should be sufficent;
2. Get a **2Y0A21 F42** distance sensor;
3. Get a **2Y0A21 F08** distance sensor.

When the prerequisites are satisfied, proceed to the [phidget python library](https://www.phidgets.com/docs/Language_-_Python) installation on the raspberry:
```bash
mkdir -p libphidget22
wget -q https://www.phidgets.com/downloads/phidget22/libraries/linux/libphidget22.tar.gz -O - | gunzip -c - | tar xf - -C "${PWD}/libphidget22" --strip-components 1
cd libphidget22
./configure && make
sudo make install
sudo ldconfig
sudo cp "${PWD}/libphidget22/plat/linux/udev/99-libphidget22.rules" /etc/udev/rules.d/
```

After which you may have to reboot the raspberry. Once this is done you may proceed to the python dependencies installation:

```bash
cd ${PROJECT_ROOT}/raspberry
pip install -r requirements.txt
```

##### Callibration

Each and every sensor, even the most precise ones, have to be callibrated.
They cannot provide accurate result out of the box.

So one must take measurement of the output of the sensor depending on the real value that is measured.

You can find an exemple of this kind of measurement in the file `raspberry/data/raw_calibration_measurement.csv`.

Generally the sensors do respect some kind of proportionality between the real and measured unit (distance in this case).
So we just have to find the best fitting line in all the points that comes from the measurement.

You can find a python script that does that in the file `raspberry/calibration.py`.

Additionnaly, this script produce a graph of the measured data and the best fitting line and put the parameters in a file `parameters.json`.

![graph](raspberry/data/calibration_measurement.png)

Once the parameters of the line has been extracted (slope and y position at origin), you can use it to correct the sensor values.

So if 

```
f(x) = m * x + c
```

and we have `y`, `m` and `c`, we need to isolate `x`

```
x = (y - c)/m
```

In python this could be written as




#### Arduino
TODO

### Web development

#### With Docker:

Prerequisites:
1. Have [docker](https://docs.docker.com/engine/install/) installed;
2. Have [docker-compose](https://docs.docker.com/compose/install/) installed.

When the prerequisites are satisfied, run:

```bash
docker-compose up -d
```

You're done! Drink a cup of coffee while backend and frontend sources are compiled and then head to [localhost:8000](http://localhost:8000)!
The migrations, backend and frontend logs can be respectively found in `migrations.log`, `backend.log` and `frontend.log`.

#### Without Docker:

Prerequisites:
1. Have a [Java Runtime Environment](https://www.java.com/en/download/) version 8 or superior;
2. Have [SBT](https://www.scala-sbt.org/download.html) installed;
3. Have [NodeJS](https://nodejs.org/en/download/) and NPM installed;
4. Have a [MySQL](https://www.mysql.com/downloads/) or [MariaDB](https://mariadb.org/download/) server available.

##### Running migrations
When the prerequisites are satisfied, you may run the migrations with the following command:
```bash
cd ${PROJECT_ROOT}/backend
sbt "run migrate \
    --database-host $DB_HOST \
    --database-port $DB_PORT \
    --database-schema $DB_SCHEMA \
    --database-user $DB_USER \
    --database-password $DB_PASSWORD
"
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
mkdir backend/target/scala-2.12/assets
cp -R frontend/public/. backend/target/scala-2.12/assets
```
If you wish to automate the process you may either use a watcher such as inotify or simply link the two folders.
