# -*- coding: utf-8 -*-

import json
from typing import Optional as Option, Dict, Any
from decimal import Decimal
from datetime import datetime
from re import sub

from requests import post

from .Crypto import Crypto
from .errors import ApiException
from .lib import NANO, log, DecimalEncoder


class Api(object):

    __slots__ = ['crypto', 'name', 'server']

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(
        self, crypto: Crypto,
        name: str,
        host: str,
        port: int,
        ssl: bool
    ):
        self.crypto = crypto
        self.name = name
        self.server = f"http{'s' if ssl else ''}://{host}:{port}"

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
        r = post(self._uri(endpoint), data=json.dumps(data, cls=DecimalEncoder), headers=headers)

        content_type = r.headers.get('Content-Type', 'application/json')
        try:
            data = r.json() if content_type.startswith('application/json') else {}
        except:
            data = {}

        if r.status_code >= 400:
            raise ApiException(sub('[ ][ ]+', '\n', f"""
                api.register: Request to {r.url} failed.
                Code: {r.status_code}
                Reason: {data.get('reason', 'Not given')}
            """))

        return data

    # ---------------------------- Public Methods ----------------------------

    def sync(self, filling_rate: Decimal, time: datetime) -> bool:
        data = {
            'time': time.strftime('%Y-%m-%d %H:%M:%S'),
            'fillingRate': filling_rate.quantize(Decimal('0.01'))
        }

        to_sign = f"""{{"time":"{data['time']}","fillingRate":{data['fillingRate']}}}"""
        try:
            log(f"{self.name} - Attempting to send payload to server...")
            self._post(f"/api/can/{self.name}/sync", {
                'data': data,
                'signature': self.crypto.sign(to_sign)
            })
            log(f"{self.name} - Success.")
            return True
        except ApiException:
            log(f"{self.name} - Failure.")
            return False

    def publish(
        self,
        ids: (str, str),
        latitude: Decimal,
        longitude: Decimal,
    ) -> None:
        username, password = ids

        log(f"{self.name} - Logging in as {ids[0]} on {self.server}")
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
        log(f"{self.name} - Registering on {self.server}")
        self._post('/api/admin/can', {
            'id': self.name,
            'latitude': latitude.quantize(NANO),
            'longitude': longitude.quantize(NANO),
            'publicKey': self.crypto.b64_subject_info,
            'signProtocol': 'rsa'
        }, access_token)
