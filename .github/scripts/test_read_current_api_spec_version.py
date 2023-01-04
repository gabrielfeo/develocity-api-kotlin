#!/usr/bin/env python3

from read_current_api_spec_version import main
from tempfile import NamedTemporaryFile
import unittest
from unittest import mock


class TestReadCurrentApiSpecVersion(unittest.TestCase):

    def setUp(self):
        self.properties_file = NamedTemporaryFile()
        self.properties_file.write(b'gradle.enterprise.version=2022.4\n1=2\n')
        self.properties_file.flush()

    def tearDown(self):
        self.properties_file.close()

    @mock.patch('builtins.print')
    def test_main(self, mock_print):
        main(self.properties_file.name)
        mock_print.assert_called_once_with('2022.4')


if __name__ == '__main__':
    unittest.main()
