#!/usr/bin/env python
"""

"""
from Phidget22.Phidget import *
from Phidget22.Devices.VoltageRatioInput import *
import time


def onSensorChange(self, sensorValue, sensorUnit):
    print("SensorValue [" + str(self.getChannel()) + "]: " + str(sensorValue))
    print("SensorUnit [" + str(self.getChannel()) + "]: " + str(sensorUnit.symbol))
    print("----------")


def main():
    voltageRatioInput0 = VoltageRatioInput()
    voltageRatioInput1 = VoltageRatioInput()

    voltageRatioInput0.setChannel(0)
    voltageRatioInput1.setChannel(1)

    voltageRatioInput0.setOnSensorChangeHandler(onSensorChange)
    voltageRatioInput1.setOnSensorChangeHandler(onSensorChange)

    voltageRatioInput0.openWaitForAttachment(5000)
    voltageRatioInput1.openWaitForAttachment(5000)

    voltageRatioInput0.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1124)
    voltageRatioInput1.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1124)

    try:
        input("Press Enter to Stop\n")
    except (Exception, KeyboardInterrupt):
        pass

    voltageRatioInput0.close()
    voltageRatioInput1.close()


main()
