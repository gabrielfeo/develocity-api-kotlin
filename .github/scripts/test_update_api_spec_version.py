#!/usr/bin/env python3

from pathlib import Path
from update_api_spec_version import main
from tempfile import NamedTemporaryFile
import unittest
from unittest import mock

TEST_RESOURCES = Path(__file__).parent / 'test_resources'
TEST_API_MANUAL_HTML = TEST_RESOURCES / 'api_manual.html'
LATEST_VERSION = '2023.1'  # Same as HTML


class TestCheckForNewApiSpec(unittest.TestCase):

    @mock.patch('builtins.print')
    @mock.patch('requests.get')
    def test_main_with_update_available(self, mock_get, _):
        mock_get.return_value.status_code = 200
        mock_get.return_value.text = TEST_API_MANUAL_HTML.read_text()
        with self.properties_file(version='2022.4') as file:
            main(properties_file=file.name)
            self.assert_properties_version(file, LATEST_VERSION)

    @mock.patch('builtins.print')
    @mock.patch('requests.get')
    def test_main_without_update_available(self, mock_get, _):
        mock_get.return_value.status_code = 200
        mock_get.return_value.text = TEST_API_MANUAL_HTML.read_text()
        with self.properties_file(version=LATEST_VERSION) as file:
            with self.assertRaises(SystemExit):
                main(properties_file=file.name)
            self.assert_properties_version(file, LATEST_VERSION)

    def assert_properties_version(self, file, version):
        with open(file.name) as file:
            expected = f"gradle.enterprise.version={version}\n1=2\n"
            self.assertEqual(file.read(), expected)

    def properties_file(self, version):
        file = NamedTemporaryFile()
        content = f"gradle.enterprise.version={version}\n1=2\n"
        file.write(content.encode())
        file.flush()
        return file


if __name__ == '__main__':
    unittest.main()
