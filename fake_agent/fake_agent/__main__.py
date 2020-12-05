#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
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


@cli.command()
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
def run(
    name: Option[str],
    directory: str,
) -> None:
    """Run the *agent_name* agent"""
    if name:
        agent = Agent.from_directory(join(directory, name))
        agent.start()
        agent.join()
    else:
        manager = AgentManager(directory)
        manager.start()


@cli.command()
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
    Generate a fake trash can and publish it to the specified server or
    localhost:80 if left unspecified.

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


if __name__ == '__main__':
    cli.main(args=sys.argv[1:], prog_name="fake_agent")
