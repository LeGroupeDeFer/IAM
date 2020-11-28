#!/usr/bin/env python
import json
import os
import pandas as pd
import matplotlib.pyplot as plt
from numpy import polyfit

plt.close('all')


def get_linear_function_param(name: str, df: pd.DataFrame) -> (float, float):
    """
    Take the name of the column for which we want to calculate the best line function

    A line function can be written like this : f(x) = m*x + c
    where m is the slope and c is the value of f(x) when x is zero.

    :param name: the name of the column of the dataframe for wich we want a function
    :param df: the dataframe containing the values
    :return: if a line function can be written as f(x) = m*x + c, this function return (m,c)
    """
    local_df = df[[name]]
    local_df = local_df[local_df[name] > 0.01]
    x = local_df.index.values
    y = local_df[name].values
    t = polyfit(x, y, 1)
    return t[0], t[1]


# from statistics import mean

file = os.path.join(os.path.dirname(__file__), "data/raw_calibration_measurement.csv")

df = pd.read_csv(file, sep=";")
avg = df.groupby(by='real').mean()

# avg.to_csv(os.path.join(os.path.dirname(__file__), "data/calibrated_avg.csv"), sep=";")


(m_42, c_42) = get_linear_function_param('2Y0A21 F42', avg)
col_f42 = [m_42 * x + c_42 for x in avg.index.values]
(m_08, c_08) = get_linear_function_param('2Y0A21 F08', avg)
col_f08 = [m_08 * x + c_08 for x in avg.index.values]

avg['calculated f42'] = col_f42
avg['calculated f08'] = col_f08
avg.plot()
plt.show()

data = {
    'can_max_height': 100,
    'min_diff_for_sync': 5,
    '2Y0A21 F42': {
        'm': m_42,
        'c': c_42
    },
    '2Y0A21 F08': {
        'm': m_08,
        'c': c_08
    }
}
with open(os.path.join(os.path.dirname(__file__), "data/parameters.json"), 'w') as f:
    json.dump(data, f)
