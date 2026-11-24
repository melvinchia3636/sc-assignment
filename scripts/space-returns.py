#!/usr/bin/env python3
"""Insert a blank line before return statements in Java sources.

Skips a return when the line above is already blank, when it opens a
method/block (ends with "{"), when it is a case/default label (ends with ":"),
or when it is a comment attached to the return.
"""

import argparse
import pathlib
import re
import sys

RETURN = re.compile(r"^\s*return\b")
COMMENT = re.compile(r"^\s*(//|/\*|\*|\*/)")


def needs_blank_line(lines, index):
    if index == 0:
        return False
    previous = lines[index - 1]
    if not previous.strip():
        return False
    stripped = previous.strip()
    if stripped.endswith("{") or stripped.endswith(":"):
        return False
    if COMMENT.match(previous):
        return False
    return True


def format_file(path, check_only):
    original = path.read_text(encoding="utf-8")
    lines = original.splitlines(keepends=True)
    result = []
    inserted = 0

    for index, line in enumerate(lines):
        if RETURN.match(line) and needs_blank_line(lines, index):
            result.append("\n")
            inserted += 1
        result.append(line)

    updated = "".join(result)
    if inserted and not check_only:
        path.write_text(updated, encoding="utf-8")
    return inserted


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="*", type=pathlib.Path,
                        default=[pathlib.Path("src/main/java"),
                                 pathlib.Path("src/test/java")])
    parser.add_argument("--check", action="store_true",
                        help="report files that need changes without writing")
    args = parser.parse_args()

    files = []
    for path in args.paths:
        if path.is_dir():
            files.extend(sorted(path.rglob("*.java")))
        elif path.suffix == ".java":
            files.append(path)

    changed_files = 0
    changed_returns = 0
    for path in files:
        inserted = format_file(path, args.check)
        if inserted:
            changed_files += 1
            changed_returns += inserted
            print(f"{path}: {inserted} return statement(s)")

    verb = "would change" if args.check else "changed"
    print(f"{verb} {changed_returns} return statement(s) "
          f"in {changed_files} file(s)", file=sys.stderr)


if __name__ == "__main__":
    main()
