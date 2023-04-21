#!/usr/bin/env python3

import requests
import re
import fileinput
import sys
from read_current_api_spec_version import get_current_api_spec_version

VERSIONS_URL = "https://docs.gradle.com/enterprise/api-manual/"
LATEST_VERSION_REGEX = r'<a [^>]*href="ref/gradle-enterprise-([\d.]+)-api\.yaml">Specification</a>'


def extract_latest_version():
    resp = requests.get(VERSIONS_URL)
    resp.raise_for_status()
    match = re.search(LATEST_VERSION_REGEX, resp.text)
    if not match:
        raise RuntimeError("Failed to retrieve latest version")
    return match.group(1)


def update_version(properties_file, new_version):
    for line in fileinput.input(properties_file, inplace=True):
        if '=' in line:
            k, v = line.strip().split('=', maxsplit=2)
            if k == 'gradle.enterprise.version':
                line = f"{k}={new_version}\n"
        sys.stdout.write(line)


def main(properties_file):
    current = get_current_api_spec_version(properties_file)
    latest = extract_latest_version()
    if current == latest:
        exit(1)
    update_version(properties_file, latest)
    print(latest)


if __name__ == '__main__':
    main(properties_file='gradle.properties')
