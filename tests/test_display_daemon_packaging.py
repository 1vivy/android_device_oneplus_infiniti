# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0

"""A copied display config must have its consumer packaged.

`adfr-floor-daemon` is built by hardware/oplus and is the ONLY consumer of
`multimedia_display_adfr2minfps_config.xml`. The daemon was defined, given an
init .rc, an SELinux domain, an exec label and sysfs access, and its config was
copied into the image - but nothing named it in PRODUCT_PACKAGES, so Soong built
it and the product never installed it. The ADFR minimum-fps floor policy was
therefore dead code on the device while every surrounding artefact looked
correct.

This locks the coupling rather than the headcount: dropping either daemon from
PRODUCT_PACKAGES fails, and adding an unrelated package does not.
"""

import re
import unittest
from pathlib import Path

DEVICE = Path(__file__).resolve().parents[1]
CONSUMERS = {
    "configs/display/multimedia_display_adfr2minfps_config.xml": "adfr-floor-daemon",
    "configs/display/displaypanelfeature_publisher.xml": "displaypanelfeature-publisher",
}


def product_packages(text: str) -> set[str]:
    names: set[str] = set()
    for block in re.finditer(r"PRODUCT_PACKAGES\s*\+?=((?:[^\n]*\\\n)*[^\n]*)", text):
        for token in block.group(1).replace("\\", " ").split():
            names.add(token)
    return names


class DisplayDaemonPackagingTest(unittest.TestCase):
    def setUp(self) -> None:
        self.text = (DEVICE / "device.mk").read_text(encoding="utf-8")
        self.packages = product_packages(self.text)

    def test_probe_sees_a_real_package_list(self) -> None:
        # Positive control: without this, an empty parse would pass everything.
        self.assertIn("android.hardware.health-service.oplus", self.packages)

    def test_every_copied_display_config_has_its_consumer_packaged(self) -> None:
        for config, consumer in CONSUMERS.items():
            with self.subTest(config=config):
                if config not in self.text:
                    continue
                self.assertIn(
                    consumer,
                    self.packages,
                    f"{config} is installed but its consumer {consumer} is not packaged",
                )


if __name__ == "__main__":
    _ = unittest.main()
