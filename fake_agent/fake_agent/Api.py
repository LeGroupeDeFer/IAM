# -*- coding: utf-8 -*-

from typing import Optional as Option, Dict, Any
from decimal import Decimal
from datetime import datetime
from re import sub

import click
from requests import post

from .Crypto import Crypto
from .errors import ApiException
from .lib import NANO


class Api(object):

    __slots__ = ['crypto', 'identifier', 'server']

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(self, crypto: Crypto, identifier: str, server: str):
        self.crypto = crypto
        self.identifier = identifier
        self.server = server

    # ---------------------------- Static Methods ----------------------------

    # --------------------------- Private Methods ----------------------------

    def _uri(self, endpoint: str) -> str:
        if endpoint.startswith(self.server):
            return endpoint
        return f"{self.server}{endpoint}"

    def _post(
        self,
        endpoint: str,
        data: Dict[str, Any],
        token: Option[str] = None
    ) -> Dict[str, Any]:
        headers = {}
        if token is not None:
            headers = {'Authorization': f'Bearer {token}'}
        r = post(self._uri(endpoint), json=data, headers=headers)

        data = {}
        try:  # FIXME
            data = r.json()
        except Exception as _:
            pass

        if r.status_code >= 400:
            raise ApiException(sub('[ ][ ]+', '\n', f"""
                api.register: Request to {r.url} failed.
                Code: {r.status_code}
                Reason: {data.get('reason', 'Not given')}
            """))

        return data

    # ---------------------------- Public Methods ----------------------------

    def sync(self, filling_rate: Decimal, time: datetime) -> None:
        # time.astimezone().replace(microsecond=0).isoformat()
        data = {
            'time': time.strftime('%Y-%m-%d %H:%M:%S'),
            'fillingRate': filling_rate.quantize(Decimal('0.01'))
        }

        self._post(f"/api/can/{self.identifier}/sync", {
            'data': data,
            'signature': self.crypto.sign(data)
        })

    def register(
        self,
        ids: (str, str),
        latitude: Decimal,
        longitude: Decimal,
    ) -> None:
        username, password = ids

        # Login
        r = self._post('/api/auth/login', {
            'username': username,
            'password': password
        })
        refresh_token = r['token']

        # Refresh
        r = self._post('/api/auth/refresh', {
            'username': username,
            'token': refresh_token
        })
        access_token = r['access']

        # Create the trash can
        self._post('/api/admin/can', {
            'id': self.identifier,
            'latitude': latitude.quantize(NANO),
            'longitude': longitude.quantize(NANO),
            'publicKey': self.crypto.b64_subject_info,
            'signProtocol': 'rsa'
        }, access_token)
