# -*- coding: utf-8 -*-

from typing import Optional as Option, Union, Dict, Any
from base64 import b64encode
from multiprocessing import cpu_count
from os.path import join, expanduser, isabs
import re
import json

from rsa import newkeys, PrivateKey, PublicKey, sign as rsa_sign

from .errors import CryptoException


Key = Union[PrivateKey, PublicKey]


def _load_from_file(file_path: str) -> bytes:
    """
    Loads the agent PCKs key in PEM or DER format from *file_path*.

    Relative paths are resolved from the active user home.

    :param file_path: The file path of the key
    """

    resolved_file_path: str
    if isabs(file_path):
        resolved_file_path = file_path
    else:
        resolved_file_path = join(expanduser('~'), file_path)

    try:
        with open(resolved_file_path, 'rb') as fin:
            return fin.read()
    except OSError as e:
        raise CryptoException(f'crypto.load: {e.strerror}')


class Crypto(object):

    HASH_ALGORITHM: str                = 'SHA512'
    KEY_LENGTH:     int                = 2048
    KEY_FORMAT:     str                = 'PEM'
    _PRIVATE_KEY:   Option[PrivateKey] = None

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(self, private_key: Union[bytes, str], public_key: Union[bytes, str]):
        """
        Loads the agent PCKs key in PEM or DER format from *private_key*.

        If *private_key* is a byte representation of a private key, it will be
        loaded as is. If it is a string, an attempt will be made to read the
        file from the *private_key* path prior to loading it.

        Relative paths are resolved from the active user home.

        :param private_key: The private key bytes or file path
        """
        if isinstance(private_key, str):
            private_key = _load_from_file(private_key)
        self.private_key = PrivateKey.load_pkcs1(private_key, self.KEY_FORMAT)

        if isinstance(public_key, str):
            public_key = _load_from_file(public_key)
        self.public_key = PublicKey.load_pkcs1(public_key, self.KEY_FORMAT)

    # --------------------------- Private Methods ----------------------------

    @classmethod
    def _save_key(cls, key: Key, directory: str, file_name: str) -> str:
        pcks1 = key.save_pkcs1(cls.KEY_FORMAT)
        file = f'{file_name}.{cls.KEY_FORMAT.lower()}'
        path = join(directory, file)
        with open(path, 'wb') as out:
            out.write(pcks1)
        return path

    # ---------------------------- Public Methods ----------------------------

    @classmethod
    def generate(cls, name: str, directory: str) -> 'Crypto':
        """
        Generate the *name* keypair in *directory*.

        Relative paths are resolved from the active user home. If *set_active*
        is specified, the private key will be loaded for consecutive signature
        uses.

        :param name:       The keypair name
        :param directory:  The directory in which to save the keypair.
        :return: The (public, private) keypair paths
        """
        public, private = newkeys(cls.KEY_LENGTH, poolsize=cpu_count())

        resolved_directory: str
        if isabs(directory):
            resolved_directory = directory
        else:
            resolved_directory = join(expanduser('~'), directory)

        try:
            cls._save_key(private, resolved_directory, f'{name}')
            private_bytes = private.save_pkcs1(cls.KEY_FORMAT)

            cls._save_key(public, resolved_directory, f'{name}.pub')
            public_bytes = public.save_pkcs1(cls.KEY_FORMAT)

            return Crypto(private_bytes, public_bytes)
        except IOError as e:
            raise CryptoException(f'crypto.generate: {e.strerror}')

    def sign(self, data: Dict[Any, Any]) -> str:
        """
        Signs the payload with the agent private key. The key must have been
        loaded prior to this function call.

        :param data: The payload to be signed
        :return: The base64 encoded signature
        """

        payload = re.sub('[\n\t\r ]', '', json.dumps(data)).encode('utf-8')
        signature = rsa_sign(payload, self.private_key, self.HASH_ALGORITHM)

        return b64encode(signature).decode('utf-8')

    @property
    def b64_private_key(self) -> str:
        der = self.private_key.save_pkcs1('DER')
        return b64encode(der).decode('utf-8')

    @property
    def b64_public_key(self) -> str:
        der = self.public_key.save_pkcs1('DER')
        return b64encode(der).decode('utf-8')
