#!/usr/bin/env python3

import argparse
from pathlib import Path

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("path", type=Path, help="Path to root directory")
    parser.add_argument("old_string", type=str, help="Old string to be replaced")
    parser.add_argument("new_string", type=str, help="New string to replace old string")
    args = parser.parse_args()
    replace_string(args.path, args.old_string, args.new_string)


def replace_string(path: Path, old_string: str, new_string: str) -> None:
    for file in path.glob('**/*'):
        if file.is_file():
            text = file.read_text()
            text = text.replace(old_string, new_string)
            file.write_text(text)


if __name__ == "__main__":
    main()
