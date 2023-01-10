#!/usr/bin/env python3

from update_api_spec_version import main
from tempfile import NamedTemporaryFile
import unittest
from unittest import mock

JAVADOC_LINK_1 = "https://docs.gradle.com/enterprise/api-manual"
EXTERNAL_LINK = "https://google.com"


class TestCheckForNewApiSpec(unittest.TestCase):

    def write_readme(self, text):
        self.readme_file = NamedTemporaryFile()
        self.readme_file.write(text.encode())
        self.readme_file.flush()

    def tearDown(self):
        self.readme_file.close()

    @mock.patch('builtins.print')
    def test_main_replaces_javadoc_links_for_local_links(self, mock_print):
        self.write_readme(TODO)
        main(self.readme_file.name)
        # TODO Assert replaced links
        # TODO Assert printed replacements

    @mock.patch('builtins.print')
    def test_main_preserves_non_javadoc_links(self, mock_print):
        self.write_readme(TODO)
        main(self.readme_file.name)
        # TODO Assert replaced javadoc links
        # TODO Assert printed replacements
        # TODO Assert didn't replace non-javadoc links

    def assert_readme_content(self, expected):
        with open(self.readme_file.name) as file:
            self.assertEqual(file.read(), expected)

if __name__ == '__main__':
    unittest.main()
