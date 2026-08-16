from __future__ import annotations

import argparse
import subprocess
from pathlib import Path
from typing import cast


def verify_query(query: str, sentinel: Path) -> None:
    lines = query.splitlines()
    input_index = next(
        (index for index, line in enumerate(lines) if line.startswith("  input:")),
        None,
    )
    if input_index is None:
        raise ValueError("generated build-manifest edge has no input section")
    output_index = next(
        (
            index
            for index, line in enumerate(lines[input_index + 1 :], input_index + 1)
            if line.startswith("  outputs:")
        ),
        len(lines),
    )
    inputs = {line.strip() for line in lines[input_index + 1 : output_index]}
    if str(sentinel) not in inputs:
        raise ValueError(f"generated build-manifest edge is missing sentinel input {sentinel}")
    if sentinel.exists():
        raise ValueError(f"build-manifest sentinel must remain absent: {sentinel}")


def main() -> int:
    parser = argparse.ArgumentParser()
    _ = parser.add_argument("--ninja", required=True, type=Path)
    _ = parser.add_argument("--target", required=True)
    _ = parser.add_argument("--sentinel", required=True, type=Path)
    args = parser.parse_args()
    ninja = cast(Path, args.ninja)
    target = cast(str, args.target)
    sentinel = cast(Path, args.sentinel)
    query = subprocess.run(
        ("ninja", "-f", str(ninja), "-t", "query", target),
        check=True,
        capture_output=True,
        text=True,
    ).stdout
    verify_query(query, sentinel)
    print("build-manifest freshness edge: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
