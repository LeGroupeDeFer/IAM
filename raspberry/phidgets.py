#!/usr/bin/env python

from Phidget22.Devices.VoltageRatioInput import *
import json
from lib import sync
from datetime import datetime

# 0 : slider 60mm [P/N 1112(1)
# 1 : temperature
# 5 : IR reflective 10cm [P/N 1103(1)]
# 6 : (right) sharp [SHARP 2Y0A21 F42] <-> IR adapter [P/N 1101 (O)]
# 7 : (left) sharp [SHARP 2Y0A21 F08] <-> IR adapter [P/N 1101 (OA)]
channels = {
    6: '2Y0A21 F42',
    7: '2Y0A21 F08'
}


def reset_buffered_data() -> dict:
    return {6: [], 7: []}


last_sync = datetime.now().timestamp()
sync_interval = 30  # (sec)
buffered_data = reset_buffered_data()

with open('parameters.json') as json_file:
    data = json.load(json_file)


def on_sharp_change(self, sensor_value, _sensor_unit):
    global channels, data, last_sync, sync_interval, buffered_data

    model = channels[self.getChannel()]
    # if f(x) = m*x + c
    # and we get sensor_value as f(x) (or y)
    # to find x, we must calculate (y-c)/m
    corrected_value = (float(sensor_value) - data[model]['c']) / data[model]['m']
    buffered_data[self.getChannel()].append(corrected_value)

    # prevent sync overflow
    if last_sync + sync_interval <= datetime.now().timestamp():
        send_sync_message()
        buffered_data = reset_buffered_data()
        last_sync = datetime.now().timestamp()


def send_sync_message():
    global data, buffered_data, channels
    can_max_height = data['can_max_height']

    avg = 0
    for index, values in buffered_data.values():
        if channels[index] == '2Y0A21 F08':
            t = [v for v in values if v < 60]
            specific_avg = sum(t) / len(t)
        else:
            specific_avg = sum(values) / len(values)
        avg += specific_avg
    avg = avg / len(buffered_data)

    # get the filling rate between 0 and 1
    filling_rate = 1 - avg / can_max_height
    if filling_rate < 0:
        filling_rate = 0
    if filling_rate > 1:
        filling_rate = 1

    date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    data = {
        'time': date_time,
        'fillingRate': filling_rate
    }
    sync(data)


def main():
    sharp_distance_sensor_left = VoltageRatioInput()
    sharp_distance_sensor_left.setChannel(6)
    sharp_distance_sensor_left.setOnSensorChangeHandler(on_sharp_change)
    sharp_distance_sensor_left.openWaitForAttachment(5000)
    sharp_distance_sensor_left.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1101_SHARP_2Y0A21)

    sharp_distance_sensor_right = VoltageRatioInput()
    sharp_distance_sensor_right.setChannel(7)
    sharp_distance_sensor_right.setOnSensorChangeHandler(on_sharp_change)
    sharp_distance_sensor_right.openWaitForAttachment(5000)
    sharp_distance_sensor_right.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1101_SHARP_2Y0A21)

    try:
        input("Press Enter to Stop\n")
    except (Exception, KeyboardInterrupt):
        pass

    sharp_distance_sensor_left.close()
    sharp_distance_sensor_right.close()


main()
