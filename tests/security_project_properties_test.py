# SPDX-License-Identifier: Apache-2.0

import re
import unittest
from pathlib import Path

DEVICE_PATH = Path(__file__).resolve().parents[1]
PROJECT_PATH = DEVICE_PATH / "recovery/root/vendor/odm/etc"

PROV40 = "vendor.wv.oemcrypto.debug.enable_prov40"
IFAA_MODEL = "sys.oplus.ifaa.model"
PKI = "ro.vendor.oplus.provision.pki"
RKP_ONLY = (
    "remote_provisioning.strongbox.rkp_only",
    "remote_provisioning.tee.rkp_only",
)
STRONGBOX_IDENTITY = (
    "ro.strongbox.manufacturer",
    "ro.strongbox.model",
)
PROJECT_SECURITY_KEYS = (PROV40, IFAA_MODEL, PKI, *RKP_ONLY, *STRONGBOX_IDENTITY)


def parse_properties(path: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    for raw_line in path.read_text().splitlines():
        line = raw_line.strip()
        if not line or line.startswith(("#", "import ")):
            continue
        key, separator, value = line.partition("=")
        if not separator:
            raise AssertionError(f"invalid property line in {path}: {raw_line}")
        properties[key.strip()] = value.strip()
    return properties


def resolve_project(project: str) -> dict[str, str]:
    # Android gives ODM precedence over vendor. An explicitly empty ODM value is
    # the fail-closed sentinel and is exposed to consumers as an unset value.
    common_vendor = {PROV40: "true"}
    odm = parse_properties(DEVICE_PATH / "odm.prop")
    project_file = PROJECT_PATH / project / "build.default.prop"
    if project_file.is_file():
        odm.update(parse_properties(project_file))
    return {
        key: value
        for key in common_vendor | odm
        if (value := odm.get(key, common_vendor.get(key, "")))
    }


class SecurityProjectPropertiesTest(unittest.TestCase):
    def test_project_selector_is_machine_consumed(self) -> None:
        odm = (DEVICE_PATH / "odm.prop").read_text()
        self.assertIn("import /odm/etc/${ro.boot.prjname}/build.default.prop", odm)
        copied_projects: set[str] = set()
        copy_pattern = re.compile(
            r"recovery/root/vendor/odm/etc/(\d+)/build\.default\.prop:"
        )
        for match in copy_pattern.finditer((DEVICE_PATH / "device.mk").read_text()):
            copied_projects.add(match.group(1))
        self.assertEqual({"24831", "24863"}, copied_projects)

    def test_24831_uses_only_proven_security_identity(self) -> None:
        properties = resolve_project("24831")
        self.assertEqual("false", properties.get(PROV40))
        self.assertEqual("ONEPLUS-R24831", properties.get(IFAA_MODEL))
        self.assertNotIn(PKI, properties)
        for key in (*RKP_ONLY, *STRONGBOX_IDENTITY):
            self.assertNotIn(key, properties)

    def test_24863_uses_only_proven_security_identity(self) -> None:
        properties = resolve_project("24863")
        self.assertEqual("true", properties.get(PROV40))
        self.assertEqual("10", properties.get(PKI))
        self.assertNotIn(IFAA_MODEL, properties)
        for key in (*RKP_ONLY, *STRONGBOX_IDENTITY):
            self.assertNotIn(key, properties)

    def test_unknown_project_fails_closed(self) -> None:
        properties = resolve_project("99999")
        for key in PROJECT_SECURITY_KEYS:
            self.assertNotIn(key, properties)

    def test_no_project_security_identity_has_a_global_value(self) -> None:
        for filename in ("system_ext.prop", "vendor.prop"):
            properties = parse_properties(DEVICE_PATH / filename)
            for key in PROJECT_SECURITY_KEYS:
                self.assertNotIn(
                    key, properties, f"{key} must not be global in {filename}"
                )

    def test_stock_build_and_product_identities_remain_distinct(self) -> None:
        product = (DEVICE_PATH / "lineage_infiniti.mk").read_text()
        self.assertIn("OnePlus/CPH2745IN/OP611FL1:16/BP2A.250605.015/", product)
        self.assertIn("DeviceName=OP611FL1", product)
        self.assertIn("SystemDevice=OP611FL1", product)

        north_america = parse_properties(PROJECT_PATH / "24863/build.NA.prop")
        self.assertEqual("CPH2749", north_america.get("ro.product.odm.model"))
        self.assertEqual("CPH2749", north_america.get("ro.product.odm.name"))


if __name__ == "__main__":
    _ = unittest.main()
