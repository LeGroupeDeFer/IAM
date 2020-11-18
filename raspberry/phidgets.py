#!/usr/bin/env python
"""

"""
from Phidget22.Phidget import *
from Phidget22.Devices.VoltageRatioInput import *
import time

tab_last = ["" for _ in range(8)]
change_count = 0
units = ["%", "°C", "", "", "", "?", "cm", "cm"]


# 0 : slider 60mm [P/N 1112(1)
# 1 : temperature
# 5 : IR reflective 10cm [P/N 1103(1)]
# 6 : (right) sharp [SHARP 2Y0A21 F42] <-> IR adapter [P/N 1101 (O)]
# 7 : (left) sharp [SHARP 2Y0A21 F08] <-> IR adapter [P/N 1101 (OA)]


def on_sensor_change(self, sensor_value, sensor_unit):
    global change_count
    change_count += 1
    channel = self.getChannel()
    tab_last[channel] = str(sensor_value)

    if change_count % 10 == 0:
        print_all_information()
        if change_count > 1000000:
            change_count = 0


def print_all_information(only_unit=False):
    if only_unit:
        print("\t".join(filter(lambda t: t != "", units)))
    else:
        print("\t".join(filter(lambda v: v != "", tab_last)))
    pass


def main():
    slider_sensor = VoltageRatioInput()
    slider_sensor.setChannel(0)
    slider_sensor.setOnSensorChangeHandler(on_sensor_change)
    slider_sensor.openWaitForAttachment(5000)
    slider_sensor.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1112)

    temperature_sensor = VoltageRatioInput()
    temperature_sensor.setChannel(1)
    temperature_sensor.setOnSensorChangeHandler(on_sensor_change)
    temperature_sensor.openWaitForAttachment(5000)
    temperature_sensor.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1124)

    ir_reflective_sensor = VoltageRatioInput()
    ir_reflective_sensor.setChannel(5)
    ir_reflective_sensor.setOnSensorChangeHandler(on_sensor_change)
    ir_reflective_sensor.openWaitForAttachment(5000)
    ir_reflective_sensor.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1103)

    sharp_distance_sensor_left = VoltageRatioInput()
    sharp_distance_sensor_left.setChannel(6)
    sharp_distance_sensor_left.setOnSensorChangeHandler(on_sensor_change)
    sharp_distance_sensor_left.openWaitForAttachment(5000)
    sharp_distance_sensor_left.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1101_SHARP_2Y0A21)

    sharp_distance_sensor_right = VoltageRatioInput()
    sharp_distance_sensor_right.setChannel(7)
    sharp_distance_sensor_right.setOnSensorChangeHandler(on_sensor_change)
    sharp_distance_sensor_right.openWaitForAttachment(5000)
    sharp_distance_sensor_right.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1101_SHARP_2Y0A21)

    try:
        input("Press Enter to Stop\n")
        print_all_information(True)
    except (Exception, KeyboardInterrupt):
        pass

    slider_sensor.close()
    temperature_sensor.close()
    ir_reflective_sensor.close()
    sharp_distance_sensor_left.close()
    sharp_distance_sensor_right.close()


main()
