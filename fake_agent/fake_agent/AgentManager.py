# -*- coding: utf-8 -*-

from os import listdir
from os.path import join, isdir
from signal import signal, SIGTERM, SIGINT, SIGHUP

from .Agent import Agent
from .lib import resolve, log


class AgentManager(object):

    __slots__ = ['agents']

    def __init__(self, directory: str):
        directory = resolve(directory)
        self.agents = []
        agent_names = listdir(directory)

        for agent_name in agent_names:
            agent_directory = join(directory, agent_name)
            if not isdir(agent_directory):
                continue
            log(f"Agent {agent_name} is now a candidate")
            self.agents.append(Agent.from_directory(agent_directory))

        super(AgentManager, self).__init__()

    def handle(self, signum, frame):
        for agent in self.agents:
            agent.running = False

    def start(self):

        signal(SIGTERM, self.handle)
        signal(SIGINT, self.handle)
        signal(SIGHUP, self.handle)

        for agent in self.agents:
            agent.start()
        for agent in self.agents:
            agent.join()
