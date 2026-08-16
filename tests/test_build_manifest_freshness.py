from __future__ import annotations

from pathlib import Path

import pytest

from tests.verify_build_manifest_ninja import verify_makefile, verify_query

ROOT = Path(__file__).resolve().parent.parent


def test_makefile_uses_real_build_datetime_input() -> None:
    verify_makefile((ROOT / "Android.mk").read_text(encoding="utf-8"))


def test_generated_edge_depends_on_build_datetime(tmp_path: Path) -> None:
    build_datetime = tmp_path / "build_date.txt"
    build_datetime.touch()
    query = (
        "out/product/etc/build-manifest.xml:\n"
        "  input: rule\n"
        f"    {build_datetime}\n"
        "  outputs:\n"
        "    out/product.img\n"
    )

    verify_query(query, build_datetime)


def test_generated_edge_without_build_datetime_refuses(tmp_path: Path) -> None:
    build_datetime = tmp_path / "build_date.txt"
    build_datetime.touch()

    with pytest.raises(ValueError):
        verify_query(
            "out/product/etc/build-manifest.xml:\n  input: rule\n",
            build_datetime,
        )


def test_build_datetime_listed_only_as_output_refuses(tmp_path: Path) -> None:
    build_datetime = tmp_path / "build_date.txt"
    build_datetime.touch()
    query = "\n".join(
        (
            "out/product/etc/build-manifest.xml:",
            "  input: rule",
            "    out/other-input",
            "  outputs:",
            f"    {build_datetime}",
        )
    )

    with pytest.raises(ValueError):
        verify_query(query, build_datetime)


def test_missing_build_datetime_refuses(tmp_path: Path) -> None:
    build_datetime = tmp_path / "build_date.txt"
    query = (
        "out/product/etc/build-manifest.xml:\n"
        "  input: rule\n"
        f"    {build_datetime}\n"
    )

    with pytest.raises(ValueError):
        verify_query(query, build_datetime)
