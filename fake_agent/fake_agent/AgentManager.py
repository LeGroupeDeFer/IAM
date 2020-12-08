# -*- coding: utf-8 -*-

import re
from os import listdir
from os.path import join, isdir
from signal import signal, SIGTERM, SIGINT, SIGHUP
from typing import Optional as Option

from .Agent import Agent
from .lib import resolve, log


class AgentManager(object):

    __slots__ = ['agents']

    def __init__(self, directory: str, host: Option[str] = None):
        directory = resolve(directory)
        host_regex = re.compile(host) if host else None
        self.agents = []
        agent_names = listdir(directory)

        for agent_name in agent_names:
            agent_directory = join(directory, agent_name)
            if not isdir(agent_directory):
                continue
            agent = Agent.from_directory(agent_directory)
            if host is None or host_regex.match(agent.host):
                self.agents.append(agent)

        super(AgentManager, self).__init__()

    def handle(self, signum, frame):
        for agent in self.agents:
            agent.running = False

    def start(self):

        signal(SIGTERM, self.handle)
        signal(SIGINT, self.handle)
        signal(SIGHUP, self.handle)

        for agent in self.agents:
            log(f"Agent {agent.name} is now a candidate")
            agent.start()
        for agent in self.agents:
            agent.join()

{"time":"2020-12-08 09:14:02","fillingRate":1.00}
{"time":"2020-12-08 09:14:02","fillingRate":1.00}