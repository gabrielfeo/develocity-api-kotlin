#!/usr/bin/env python3

import requests
import fileinput
import sys
from read_current_api_spec_version import get_current_api_spec_version


def get_possible_version_bumps(version: str) -> list[str]:
    parts: list = version.split('.')
    possible_bumps = []
    for i in range(len(parts)):
        bump = parts.copy()
        bumped_part = int(bump[i]) + 1
        bump[i] = str(bumped_part)
        possible_bumps.append(".".join(bump))
    return possible_bumps


def get_first_available_version(versions: list[str]) -> str:
    for version in versions:
        response = requests.get(
            f"https://docs.gradle.com/enterprise/api-manual/ref/gradle-enterprise-{version}-api.yaml")
        if response.status_code == 200:
            return version


def update_version(properties_file, new_version):
    for line in fileinput.input(properties_file, inplace=True):
        if '=' in line:
            k, v = line.strip().split('=', maxsplit=2)
            if k == 'gradle.enterprise.version':
                line = f"{k}={new_version}\n"
        sys.stdout.write(line)


def main(properties_file):
    current = get_current_api_spec_version(properties_file)
    possible_bumps = get_possible_version_bumps(current)
    available_update = get_first_available_version(possible_bumps)
    if available_update:
        print(f"Updating to {available_update}")
        update_version(properties_file, available_update)
    else:
        print('No update available')


if __name__ == '__main__':
    main(properties_file='gradle.properties')
