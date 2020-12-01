# -*- coding: utf-8 -*-

from typing import Optional as Option, Dict, Any
from decimal import Decimal
from datetime import datetime

import requests

from .Crypto import Crypto
from .errors import ApiException
from .lib import NANO


class Api(object):

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(self, crypto: Crypto, identifier: str, server: str):
        self.crypto = crypto
        self.identifier = identifier
        self.server = server

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
        r = requests.post(self._uri(endpoint), json=data, headers=headers)
        data = r.json()

        if r.status_code >= 400:
            raise ApiException(f"""
                api.register: Request to {r.url} failed.
                Code: {r.status_code}
                Reason: {data.get('reason', 'Not given')}
            """)

        return data

    # ---------------------------- Public Methods ----------------------------

    def sync(self, filling_rate: Decimal, time: datetime) -> None:
        data = {
            'fillingRate': filling_rate.quantize(Decimal('0.01')),
            'time': time.astimezone().replace(microsecond=0).isoformat()
        }

        self._post(f"/api/can/${self.identifier}/sync", {
            'data': data,
            'signature': self.crypto.sign(data),
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
            'publicKey': self.crypto.b64_public_key,
            'signProtocol': 'rsa'
        }, access_token)
