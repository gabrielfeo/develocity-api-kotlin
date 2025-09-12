#!/usr/bin/env python3
# pylint: disable=missing-function-docstring,missing-module-docstring

import argparse
from pathlib import Path
import re
import sys
import git


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("path", type=Path, help="Path to root directory")
    parser.add_argument("old", type=str, help="Old string to be replaced")
    parser.add_argument(
        "new", type=str, help="New string to replace old string")
    args = parser.parse_args()
    replace_string(args.path, args.old, args.new)


def replace_string(path: Path, old: str, new: str) -> None:
    repo = git.Repo(path, search_parent_directories=True)
    print(f'Replacing {old} for {new}...')
    for file in path.glob('**/*'):
        if not _should_replace_in(repo, file):
            continue
        try:
            text = file.read_text()
            text = _replace_badge_versions(text, old, new)
            text = _replace_non_badge_versions(text, old, new)
            file.write_text(text)
            print(f'Replaced in file {file}')
        except UnicodeError as e:
            print(f'Error processing file {file}:', e, file=sys.stderr)


def _replace_badge_versions(text: str, old: str, new: str) -> str:
    return re.sub(
        rf'''https://img\.shields\.io/badge/(.+?)-{_badge_version(old)}-(\w+)''',
        rf'''https://img.shields.io/badge/\1-{_badge_version(new)}-\2''',
        text
    )


def _replace_non_badge_versions(text: str, old: str, new: str) -> str:
    return re.sub(rf'(?!https://img\.shields\.io/badge/){old}', new, text)


def _badge_version(version: str) -> str:
    return version.replace('-', '--')


def _should_replace_in(repo, file: Path) -> bool:
    return file.is_file() \
        and not repo.ignored(file) \
        and file.parts[0] != '.git' \
        and file.name != 'test_replace_string.py'


if __name__ == "__main__":
    main()
