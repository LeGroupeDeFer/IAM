#!/usr/bin/env python
from typing import Optional

from Phidget22.Devices.VoltageRatioInput import *
import json
from lib import sync, is_debug
from datetime import datetime
import logging

logging.basicConfig(filename='phidgets.log', level=logging.INFO, format='[%(asctime)s] %(levelname)s\t%(message)s')

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
    return {5: [], 6: [], 7: []}


def load_data():
    with open('parameters.json') as json_file:
        return json.load(json_file)


last_sync = datetime.now().timestamp()
last_sync_filling_rate: Optional[float] = None
sync_interval = 10  # (sec)
buffered_data = reset_buffered_data()

data = load_data()


def get_corrected_value(model: str, value: float):
    global data
    corrected_value = None
    attempts = 0
    while corrected_value is None or attempts < 5:
        try:
            attempts += 1
            corrected_value = (float(value) - data[model]['c']) / data[model]['m']
        except KeyError:
            data = load_data()
    if corrected_value is None:
        corrected_value = 0
    return corrected_value


def get_max_height():
    global data
    height = None
    attempts = 0
    while height is None or attempts < 5:
        try:
            attempts += 1
            height = data['can_max_height']
        except KeyError:
            data = load_data()

    if height is None:
        height = 100
    return height


def get_min_change_for_sync():
    global data
    v = None
    attempts = 0
    while v is None or attempts < 5:
        try:
            attempts += 1
            v = data['min_diff_for_sync']
        except KeyError:
            data = load_data()
    if v is None:
        v = 5
    return v


def is_diff_since_last_change_large_enough(value: float) -> float:
    global last_sync_filling_rate
    if last_sync_filling_rate is None:
        return True

    threshold = get_min_change_for_sync()
    return abs(last_sync_filling_rate - value) > threshold


def on_ir_change(self, sensor_value, _sensor_unit):
    global last_sync, sync_interval, buffered_data

    is_less_than_10_cm = float(sensor_value) > 0.5
    buffered_data[5].append(is_less_than_10_cm)

    # prevent sync overflow
    if last_sync + sync_interval <= datetime.now().timestamp():
        send_sync_message()


def on_sharp_change(self, sensor_value, _sensor_unit):
    global channels, data, last_sync, sync_interval, buffered_data

    model = channels[self.getChannel()]
    # if f(x) = m*x + c
    # and we get sensor_value as f(x) (or y)
    # to find x, we must calculate (y-c)/m
    corrected_value = get_corrected_value(model, sensor_value)

    if is_debug():
        debug_info = f'[{channels[self.getChannel()]}] {sensor_value} -> {corrected_value}'
        logging.debug(debug_info)

    if (model == '2Y0A21 F08' and sensor_value < 60) or (model == '2Y0A21 F42'):
        buffered_data[self.getChannel()].append(corrected_value)


def send_sync_message():
    global data, buffered_data, channels, last_sync, last_sync_filling_rate
    can_max_height = get_max_height()
    if is_debug():
        logging.info("will send sync message")
        logging.debug("buffered data looks like this")
        logging.debug(f'{buffered_data}')

    avg = 0
    c = 0
    for index, name in channels.items():
        values = buffered_data[index]
        if len(values) > 0:
            c += 1
            avg += sum(values) / len(values)

    avg = 0.00000000001 if c == 0 else avg / c

    # get the filling rate between 0 and 1
    filling_rate = (can_max_height - avg) / can_max_height
    if filling_rate < 0:
        filling_rate = 0
    if filling_rate > 1:
        filling_rate = 1

    if len(buffered_data[5]) > 1 and sum(buffered_data[5]) > len(buffered_data) / 2:
        filling_rate = 0.99999

    if not is_diff_since_last_change_large_enough(filling_rate * 100):
        logging.info("Nothing will be sent because the difference between last sync is too small")
        buffered_data = reset_buffered_data()
        last_sync = datetime.now().timestamp()
        pass
    else:
        date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        data = {
            'time': date_time,
            'fillingRate': filling_rate * 100
        }
        sync(data)
        buffered_data = reset_buffered_data()
        last_sync = datetime.now().timestamp()
        last_sync_filling_rate = filling_rate * 100


def main():
    ir_reflective_sensor = VoltageRatioInput()
    ir_reflective_sensor.setChannel(5)
    ir_reflective_sensor.setOnSensorChangeHandler(on_ir_change)
    ir_reflective_sensor.openWaitForAttachment(5000)
    ir_reflective_sensor.setSensorType(VoltageRatioSensorType.SENSOR_TYPE_1103)

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

    while True:
        pass

    ir_reflective_sensor.close()
    sharp_distance_sensor_left.close()
    sharp_distance_sensor_right.close()


def super_robust_main():
    """
    kind of ugly way to be certain that the app is always running
    """
    try:
        main()
    except Exception:
        super_robust_main()


super_robust_main()
