from __future__ import annotations

from pathlib import Path

import pytest

from tests.verify_build_manifest_ninja import verify_query

ROOT = Path(__file__).resolve().parent.parent


def test_sentinel_rule_has_a_recipe() -> None:
    lines = (ROOT / "Android.mk").read_text(encoding="utf-8").splitlines()
    rule = lines.index("$(infiniti_build_manifest_sentinel):")

    assert lines[rule + 1].startswith("\t")


def test_generated_edge_depends_on_absent_real_sentinel(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"
    manifest_query = (
        "out/product/etc/build-manifest.xml:\n"
        "  input: rule\n"
        f"    {sentinel}\n"
        "  outputs:\n"
        "    out/product.img\n"
    )
    sentinel_query = f"{sentinel}:\n  input: rule\n  outputs:\n"

    verify_query(manifest_query, sentinel_query, sentinel)


def test_generated_edge_without_sentinel_refuses(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"

    with pytest.raises(ValueError):
        verify_query(
            "out/product/etc/build-manifest.xml:\n  input: rule\n",
            f"{sentinel}:\n  input: rule\n",
            sentinel,
        )


def test_sentinel_listed_only_as_output_refuses(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"
    query = "\n".join(
        (
            "out/product/etc/build-manifest.xml:",
            "  input: rule",
            "    out/other-input",
            "  outputs:",
            f"    {sentinel}",
        )
    )

    with pytest.raises(ValueError):
        verify_query(query, f"{sentinel}:\n  input: rule\n", sentinel)


def test_phony_sentinel_edge_refuses(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"
    manifest_query = (
        "out/product/etc/build-manifest.xml:\n"
        "  input: rule\n"
        f"    {sentinel}\n"
    )
    sentinel_query = f"{sentinel}:\n  input: phony\n"

    with pytest.raises(ValueError):
        verify_query(manifest_query, sentinel_query, sentinel)


def test_materialized_sentinel_refuses(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"
    sentinel.touch()

    with pytest.raises(ValueError):
        verify_query(
            "\n".join(
                (
                    "out/product/etc/build-manifest.xml:",
                    "  input: rule",
                    f"    {sentinel}",
                )
            ),
            f"{sentinel}:\n  input: rule\n",
            sentinel,
        )
