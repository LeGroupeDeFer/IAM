#!/usr/bin/env python
import base64

from dotenv import load_dotenv
import os
import json
import rsa
import requests


class ConfigurationException(Exception):
    """
    Custom Exception for configuration problems
    """
    pass


def get_config(name: str, default: str = None) -> str:
    """
    Helper to get values stored as configuration in a .env file

    The .env file NEED to be in the same directory as this `utils.py` file.

    :param name: the name of the configuration value that we want
    :param default: a default value for this configuration value
    :return: the configuration wanted as a string
    """
    current_dir = os.path.dirname(os.path.abspath(__file__))
    configuration_file = f'{current_dir}/.env'

    if not os.path.isfile(configuration_file):
        raise ConfigurationException('The .env file is missing !!')

    load_dotenv(configuration_file)
    value = os.getenv(name)
    if value is not None:
        return value

    value = os.getenv(name.upper())
    if value is not None:
        return value

    if default is not None:
        return default

    raise ConfigurationException(f'Missing configuration value called "{name}".')


def is_debug() -> bool:
    """
    Simplified way to check if we are in debug mode or not

    :return: true if we are in debug mode, false otherwise
    """
    txt = 'something so improbable that we can assume that no one will use it as configuration for the debug config '
    return get_config('debug', txt) != txt


def sign(data: bytes) -> bytes:
    """
    Sign the python dict data with the private key stored in .env file with `private_key_path` name
        - The hash algorithm used is `SHA-512`

    The private key MUST be of `PEM` format, you should check in the `readme.md` file how to generate a private key

    :param data: The python dict that should be signed
    :return: The data signed by the private key
    """
    with open(get_config('private_key_path'), mode='rb') as pk_file:
        key_data = pk_file.read()
        private_key = rsa.PrivateKey.load_pkcs1(key_data)
    print(data)
    signed_data = rsa.sign(data, private_key, 'SHA-512')
    signed_encoded_data = base64.b64encode(signed_data)
    return signed_encoded_data


def purify(txt: str) -> bytes:
    return txt.replace(' ', '') \
        .replace("\n", '') \
        .replace("\t", '') \
        .replace("\r", '') \
        .encode('utf8')


def sync(data: dict) -> None:
    """
    Send data to the endpoint stored in the `sync_endpoint` config file
    This data is signed before being sent

    :param data: the data to be sent as json
    :return: None
    """
    data_as_json = purify(json.dumps(data))

    signature = sign(data_as_json).decode("utf-8")

    signed_data = {
        'signature': signature,
        'data': data
    }

    signed_data_as_json = purify(json.dumps(signed_data))

    headers = {'Content-type': 'application/json'}
    v = requests.post(get_config('sync_endpoint'), signed_data_as_json, headers=headers)
    if is_debug():
        print(v.content)
