#!/usr/bin/env python3

import argparse
from pathlib import Path
import sys
import git


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("path", type=Path, help="Path to root directory")
    parser.add_argument("old_string", type=str, help="Old string to be replaced")
    parser.add_argument("new_string", type=str, help="New string to replace old string")
    args = parser.parse_args()
    replace_string(args.path, args.old_string, args.new_string)


def replace_string(path: Path, old_string: str, new_string: str) -> None:
    repo = git.Repo(path, search_parent_directories=True)
    print(f'Replacing {old_string} for {new_string}...')
    for file in path.glob('**/*'):
        if not should_replace(repo, file):
            continue
        try:
            text = file.read_text()
            if old_string not in text:
                continue
            text = text.replace(old_string, new_string)
            file.write_text(text)
            print(f'Replaced in file {file}')
        except UnicodeError as e:
            print(f'Error processing file {file}:', e, file=sys.stderr)


def should_replace(repo, file):
    return file.is_file() and not repo.ignored(file) and file.parts[0] != '.git'


if __name__ == "__main__":
    main()
