#!/usr/bin/env python3

import unittest
from pathlib import Path
from shutil import copytree
from tempfile import TemporaryDirectory
from replace_string import replace_string

TEST_RESOURCES = Path(__file__).parent / 'test_resources'

class TestReplaceString(unittest.TestCase):

    def test_replace_string(self):
        old = '0.17.0'
        new = '0.17.1'
        files = ('README.md', 'build.gradle.kts', 'notebook.ipynb')
        with TemporaryDirectory() as temp_dir:
            dir = Path(temp_dir) / 'resources'
            copytree(TEST_RESOURCES, dir)
            replace_string(dir, old, new)
            for file in files:
                self.assert_replaced(dir / file, old, new)

    def assert_replaced(self, file, old, new):
        content = file.read_text()
        self.assertIn(new, content)
        self.assertNotIn(old, content)

if __name__ == "__main__":
    unittest.main()
