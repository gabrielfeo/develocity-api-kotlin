#!/usr/bin/env python3

import requests
import re
import fileinput
import sys

VERSIONS_URL = "https://docs.gradle.com/enterprise/api-manual/"
LATEST_VERSION_REGEX = r'<a [^>]*href="ref/develocity-([\d.]+)-api\.yaml">Specification</a>'


def main(properties_file='gradle.properties'):
    current = get_current_api_spec_version(properties_file)
    print(f"Current spec version in", properties_file, "is", current, file=sys.stderr)
    latest = extract_latest_version()
    if current == latest:
        exit(1)
    update_version(properties_file, latest)
    print(latest)


def get_current_api_spec_version(properties_file) -> str:
    with open(properties_file, mode='r') as file:
        for line in file.readlines():
            if '=' not in line:
                continue
            k, v = line.strip().split('=', maxsplit=2)
            if k == 'develocity.version':
                return v


def extract_latest_version():
    resp = requests.get(VERSIONS_URL)
    resp.raise_for_status()
    match = re.search(LATEST_VERSION_REGEX, resp.text)
    if not match:
        raise RuntimeError(f"Failed to get latest version from {VERSIONS_URL} \
                             with /{LATEST_VERSION_REGEX}/")
    print("First match in HTML of ", VERSIONS_URL, "is", match, file=sys.stderr)
    return match.group(1)


def update_version(properties_file, new_version):
    for line in fileinput.input(properties_file, inplace=True):
        if '=' in line:
            k, v = line.strip().split('=', maxsplit=2)
            # Update target API spec version
            if k == 'develocity.version':
                line = f"{k}={new_version}\n"
            # Update library version
            if k == 'version':
                line = f"{k}={new_version}.0\n"
        sys.stdout.write(line)


if __name__ == '__main__':
    main()
