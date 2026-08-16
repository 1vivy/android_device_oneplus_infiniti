from __future__ import annotations

import argparse
import subprocess
from pathlib import Path
from typing import cast


def verify_query(query: str, sentinel: Path) -> None:
    if str(sentinel) not in query.splitlines():
        indented = f"    {sentinel}"
        if indented not in query.splitlines():
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
