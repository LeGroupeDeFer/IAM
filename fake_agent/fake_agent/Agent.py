# -*- coding: utf-8 -*-

import sys
import json
from decimal import Decimal
from random import uniform
from time import sleep
from typing import Optional as Option
from os.path import join, isfile, isdir, exists, basename
from uuid import uuid1
from pathlib import Path
from shutil import rmtree
from enum import Enum
from datetime import datetime
from threading import Thread

from cryptography.hazmat.primitives.serialization.base import Encoding

from .Crypto import Crypto
from .Api import Api
from .errors import AgentException
from .lib import random_coordinates, resolve, log, pluck


class Agent(Thread):

    __slots__ = [
        'root', 'name', 'private_key_path', 'public_key_path',
        'certificate_path', 'crypto', 'api', 'mode', 'filling_rate', 'running'
    ]

    # TODO - There is specific no reason for x509 to be PEM encoded
    _FORMATS = [
        ('PEM',  '',     Encoding.PEM),
        ('PEM',  '.pem', Encoding.PEM),
        ('x509', '.pem', Encoding.PEM),
        ('DER',  '.der', Encoding.DER)
    ]

    class Mode(Enum):
        LINEAR = 0

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(
        self,
        crypto: Crypto,
        api: Api,
        name: str,
        mode: Mode = Mode.LINEAR,
        speed: float = 0.5
    ):
        self.name         = name
        self.crypto       = crypto
        self.api          = api
        self.mode         = mode
        self.filling_rate = Decimal(uniform(0, 100))
        self.running      = False
        self.speed        = speed

        super(Agent, self).__init__()

    # ---------------------------- Static Methods ----------------------------

    @classmethod
    def generate(
        cls,
        ids: (str, str),
        location: (Decimal, Decimal, Decimal),
        name: str = None,
        directory: str = '/tmp',
        host: str = 'localhost',
        port: int = 80,
        ssl: bool = False,
        key_format: str = 'PEM',
        key_length: int = 2048,
        mode: Mode = Mode.LINEAR,
        speed: (float, float) = (1, 0.5)
    ) -> 'Agent':

        assert key_length in [2**x for x in range(9, 13)]

        name            = str(uuid1()) if name is None else name
        agent_directory = join(resolve(directory), name)
        already_existed = exists(agent_directory)

        try:

            Path(agent_directory).mkdir(parents=True, exist_ok=True)
            crypto = Crypto.generate(
                name,
                host,
                agent_directory,
                key_format=key_format.upper(),
                key_length=key_length
            )

            api = Api(crypto, name, host, port, ssl)
            lat, lon, radius = location
            latitude, longitude = random_coordinates(lat, lon, radius)
            mean, delta = speed
            actual_speed = uniform(mean, delta)
            api.publish(ids, latitude, longitude)

            with open(join(agent_directory, 'host'), 'w') as host_file:
                host_file.write(json.dumps({
                    'host': host,
                    'port': port,
                    'ssl': ssl,
                    'mode': mode.value,
                    'position': f"{latitude}:{longitude}",
                    'speed': actual_speed
                }))

        except Exception as e:
            if not already_existed:
                rmtree(agent_directory)
            raise AgentException('Unable to generate agent') from e

        return Agent(crypto, api, name, mode, actual_speed)

    @classmethod
    def from_directory(cls, directory):
        directory = resolve(directory)
        name = basename(directory)
        if not isdir(directory):
            raise RuntimeError(f"Agent {name} not found in {directory}")

        private_key_path: Option[str] = None
        public_key_path : Option[str] = None
        certificate_path: Option[str] = None
        key_format = 'PEM'

        for fmt, ext, encoding in cls._FORMATS:

            priv = join(directory, f"{name}{ext}")
            pub  = join(directory, f"{name}.pub{ext}")
            cert = join(directory, f"{name}.cert{ext}")

            if all(isfile(file_path) for file_path in [priv, pub, cert]):
                private_key_path = priv
                public_key_path  = pub
                certificate_path = cert
                key_format = encoding

        if None in [public_key_path, private_key_path, certificate_path]:
            raise RuntimeError(
                f"Unable to find private/public key or certificate in {directory}"
            )

        crypto = Crypto(
            private_key_path,
            public_key_path,
            certificate_path,
            key_format=key_format
        )

        with open(join(directory, 'host'), 'r') as host_file:
            conf = json.loads(host_file.read())
        host, port, ssl, mode, speed = pluck(
            conf, 'host', 'port', 'ssl', 'mode', 'speed'
        )
        api = Api(crypto, name, host, port, ssl)

        return cls(crypto, api, name, cls.Mode(mode), speed)

    # --------------------------- Private Methods ----------------------------

    def _run_linear(self):

        success = True
        attempts = 0

        while self.running:

            if not success and attempts < 3:

                success = self.api.sync(self.filling_rate, datetime.now())
                attempts += 1
                sleep(5)

            elif self.filled:

                log(f"{self.name} - Trash can is full, skipping.")
                sleep(5)

            else:

                candidate = self.filling_rate + Decimal(uniform(-2.5, 5))
                candidate = max(Decimal('0'), min(Decimal('100'), candidate))
                self.filling_rate = candidate
                log(f"{self.name} - Current filling rate: {self.filling_rate}")

                success = self.api.sync(self.filling_rate, datetime.now())
                attempts = 0 if success else attempts

                delay = int(uniform(0, 60 / self.speed))
                log(f"{self.name} - Next payload in {delay}s")
                sleep(delay)

    # ---------------------------- Public Methods ----------------------------

    @property
    def filled(self):
        return self.filling_rate >= Decimal('100')

    def run(self):
        log(f"{self.name} - Starting")
        self.running = True
        self._run_linear()
        log(f"{self.name} - Terminating")
