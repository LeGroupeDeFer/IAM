# -*- coding: utf-8 -*-

import random
from decimal import Decimal


NANO    = Decimal('0.000000001')
CENTI   = Decimal('0.01')
MILLI   = Decimal('0.001')


class MetaDict(type):

    _items = {}

    def __getitem__(cls, key):
        return cls._items[key]

    def __setitem__(cls, key, value):
        cls._items[key] = value

    def __contains__(cls, key):
        return key in cls._items


def random_coordinates(
    latitude: Decimal,
    longitude: Decimal,
    radius: Decimal
) -> (Decimal, Decimal):
    radius = min(radius, Decimal('360'))
    radius = float(radius)
    lat = latitude + Decimal(random.uniform(-radius, radius))
    lon = longitude + Decimal(random.uniform(-radius, radius))

    return lat.quantize(NANO), lon.quantize(NANO)
