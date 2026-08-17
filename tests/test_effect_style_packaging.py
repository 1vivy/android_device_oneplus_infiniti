# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0

"""Selecting the effect-stream backend obliges the device to ship its selector.

The vibrator service serves 110 waveform payloads across two style tiers only
because this device sets USE_EFFECT_STREAM. Without a packaged selector the
second tier is installed data the user can never reach - the same shape as the
ADFR floor daemon that was built, labelled, and then never installed.

Keyed on the coupling, not on a package headcount: dropping the selector while
the effect stream stays selected fails, and adding an unrelated package does not.
"""

import re
import unittest
from pathlib import Path

DEVICE = Path(__file__).resolve().parents[1]
EFFECT_STREAM = "soong_config_set_bool,OPLUS_LINEAGE_VIBRATOR_HAL,USE_EFFECT_STREAM,true"
SELECTOR = "OplusVibratorStyle"


def product_packages(text: str) -> set[str]:
    names: set[str] = set()
    for block in re.finditer(r"PRODUCT_PACKAGES\s*\+?=((?:[^\n]*\\\n)*[^\n]*)", text):
        for token in block.group(1).replace("\\", " ").split():
            names.add(token)
    return names


class EffectStylePackagingTest(unittest.TestCase):
    text: str = ""
    packages: set[str] = set()

    def setUp(self) -> None:
        self.text = (DEVICE / "device.mk").read_text(encoding="utf-8")
        self.packages = product_packages(self.text)

    def test_probe_sees_the_real_vibrator_selection(self) -> None:
        # Positive control: both halves of the premise are actually observable
        # here, so a silent parse failure cannot make the rule vacuous.
        self.assertIn("vendor.qti.hardware.vibrator.service.oplus", self.packages)
        self.assertIn(EFFECT_STREAM, self.text)

    def test_effect_stream_devices_package_the_style_selector(self) -> None:
        if EFFECT_STREAM not in self.text:
            self.skipTest("device does not select the effect-stream backend")
        self.assertIn(
            SELECTOR,
            self.packages,
            "USE_EFFECT_STREAM ships multiple style tiers; package the selector",
        )


if __name__ == "__main__":
    _ = unittest.main()
