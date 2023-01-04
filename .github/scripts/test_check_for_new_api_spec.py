#!/usr/bin/env python3

from check_for_new_api_spec import *
from tempfile import NamedTemporaryFile
import unittest
from unittest import mock


class TestCheckForNewApiSpec(unittest.TestCase):

    def setUp(self):
        self.properties_file = NamedTemporaryFile()
        self.properties_file.write(b'gradle.enterprise.version=2022.4\n1=2\n')
        self.properties_file.flush()

    def tearDown(self):
        self.properties_file.close()

    @mock.patch('builtins.print')
    @mock.patch('requests.get')
    def test_main_many_updates_available(self, mock_get, mock_print):
        mock_get.return_value.status_code = 200
        main(self.properties_file.name)
        mock_print.assert_called_once_with('2023.4')
        mock_get.assert_called_once_with(
            'https://docs.gradle.com/enterprise/api-manual/ref/gradle-enterprise-2023.4-api.yaml'
        )

    @mock.patch('builtins.print')
    @mock.patch('requests.get')
    def test_main_no_updates_available(self, mock_get, mock_print):
        mock_get.return_value.status_code = 404
        main(self.properties_file.name)
        mock_print.assert_not_called()
        mock_get.assert_has_calls([
            mock.call(
                'https://docs.gradle.com/enterprise/api-manual/ref/gradle-enterprise-2023.4-api.yaml'),
            mock.call(
                'https://docs.gradle.com/enterprise/api-manual/ref/gradle-enterprise-2022.5-api.yaml'),
        ])


if __name__ == '__main__':
    unittest.main()
