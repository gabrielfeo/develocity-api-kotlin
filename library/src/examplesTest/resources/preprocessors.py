from nbconvert.preprocessors import Preprocessor
from traitlets import Unicode
import re


class ReplacePatternPreprocessor(Preprocessor):
    """
    Preprocessor that replaces lines in code cells matching a regex pattern
    with a replacement string, while keeping magic lines (e.g. '%use [...]')
    at the top, which is a requirement of the Kotlin kernel for Jupyter.
    The pattern and replacement can be set via config, allowing use for any regex replacement.
    """

    pattern = Unicode().tag(config=True)
    replacement = Unicode().tag(config=True)

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.did_replace = False

    def preprocess(self, nb, resources):
        super().preprocess(nb, resources)
        if not self.did_replace:
            raise ValueError(f"No replacements made with pattern: {self.pattern}")
        return nb, resources

    def preprocess_cell(self, cell, resources, cell_index):
        # Only process code cells
        if cell.cell_type != "code":
            return cell, resources

        if not isinstance(cell.source, str):
            raise ValueError("Cell source must be a string.")

        regex = re.compile(self.pattern)
        line_magics = []
        replaced = []
        for line in cell.source.splitlines(keepends=True):
            # Replace pattern with replacement
            new_lines = regex.sub(self.replacement, line).splitlines(keepends=True)
            for new_line in new_lines:
                if new_line.startswith('%'):
                    line_magics.append(new_line)
                else:
                    replaced.append(new_line)

        new_source = "".join(line_magics + replaced)
        if new_source != cell.source:
            self.did_replace = True
        cell.source = new_source
        return cell, resources
