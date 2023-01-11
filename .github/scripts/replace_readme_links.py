#!/usr/bin/env python3

import fileinput
import sys


JAVADOC_EXTERNAL_URL = "https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/"
JAVADOC_LOCAL_URL = "file:///Users/gabriel.feo/projects/gradle-enterprise-api-kotlin"\
                    "/build/dokka/html/"


def main(readme_file: str):
    with fileinput.input(readme_file, inplace=True) as file:
        for line in file:
            new_line = line.replace(JAVADOC_EXTERNAL_URL, JAVADOC_LOCAL_URL)
            sys.stdout.write(new_line)


if __name__ == '__main__':
    main(readme_file='README.md')
