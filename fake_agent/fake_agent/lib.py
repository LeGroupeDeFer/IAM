# -*- coding: utf-8 -*-
import sys
from datetime import datetime
from random import uniform
from decimal import Decimal
from os.path import expanduser, isabs, join
from json import JSONEncoder
from typing import Dict, Any, Tuple

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


class DecimalEncoder(JSONEncoder):
    def default(self, o):
        if isinstance(o, Decimal):
            return float(str(o))
        return super(DecimalEncoder, self).default(o)


def random_coordinates(
    latitude: Decimal,
    longitude: Decimal,
    radius: Decimal
) -> (Decimal, Decimal):
    radius = min(radius, Decimal('360'))
    radius = float(radius)
    lat = latitude + Decimal(uniform(-radius, radius))
    lon = longitude + Decimal(uniform(-radius, radius))

    return lat.quantize(NANO), lon.quantize(NANO)


def resolve(path: str) -> str:
    if isabs(path):
        return path
    return join(expanduser('~'), path)


def log(*args):
    now = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
    print(f"{now} - INFO:", *args, file=sys.stderr)


def pluck(d: Dict[Any, Any], *keys: Any) -> Tuple[Any, ...]:
    return tuple(d.get(key) for key in keys)
