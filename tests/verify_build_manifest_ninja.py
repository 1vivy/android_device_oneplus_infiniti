from __future__ import annotations

import argparse
import subprocess
from pathlib import Path
from typing import cast


def verify_makefile(makefile: str) -> None:
    dependency = "$(INSTALLED_BUILD_MANIFEST_XML_TARGET): $(BUILD_DATETIME_FILE)"
    if dependency not in makefile.splitlines():
        raise ValueError("build manifest must depend on BUILD_DATETIME_FILE")


def verify_query(query: str, build_datetime: Path) -> None:
    lines = query.splitlines()
    input_index = next(
        (index for index, line in enumerate(lines) if line.startswith("  input:")),
        len(lines),
    )
    output_index = next(
        (
            index
            for index, line in enumerate(lines[input_index + 1 :], input_index + 1)
            if line.startswith("  outputs:")
        ),
        len(lines),
    )
    inputs = {line.strip() for line in lines[input_index + 1 : output_index]}
    if str(build_datetime) not in inputs:
        raise ValueError(
            f"generated build-manifest edge is missing build datetime input {build_datetime}"
        )
    if not build_datetime.is_file():
        raise ValueError(f"build datetime input does not exist: {build_datetime}")


def main() -> int:
    parser = argparse.ArgumentParser()
    _ = parser.add_argument("--ninja", required=True, type=Path)
    _ = parser.add_argument("--target", required=True)
    _ = parser.add_argument("--build-datetime", required=True, type=Path)
    args = parser.parse_args()
    ninja = cast(Path, args.ninja)
    target = cast(str, args.target)
    build_datetime = cast(Path, args.build_datetime)
    query = subprocess.run(
        ("ninja", "-f", str(ninja), "-t", "query", target),
        check=True,
        capture_output=True,
        text=True,
    ).stdout
    verify_query(query, build_datetime)
    print("build-manifest freshness edge: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
