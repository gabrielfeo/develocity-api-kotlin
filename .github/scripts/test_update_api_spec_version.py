#!/usr/bin/env python3

from update_api_spec_version import main
from tempfile import NamedTemporaryFile
import unittest
from unittest import mock

API_MANUAL = "https://docs.gradle.com/enterprise/api-manual"
ORIGINAL_VERSION = '2022.4'


class TestCheckForNewApiSpec(unittest.TestCase):

    def setUp(self):
        self.properties_file = NamedTemporaryFile()
        content = f"gradle.enterprise.version={ORIGINAL_VERSION}\n1=2\n"
        self.properties_file.write(content.encode())
        self.properties_file.flush()

    def tearDown(self):
        self.properties_file.close()

    @mock.patch('builtins.print')
    @mock.patch('requests.get')
    def test_main_many_updates_available(self, mock_get, mock_print):
        mock_get.return_value.status_code = 200
        main(self.properties_file.name)
        self.assert_updated_to('2023.0')
        self.assert_checked_for(['2023.0'], mock_get)

    @mock.patch('builtins.print')
    @mock.patch('requests.get')
    def test_main_no_updates_available(self, mock_get, mock_print):
        mock_get.return_value.status_code = 404
        main(self.properties_file.name)
        self.assert_updated_to(ORIGINAL_VERSION)
        self.assert_checked_for(['2023.0', '2022.5'], mock_get)

    def assert_updated_to(self, version):
        with open(self.properties_file.name) as file:
            self.assertEqual(file.read(),
                             f"gradle.enterprise.version={version}\n1=2\n")

    def assert_checked_for(self, versions, mock_get):
        mock_get.assert_has_calls(
            [mock.call(f"{API_MANUAL}/ref/gradle-enterprise-{v}-api.yaml") for v in versions])


if __name__ == '__main__':
    unittest.main()
