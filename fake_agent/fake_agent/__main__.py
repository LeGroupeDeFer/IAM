#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import pathlib
import uuid
from decimal import Decimal

import click

from .Configuration import Configuration
from .Crypto import Crypto
from .Api import Api
from .lib import random_coordinates


@click.group()
@click.version_option(prog_name="fake_agent", version='0.1.0')
def cli() -> None:
    pass
    # Configuration.load()
    # Crypto.load()
    # Api.load()


@click.command()
@click.option('-h', '--host', default="localhost:8000", help="The 'server:port' encoded IP address or FQDN")
@click.option('--ssl/--no-ssl', default=False, help="Whether to use a secure connection when contacting the server")
@click.option('-d', '--directory', default="/tmp", help="The directory in which to laod the agent files")
def run(host, ssl, directory) -> None:
    """Run the *name* agent"""
    click.echo("run")


@click.command()
@click.option('-h', '--host', default="localhost:8000", help="The 'server:port' encoded IP address or FQDN")
@click.option('--ssl/--no-ssl', default=False, help="Whether to use a secure connection when contacting the server")
@click.option('-u', '--username', prompt=True, help="Server administrator username")
@click.option('-p', '--password', prompt=True, help="Server administrator password")
@click.option('-a', '--agent-name', default='auto', help="The agent identifier")
@click.option('-d', '--directory', default="/tmp", help="The directory in which generated trash can files shall be saved")
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

    # Crypto
    agent_name = str(uuid.uuid1()) if agent_name == 'auto' else agent_name
    target_directory = os.path.join(directory, agent_name)
    pathlib.Path(target_directory).mkdir(parents=True, exist_ok=True)
    crypto = Crypto.generate(agent_name, target_directory)

    # Api
    api = Api(crypto, agent_name, Configuration['server'])
    lat, lon, radius = location.split(':')
    latitude, longitude = random_coordinates(
        Decimal(lat), Decimal(lon), Decimal(radius)
    )
    api.register((username, password), latitude, longitude)


cli.add_command(run)
cli.add_command(generate)

if __name__ == '__main__':
    cli.main(args=sys.argv[1:], prog_name="fake_agent")
