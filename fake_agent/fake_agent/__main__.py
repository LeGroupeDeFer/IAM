#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
from decimal import Decimal

import click

from .Configuration import Configuration
from .Agent import Agent


@click.group()
@click.version_option(prog_name='fake_agent', version='0.2.0')
def cli() -> None:
    pass


@click.command()
@click.option(
    '-h', '--host',
    default='localhost:8000',
    help='The "server:port" encoded IP address or FQDN'
)
@click.option(
    '--ssl/--no-ssl',
    default=False,
    help='Whether to use a secure connection when contacting the server'
)
@click.option(
    '-d', '--directory',
    default='/tmp',
    help='The directory in which to load the agent files'
)
@click.argument('agent_name')
def run(host, ssl, directory, agent_name) -> None:
    """Run the *agent_name* agent"""
    Configuration.load(host=host, ssl=ssl)
    agent = Agent(directory, agent_name)
    agent.run()


@click.command()
@click.option(
    '-h', '--host',
    default="localhost:8000",
    help="The 'server:port' encoded IP address or FQDN"
)
@click.option(
    '--ssl/--no-ssl',
    default=False,
    help="Whether to use a secure connection when contacting the server"
)
@click.option(
    '-u', '--username',
    prompt=True,
    help="Server administrator username"
)
@click.option(
    '-p', '--password',
    prompt=True,
    help="Server administrator password"
)
@click.option(
    '-a', '--agent-name',
    help="The agent identifier"
)
@click.option(
    '-d', '--directory',
    default="/tmp",
    help="The directory in which generated trash can files shall be saved"
)
@click.argument('location')
def generate(host, ssl, username, password, location, agent_name, directory) -> None:
    """
    Generate a fake trash can and publish it to the server. Generated files
    related to the agent will be saved in the *directory* folder (resolved from
    $HOME if the path is relative).

    [LOCATION] must be a 'latitude:longitude:radius' location. Specifies the
    geographical position around which the trash can shall be located.
    """
    Configuration.load(host=host, ssl=ssl)
    lat, lon, radius = map(Decimal, location.split(':'))
    ids = (username, password)
    location = (lat, lon, radius)
    agent = Agent.generate(ids, location, agent_name, directory)
    click.echo(f"Agent {agent.name} generated in {agent.root}!", err=True)
    click.echo(agent.name)


cli.add_command(run)
cli.add_command(generate)

if __name__ == '__main__':
    cli.main(args=sys.argv[1:], prog_name="fake_agent")
