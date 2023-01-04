#!/usr/bin/env python3

import requests


def get_current_api_version(properties_file) -> str:
    with open(properties_file, mode='r') as file:
        for line in file.readlines():
            if '=' not in line:
                continue
            k, v = line.strip().split('=', maxsplit=2)
            if k == 'gradle.enterprise.version':
                return v


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


def main(properties_file):
    current = get_current_api_version(properties_file)
    possible_bumps = get_possible_version_bumps(current)
    available_update = get_first_available_version(possible_bumps)
    if available_update:
        print(available_update)


if __name__ == '__main__':
    main(properties_file='gradle.properties')
