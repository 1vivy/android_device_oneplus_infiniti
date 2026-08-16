from __future__ import annotations

from pathlib import Path

import pytest

from tests.verify_build_manifest_ninja import verify_query


def test_generated_edge_depends_on_absent_real_sentinel(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"
    query = (
        "out/product/etc/build-manifest.xml:\n"
        "  input: rule\n"
        f"    {sentinel}\n"
        "  outputs:\n"
        "    out/product.img\n"
    )

    verify_query(query, sentinel)


def test_generated_edge_without_sentinel_refuses(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"

    with pytest.raises(ValueError, match="missing sentinel input"):
        verify_query(
            "out/product/etc/build-manifest.xml:\n  input: rule\n",
            sentinel,
        )


def test_materialized_sentinel_refuses(tmp_path: Path) -> None:
    sentinel = tmp_path / ".build-manifest-source-state"
    sentinel.touch()

    with pytest.raises(ValueError, match="must remain absent"):
        verify_query(
            "\n".join(
                (
                    "out/product/etc/build-manifest.xml:",
                    "  input: rule",
                    f"    {sentinel}",
                )
            ),
            sentinel,
        )
