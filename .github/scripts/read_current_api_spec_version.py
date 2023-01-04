#!/usr/bin/env python3


def get_current_api_spec_version(properties_file) -> str:
    with open(properties_file, mode='r') as file:
        for line in file.readlines():
            if '=' not in line:
                continue
            k, v = line.strip().split('=', maxsplit=2)
            if k == 'gradle.enterprise.version':
                return v


def main(properties_file):
    print(get_current_api_spec_version(properties_file))


if __name__ == '__main__':
    main(properties_file='gradle.properties')
