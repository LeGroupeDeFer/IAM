# Raspberry

## Phidgets dependencies

Before being able to use the fidgets modules on the raspberry, one must follow this tutorial : https://www.phidgets.com/?view=articles&article=EducationTutorialStartPi

The big idea is to manually compile the phidgets sources.

It takes a lot of time !
```shell script
sudo apt-get install libusb-1.0-0-dev
sudo su pi
cd ~
wget https://www.phidgets.com/downloads/phidget22/libraries/linux/libphidget22.tar.gz
tar -xvf libphidget22.tar.gz
cd libphidget22-1.6.20200921
sudo su
./configure
make
make install
exit
sudo ldconfig
sudo cp /home/pi/libphidget22-1.6.20200921/plat/linux/udev/99-libphidget22.rules /etc/udev/rules.d/
sudo reboot
```

Then one can rely on the `requirements.txt` file to install the required python packages 
```shell script
pip install -r requirements.txt
```

## RSA keys

The private key MUST be of `PEM` format.
 
You should use the following commands to generate the key pair
```shell script
openssl genrsa -out rsa.private 2048
openssl rsa -in rsa.private -out rsa.public -pubout -outform PEM
```

## autostart

To be certain that the script is launched at the raspberry boot, you have to edit the cron jobs with a custom command.

So first create a script that will be our entrypoint with `vim /home/taz/playground/exec_phidgets_iot.sh`

```
#!/bin/bash

python /home/taz/playground/phidgets.py
```
Then you want to make that file executable with `chmod +x /home/taz/playground/.sh` 

Then you need to create a service, create a new file `vim /etc/systemd/system/phidget.service`

```
[Unit]
Description=IAM phidgets can thingy

[Service]
ExecStart=/home/taz/playground/exec_phidgets_iot.sh
Restart=on-failure
RestartSec=10
User=taz
WorkingDirectory=/home/taz/playground

[Install]
WantedBy=multi-user.target
```

After that you may want to
 
- check that the service is syntactically correct with `systemctl daemon-reload`
- check that the service can be launched with `systemctl start phidget`
- check the status of the service with `systemctl status phidget`

And finally you can enable the service at the boot of your server with
```
systemctl enable phidget
```
You also may want to test that everything is working as intended by rebooting your raspberry.


## Files

### `.env`

The `.env` file should contains every configuration value needed for the raspberry part of this project.

The file is ignored by git but every field appears at least once in the `.env.example` file, giving hints of what needs 
to appear in the real `.env` file

This file is used in
- `pu.sh`
- `lib.py`

### `pu.sh`

This file is intended to ease the deployment of the `phidgets.py` script to the raspberry.

The only thing done by this script is sending the local file to the raspberry server with `scp`.

### `requirements.txt`

Like most of the python project, you can use the `requirements.txt` file to bulk-import the needed python packages.
