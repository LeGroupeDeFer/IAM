# -*- coding: utf-8 -*-

import json
from typing import Optional as Option, Dict, Any
from decimal import Decimal
from datetime import datetime
from re import sub

from requests import post, get, delete, Response

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

    def _handle_response(self, response: Response) -> Dict[str, Any]:
        content_type = response.headers.get('Content-Type', 'application/json')

        try:
            data = response.json() if content_type.startswith('application/json') else {}
        except:
            data = {}

        if response.status_code >= 400:
            raise ApiException(sub('[ ][ ]+', '\n', f"""
                api.register: Request to {response.url} failed.
                Code: {response.status_code}
                Reason: {data.get('reason', 'Not given')}
            """), response.status_code)

        return data

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

        return self._handle_response(r)

    def _get(
        self,
        endpoint: str,
        token: Option[str] = None
    ) -> Dict[str, Any]:

        headers = {}
        if token is not None:
            headers = {'Authorization': f'Bearer {token}'}

        r = get(self._uri(endpoint), headers=headers)

        return self._handle_response(r)

    def _delete(
        self,
        endpoint: str,
        token: Option[str] = None
    ) -> Dict[str, Any]:

        headers = {}
        if token is not None:
            headers = {'Authorization': f'Bearer {token}'}

        r = delete(self._uri(endpoint), headers=headers)

        return self._handle_response(r)

    def _login(self, username, password) -> str:
        log(f"{self.name} - Logging in as {username} on {self.server}")

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

        return r['access']

    # ---------------------------- Public Methods ----------------------------

    def sync(self, filling_rate: Decimal, time: datetime) -> bool:
        filling_rate_sent = float(filling_rate.quantize(Decimal('0.001')))
        data = {
            'time': time.strftime('%Y-%m-%d %H:%M:%S'),
            'fillingRate': filling_rate_sent
        }

        to_sign = f"""{{"time":"{data['time']}","fillingRate":{filling_rate_sent}}}"""
        try:
            log(f"{self.name} - Attempting to send payload to server...")
            self._post(f"/api/can/{self.name}/sync", {
                'data': data,
                'signature': self.crypto.sign(to_sign)
            })
            log(f"{self.name} - Success.")
            return True
        except ApiException as e:
            log(f"{self.name} - Failure (code {e.code}).")
            return False

    def publish(
        self,
        ids: (str, str),
        latitude: Decimal,
        longitude: Decimal,
    ) -> None:
        username, password = ids
        access_token = self._login(username, password)

        # Create the trash can
        log(f"{self.name} - Registering on {self.server}")
        self._post('/api/admin/can', {
            'id': self.name,
            'latitude': latitude.quantize(NANO),
            'longitude': longitude.quantize(NANO),
            'publicKey': self.crypto.b64_subject_info,
            'signProtocol': 'rsa'
        }, access_token)

    def host_version(self, ids: (str, str)):
        username, password = ids
        access_token = self._login(username, password)
        try:
            return self._get(f"/api/can/{self.name}", access_token)
        except ApiException as e:
            if e.code == 404:
                return None
            raise e

    def remove(self, ids: (str, str)):

        if self.host_version(ids) is None:
            return

        username, password = ids
        access_token = self._login(username, password)
        self._delete(f"/api/admin/can/{self.name}", access_token)
