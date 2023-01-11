#!/usr/bin/env python3

import fileinput
import os
import sys


JAVADOC_EXTERNAL_ROOT_URL = "https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/"


def main(readme_file: str, local_javadoc_root: str):
    local_root_url = f"file://{local_javadoc_root}"
    with fileinput.input(readme_file, inplace=True) as file:
        for line in file:
            new_line = line.replace(JAVADOC_EXTERNAL_ROOT_URL, local_root_url)
            sys.stdout.write(new_line)


if __name__ == '__main__':
    main(readme_file='README.md',
         local_javadoc_root=os.path.realpath('./build/dokka/html/'))
