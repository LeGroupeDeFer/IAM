# -*- coding: utf-8 -*-

from decimal import Decimal
from random import uniform
from time import sleep
from typing import Optional as Option
from os.path import join, isfile, isdir, exists
from uuid import uuid1
from pathlib import Path
from shutil import rmtree
from enum import Enum
from datetime import datetime


from .Crypto import Crypto
from .Api import Api
from .Configuration import Configuration
from .errors import AgentException
from .lib import random_coordinates, resolve


class Agent(object):

    __slots__ = [
        'root', 'name', 'private_key_path', 'public_key_path',
        'certificate_path', 'crypto', 'api', 'mode', 'filling_rate', 'running'
    ]

    _FORMATS = [
        ('PEM', ''),
        ('PEM', '.pem'),
        ('x509', '.pem'),  # There is no reason for x509 to be PEM encoded
        ('DER', '.der')
    ]

    class Mode(Enum):
        LINEAR = 0

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(self, directory: str, name: str, mode: Mode = Mode.LINEAR):
        directory = resolve(directory)
        self.root = join(directory, name)
        self.name = name
        if not isdir(self.root):
            raise RuntimeError(f"Agent {name} not found in {directory}")

        self.private_key_path: Option[str] = None
        self.public_key_path : Option[str] = None
        self.certificate_path: Option[str] = None
        key_format = 'PEM'

        for fmt, ext in self._FORMATS:
            private_candidate = join(self.root, f"{name}{ext}")
            public_candidate  = join(self.root, f"{name}.pub{ext}")
            certificate_candidate = join(self.root, f"{name}.cert{ext}")
            if (isfile(private_candidate) and isfile(public_candidate)
                    and isfile(certificate_candidate)):
                self.private_key_path = private_candidate
                self.public_key_path = public_candidate
                self.certificate_path = certificate_candidate
                key_format = fmt

        if None in [self.public_key_path, self.private_key_path,
                    self.certificate_path]:
            raise RuntimeError(
                f"Unable to find private/public key or certificate in {self.root}"
            )

        Crypto.KEY_FORMAT = key_format
        self.crypto = Crypto(
            self.private_key_path, self.public_key_path, self.certificate_path
        )
        self.api = Api(self.crypto, name, Configuration['server'])

        self.mode = mode
        self.filling_rate = Decimal('0.0')
        self.running = True

    # ---------------------------- Static Methods ----------------------------

    @classmethod
    def generate(
        cls,
        ids: (str, str),
        location: (Decimal, Decimal, Decimal),
        name: str = None,
        directory: str = '/tmp',
        hostname: Option[str] = None,
        key_format: str = 'PEM',
        key_length: int = 2048,
        mode: Mode = Mode.LINEAR
    ) -> 'Agent':

        assert key_length in [2**x for x in range(9, 13)]

        name            = str(uuid1()) if name is None else name
        base            = directory
        directory       = join(resolve(directory), name)
        already_existed = exists(directory)

        try:
            Path(directory).mkdir(parents=True, exist_ok=True)
            crypto = Crypto.generate(
                name,
                hostname or name,
                directory,
                key_format=key_format.upper(),
                key_length=key_length
            )

            api = Api(crypto, name, Configuration['server'])
            lat, lon, radius = location
            latitude, longitude = random_coordinates(lat, lon, radius)
            api.register(ids, latitude, longitude)
        except Exception as e:
            if not already_existed:
                rmtree(directory)
            raise AgentException('Unable to generate agent') from e

        return Agent(base, name, mode)

    # --------------------------- Private Methods ----------------------------

    def _run_linear(self):
        while self.running:
            sleep(int(uniform(0, 120)))
            if self.filled:
                continue

            candidate = self.filling_rate + Decimal(uniform(-2.5, 5))
            self.filling_rate = max(Decimal('0'), min(Decimal('100'), candidate))
            self.api.sync(self.filling_rate, datetime.now())

    # ---------------------------- Public Methods ----------------------------

    @property
    def filled(self):
        return self.filling_rate >= Decimal('100')

    def run(self):
        self._run_linear()
