# -*- coding: utf-8 -*-

from os import environ
from os.path import join, expanduser
from typing import Iterator

from dotenv import load_dotenv

from .lib import MetaDict
from .errors import ConfigurationException


class Configuration(metaclass=MetaDict):

    _items = {
        'host': 'localhost:8000',
        'ssl': False,
        'server': 'http://localhost:8000',
        'private_key': join(expanduser('~'), '.ssh', 'id_rsa'),
        'public_key': join(expanduser('~'), '.ssh', 'id_rsa.pub')
    }

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(self):
        raise NotImplementedError()

    # --------------------------- Private Methods ----------------------------

    # ---------------------------- Public Methods ----------------------------

    @classmethod
    def load(cls, **kwargs):
        load_dotenv()

        from_env: Iterator[(str, str)] = filter(
            lambda kv: kv[0] in cls,
            environ.items()
        )
        from_args: Iterator[(str, str)] = filter(
            lambda kv: kv[0] in cls,
            kwargs.items()
        )

        cls._items.update(dict(from_env))
        cls._items.update(dict(from_args))

        cls._items['ssl'] = bool(cls._items['ssl'])

        ssl, host = cls._items['ssl'], cls._items['host']
        cls._items['server'] = f"http{ssl and 's' or ''}://{host}"
