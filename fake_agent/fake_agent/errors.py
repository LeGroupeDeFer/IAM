# -*- coding: utf-8 -*-


class CryptoException(Exception):
    pass


class ApiException(Exception):
    def __init__(self, message: str, code: int):
        self.code = code
        super(ApiException, self).__init__(message)


class AgentException(Exception):
    pass
