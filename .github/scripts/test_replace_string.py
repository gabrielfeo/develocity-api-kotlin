#!/usr/bin/env python3
# pylint: disable=missing-function-docstring,missing-module-docstring,missing-class-docstring

from textwrap import dedent
import unittest
from unittest import mock
from pathlib import Path
from tempfile import TemporaryDirectory
from replace_string import replace_string

class TestReplaceString(unittest.TestCase):

    @mock.patch('git.Repo')
    def test_stable_to_stable(self, repo):
        self._test_replace_string(repo, [
            ('2024.1.0', '2024.2.0'),
            ('2024.1.0', '2024.1.1'),
            ('2024.4.2', '2025.1.1')
        ])

    @mock.patch('git.Repo')
    def test_stable_to_pre_release(self, repo):
        self._test_replace_string(repo, [
            ('2024.1.0', '2024.1.1-alpha01'),
            ('2024.1.0', '2024.1.1-beta01'),
            ('2024.1.0', '2024.2.0-alpha01'),
            ('2024.1.0', '2024.2.0-beta01'),
            ('2024.4.2', '2025.1.1-alpha01'),
        ])

    @mock.patch('git.Repo')
    def test_pre_release_to_stable(self, repo):
        self._test_replace_string(repo, [
            ('2024.1.0-alpha01', '2024.1.0'),
            ('2024.1.0-beta01', '2024.1.0'),
            ('2024.1.0-rc01', '2024.1.0'),
        ])

    @mock.patch('git.Repo')
    def test_pre_release_to_pre_release(self, repo):
        self._test_replace_string(repo, [
            ('2024.1.0-alpha01', '2024.1.0-alpha02'),
            ('2024.1.0-alpha02', '2024.1.0-beta01'),
            ('2024.1.0-beta01', '2024.1.1-rc01'),
        ])

    def _test_replace_string(self, repo, replacements):
        repo.return_value.ignored.return_value = False
        for old, new in replacements:
            with TemporaryDirectory() as temp_dir:
                target = Path(temp_dir)
                write_test_files(target, old)
                replace_string(target, old, new)
                for file in target.glob('**/*'):
                    self._assert_replaced(target / file, old, new)

    def _assert_replaced(self, file, old, new):
        content = file.read_text()
        self._assert_regular_versions_replaced(content, old, new)
        self._assert_badge_versions_replaced(content, old, new)

    def _assert_regular_versions_replaced(self, content, old, new):
        self.assertIn(new, content)
        self.assertNotIn(old, content)

    def _assert_badge_versions_replaced(self, content, old, new):
        if "badge/" not in content:
            return
        self.assertIn(
            f"badge/Maven%20Central-{new.replace('-', '--')}-blue", content)
        self.assertNotIn(
            f"badge/Maven%20Central-{old.replace('-', '--')}-blue", content)


def write_test_files(target_dir, old):
    (target_dir / 'README.md').write_text(dedent(f'''
        # Title

        [![Maven Central](https://img.shields.io/badge/Maven%20Central-{old.replace('-', '--')}-blue)][14]

        ```kotlin
        @file:DependsOn("com.gabrielfeo:develocity-api-kotlin:{old}")
        implementation("com.gabrielfeo:develocity-api-kotlin:{old}")
        ```

        ```
        %use develocity-api-kotlin(version={old})
        ```

        [14]: https://central.sonatype.com/artifact/com.gabrielfeo/develocity-api-kotlin/{old}
    '''))
    (target_dir / 'build.gradle.kts').write_text(dedent(f'''
        dependencies {'{'}
            implementation("com.gabrielfeo:develocity-api-kotlin:{old}")
        {'}'}
    '''))
    (target_dir / 'notebook.ipynb').write_text(dedent('''
        {
            "metadata": {},
            "nbformat": 4,
            "nbformat_minor": 2,
            "cells": [
                {
                    "cell_type": "code",
                    "execution_count": 1,
                    "source": [
                    "%use gradle-enterprise-api-kotlin(version={{VERSION}})"
                    ],
                    "outputs": []
                }
            ]
        }
    ''').replace('{{VERSION}}', old))


if __name__ == "__main__":
    unittest.main()
