#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import re
from os.path import join
from typing import Optional as Option
from decimal import Decimal

import click

from .Agent import Agent
from .AgentManager import AgentManager


@click.group()
@click.version_option(prog_name='fake_agent', version='0.2.0')
def cli() -> None:
    pass


@click.option(
    '-n', '--name',
    help='Specify a unique agent to run.',
    type=str
)
@click.option(
    '-d', '--directory',
    default='.fake_agent',
    help='The directory in which to load the agent(s).'
)
@click.option(
    '-h', '--host',
    help='Select only the agents belonging to a specific host.'
)
@cli.command()
def run(
    name: Option[str],
    directory: str,
    host: Option[str]
) -> None:
    """Run the specified agent(s)"""
    if name:
        agent = Agent.from_directory(join(directory, name))
        if host and agent.host != host:
            click.echo(f"Agent host {agent.host} does not match {host}")
            return
        agent.start()
        agent.join()
    else:
        manager = AgentManager(directory, host)
        manager.start()


@click.option(
    '-h', '--hostname',
    default='localhost',
    help='The IAM server IP or FQDN.'
)
@click.option(
    '--port',
    default=None,
    help='The port on which to contact the IAM server. Defaults to 80 or 443 if ssl is specified.',
    type=int
)
@click.option(
    '--ssl/--no-ssl',
    default=False,
    help='Whether to use a secure connection when contacting the IAM server.'
)
@click.option(
    '-u', '--username',
    prompt=True,
    help='Server administrator username.'
)
@click.option(
    '-p', '--password',
    prompt=True,
    help='Server administrator password.'
)
@click.option(
    '-n', '--name',
    help='The agent name.'
)
@click.option(
    '-s', '--speed',
    help='The speed factor, influences the time taken for an agent to fill up.',
    default="1.5:0.5"
)
@click.option(
    '-d', '--directory',
    default='.fake_agent',
    help='The directory in which to save the agent(s).'
)
@click.option(
    '-c', '--count',
    default=1,
    help='The number of generated agents.'
)
@click.argument('location')
@cli.command()
def generate(
    hostname: str,
    port: Option[int],
    ssl: bool,
    username: str,
    password: str,
    name: str,
    speed: float,
    directory: str,
    count: int,
    location: str,
) -> None:
    """
    Generate (a) fake trash can(s) and publish it/these to the specified server
    or localhost:80 if the server is left unspecified.

    LOCATION must be a 'latitude:longitude:radius' location. Specifies the
    geographical position around which the trash can shall be located.
    """
    if count < 1:
        return

    port = port if port else (443 if ssl else 80)

    lat, lon, radius = map(Decimal, location.split(':'))
    ids = (username, password)
    location_hint = (lat, lon, radius)
    speed, delta = map(float, speed.split(":"))

    if count == 1:
        agent = Agent.generate(ids, location_hint, name, directory, hostname)
        click.echo(f"Agent {agent.name} generated in {agent.root}!", err=True)
        click.echo(agent.name)
        return

    for n in range(1, count+1):
        agent = Agent.generate(
            ids, location_hint, f"{name}_{n}", directory, hostname, port, ssl,
            speed=(speed, delta)
        )
        click.echo(f"Agent {agent.name} generated in {directory}!", err=True)
        click.echo(agent.name)

    click.echo(f"Generated {count} agents", err=True)


@click.option(
    '-d', '--directory',
    default='.fake_agent',
    help='The directory in which to load the agent(s).'
)
@click.option(
    '-h', '--host',
    help='Select only the agents belonging to a specific host'
)
@cli.command()
def show(directory: str, host: Option[str]):
    """Lists the available agents"""

    host_regex = re.compile(host) if host else None
    manager    = AgentManager(directory)
    agents     = []
    hosts      = []

    for agent in manager.agents:
        if host is None or host_regex.match(agent.host):
            agents.append(agent)
            if agent.host not in hosts:
                hosts.append(agent.host)
            click.echo(f"{agent.name}@{agent.host}: speed={agent.speed}")

    click.echo(f"{len(agents)} agents on {len(hosts)} host(s).")


@click.option(
    '-d', '--directory',
    default='.fake_agent',
    help='The directory in which to load the agent(s).'
)
@click.option(
    '-h', '--host',
    help='Select only the agents belonging to a specific host.'
)
@click.option(
    '-n', '--name',
    help='The agent name.'
)
@click.option(
    '--stale/--no-stale',
    default=True,
    help='Whether to consider only agents which are no longer active.'
)
@click.option(
    '-u', '--username',
    prompt=True,
    help='Server administrator username.'
)
@click.option(
    '-p', '--password',
    prompt=True,
    help='Server administrator password.'
)
@cli.command()
def remove(directory, host, name, stale, username, password):
    """Remove the specified agent(s)"""
    ids = (username, password)
    if name:
        agent = Agent.from_directory(join(directory, name))
        if host and agent.host != host:
            click.echo(f"Agent host {agent.host} does not match {host}")
            return
        agent.remove(ids, stale)
    else:
        manager = AgentManager(directory, host)
        for agent in manager.agents:
            agent.remove(ids, stale)


if __name__ == '__main__':
    cli.main(args=sys.argv[1:], prog_name="fake_agent")
