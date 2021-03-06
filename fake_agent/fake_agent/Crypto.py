# -*- coding: utf-8 -*-

import json
from typing import Optional as Option, Union, Dict, Any, Callable
from base64 import b64encode
from os.path import join
from re import sub
from datetime import datetime, timedelta

from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.hashes import HashAlgorithm, SHA512
from cryptography.x509 import (
    NameOID, load_pem_x509_certificate, load_der_x509_certificate
)
from cryptography.hazmat.primitives.asymmetric import rsa, ed25519, padding
from cryptography.hazmat.primitives.serialization import (
    Encoding, NoEncryption, PrivateFormat, PublicFormat, load_pem_private_key,
    load_pem_public_key, load_der_private_key, load_der_public_key
)
from .errors import CryptoException
from .lib import resolve, DecimalEncoder, log

PrivateKey = Union[rsa.RSAPrivateKeyWithSerialization, ed25519.Ed25519PrivateKey]
PrivateKeyReader = Callable[[bytes], PrivateKey]

PublicKey = Union[rsa.RSAPublicKeyWithSerialization, ed25519.Ed25519PublicKey]
PublicKeyReader = Callable[[bytes], PublicKey]


def _load_from_file(file_path: str) -> bytes:
    try:
        with open(resolve(file_path), 'rb') as fin:
            return fin.read()
    except OSError as e:
        raise CryptoException(f'crypto.load: {e.strerror}')


def generate_certificate(
    private_key: PrivateKey,
    hostname: str,
    name: str,
    hash_algorithm: HashAlgorithm,
    lifespan: int = 14
) -> x509.Certificate:

    x509_name     = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, name)])
    x509_dns_name = x509.DNSName(f'{name}.{hostname}')
    san           = x509.SubjectAlternativeName([x509_dns_name])
    constraints   = x509.BasicConstraints(ca=True, path_length=0)

    now           = datetime.now()

    return (
        x509.CertificateBuilder()
            .subject_name(x509_name)
            .issuer_name(x509_name)
            .public_key(private_key.public_key())
            .not_valid_before(now)
            .not_valid_after(now + timedelta(days=lifespan))
            .serial_number(1000)
            .add_extension(constraints, False)
            .add_extension(san, False)
            .sign(private_key, hash_algorithm, default_backend())
    )


def remove_pem_headers(key: bytes) -> bytes:
    return b''.join(key.splitlines()[1:-1])


class Crypto(object):

    __slots__ = [
        'private_key', 'public_key', 'certificate', 'key_format', 'key_length',
        'hash_algorithm'
    ]

    HASH_ALGORITHM: HashAlgorithm = SHA512()
    KEY_FORMAT    : Encoding = Encoding.PEM
    KEY_LENGTH    : int = 2048

    READERS = {
        Encoding.PEM: (load_pem_private_key, load_pem_public_key, load_pem_x509_certificate),
        Encoding.DER: (load_der_private_key, load_der_public_key, load_der_x509_certificate),
    }

    # ---------------------------- Dunder Methods ----------------------------

    def __init__(
        self,
        private_key: Union[bytes, str],
        public_key: Union[bytes, str],
        certificate: Union[bytes, str],
        key_format: Union[Encoding, str] = KEY_FORMAT,
        key_length: int = KEY_LENGTH,
        hash_algorithm: HashAlgorithm = HASH_ALGORITHM
    ):
        if type(key_format) is Encoding:
            self.key_format = key_format
        else:
            self.key_format = Encoding(key_format)
        self.key_length = key_length
        self.hash_algorithm = hash_algorithm

        if isinstance(private_key, str):
            private_key = _load_from_file(private_key)
        self.private_key: PrivateKey = self.READERS[key_format][0](
            private_key,
            password=None,
            backend=default_backend()
        )

        if isinstance(public_key, str):
            public_key = _load_from_file(public_key)
        self.public_key: PublicKey = self.READERS[key_format][1](
            public_key,
            backend=default_backend()
        )

        if isinstance(certificate, str):
            certificate = _load_from_file(certificate)
        self.certificate: x509.Certificate = self.READERS[key_format][2](
            certificate,
            default_backend()
        )

    # ---------------------------- Static Methods ----------------------------

    # --------------------------- Private Methods ----------------------------

    @classmethod
    def _save(
        cls,
        key: PrivateKey,
        cert: x509.Certificate,
        directory: str,
        name: str,
        key_format: Encoding
    ) -> ((str, bytes), (str, bytes), (str, bytes)):

        private_file = f'{name}.{key_format.name.lower()}'
        private_path = join(directory, private_file)
        private_bytes = key.private_bytes(
            key_format,
            PrivateFormat.PKCS8,
            NoEncryption()
        )

        public_file = f'{name}.pub.{key_format.name.lower()}'
        public_path = join(directory, public_file)
        public_bytes = cert.public_key().public_bytes(
            key_format,
            PublicFormat.PKCS1
        )

        cert_file = f'{name}.cert.{key_format.name.lower()}'
        cert_path = join(directory, cert_file)
        cert_bytes = cert.public_bytes(key_format)

        serialized = (
            (private_path, private_bytes),
            (public_path, public_bytes),
            (cert_path, cert_bytes)
        )

        for file_path, file_bytes in serialized:
            with open(file_path, 'wb') as out:
                out.write(file_bytes)

        return serialized

    # ---------------------------- Public Methods ----------------------------

    @classmethod
    def generate(
        cls,
        name: str,
        hostname: str,
        directory: str,
        hash_algorithm: Option[HashAlgorithm] = None,
        key_format: Option[Union[Encoding, str]] = None,
        key_length: Option[int] = None,
        lifespan: int = 14
    ) -> 'Crypto':
        hash_algorithm  = hash_algorithm or cls.HASH_ALGORITHM
        key_format      = key_format or cls.KEY_FORMAT
        key_length      = key_length or cls.KEY_LENGTH
        directory       = resolve(directory)

        if isinstance(key_format, str):
            try:
                key_format = Encoding(key_format)
            except ValueError as e:
                raise CryptoException(e.args[0]) from e

        log(f"Generating keypair of size {key_length}")
        private = rsa.generate_private_key(
            public_exponent=65537,
            key_size=key_length,
            backend=default_backend()
        )
        log(f"Generating certificate with name {name} valid for {lifespan} days")
        certificate = generate_certificate(
            private,
            hostname,
            name,
            hash_algorithm,
            lifespan
        )

        try:
            log(f"Saving private/public key and certificate in {directory}")
            priv, pub, cert = cls._save(
                private, certificate, directory, name, key_format
            )
            return Crypto(
                priv[1], pub[1], cert[1], key_format, key_length,
                hash_algorithm
            )
        except IOError as e:
            raise CryptoException(f'crypto.generate: {e.strerror}')

    def sign(self, data: Union[str, Dict[Any, Any]]) -> str:
        if isinstance(data, dict):
            data = json.dumps(data, cls=DecimalEncoder)
            payload = sub('[\n\t\r ]', '', data).encode('utf-8')
        else:
            payload = data.encode('utf-8')
        signature = self.private_key.sign(
            payload,
            padding.PKCS1v15(),
            SHA512()
        )

        return b64encode(signature).decode('utf-8')

    @property
    def b64_private_key(self) -> str:
        bytes = remove_pem_headers(self.private_key.private_bytes(
            Encoding.PEM,
            PrivateFormat.PKCS8,
            NoEncryption()
        ))
        return bytes.decode('utf-8')

    @property
    def b64_public_key(self) -> str:
        bytes = self.public_key.public_bytes(
            Encoding.PEM,
            PublicFormat.PKCS1
        )
        return bytes.decode('utf-8')

    @property
    def b64_subject_info(self) -> str:
        bytes = remove_pem_headers(self.public_key.public_bytes(
            Encoding.PEM,
            PublicFormat.SubjectPublicKeyInfo
        ))
        return bytes.decode('utf-8')

    @property
    def b64_certificate(self) -> str:
        bytes = remove_pem_headers(self.certificate.public_bytes(Encoding.PEM))
        return bytes.decode('utf-8')
