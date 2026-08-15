from __future__ import annotations

import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FRAMEWORK_CONFIG = (
    ROOT / "overlay" / "OPlusFrameworksResTarget" / "res" / "values" / "config.xml"
)


def _resource(name: str) -> ET.Element[str]:
    root = ET.parse(FRAMEWORK_CONFIG).getroot()
    matches = [element for element in root if element.attrib.get("name") == name]
    assert len(matches) == 1, f"{name}: expected one override, found {len(matches)}"
    return matches[0]


def test_display_white_balance_stays_hidden_without_measured_panel_primaries() -> None:
    availability = _resource("config_displayWhiteBalanceAvailable")
    assert availability.tag == "bool"
    assert availability.text == "false"


def test_measured_ambient_cct_sensor_input_remains_declared() -> None:
    sensor = _resource("config_displayWhiteBalanceColorTemperatureSensorName")
    assert sensor.tag == "string"
    assert sensor.text == "qti.sensor.rgb"
