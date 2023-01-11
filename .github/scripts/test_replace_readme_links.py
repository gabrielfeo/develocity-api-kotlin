#!/usr/bin/env python3

from replace_readme_links import main, JAVADOC_EXTERNAL_ROOT_URL
from tempfile import NamedTemporaryFile
import unittest


class TestCheckForNewApiSpec(unittest.TestCase):

    def write_readme(self, text):
        self.readme_file = NamedTemporaryFile()
        self.readme_file.write(text.encode())
        self.readme_file.flush()

    def assert_readme(self, expected):
        with open(self.readme_file.name) as file:
            self.assertEqual(file.read().strip(), expected.strip())

    def tearDown(self):
        self.readme_file.close()

    def test_main_replaces_javadoc_links_for_local_links(self):
        self.write_readme(f"""
                          [a]({JAVADOC_EXTERNAL_ROOT_URL}/a) unrelated [b][external]
                          unrelated text

                          [external]: {JAVADOC_EXTERNAL_ROOT_URL}/b""")
        main(self.readme_file.name, local_javadoc_root="./example")
        self.assert_readme(f"""
                          [a](file://./example/a) unrelated [b][external]
                          unrelated text

                          [external]: file://./example/b""")

    def test_main_preserves_non_javadoc_links(self):
        self.write_readme(f"""
                          [a]({JAVADOC_EXTERNAL_ROOT_URL}/a) unrelated [b][external]
                          [c][google] unrelated text
                          [d](https://google.com)

                          [google]: https://google.com
                          [external]: {JAVADOC_EXTERNAL_ROOT_URL}/b""")
        main(self.readme_file.name, local_javadoc_root="./example")
        self.assert_readme(f"""
                          [a](file://./example/a) unrelated [b][external]
                          [c][google] unrelated text
                          [d](https://google.com)

                          [google]: https://google.com
                          [external]: file://./example/b""")


if __name__ == '__main__':
    unittest.main()
