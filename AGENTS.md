# PROJECT KNOWLEDGE BASE
**Project:** android_device_oneplus_infiniti
**Seeded-at-ref:** oneplus/lineage-23.2
**Seeded-at-oid:** ba57c74670f92f0a8870dfe2ab003aecfa2628cd
**Seeded-at-date:** 2026-08-14
**Generated-policy-sha256:** 831fceec497e89cad3d18f57f71d7d9fbc2bf2498062efbc079821f326bd17f0

## UPSTREAM DISTILLATION

### Scope and ownership

This is the infiniti product-specific device tree. It selects device-only display, audio, regional, recovery, touch, vibrator, security, Plus Key, and overlay configuration. `device.mk` inherits the external composition paths `device/oneplus/sm8850-common/common.mk`, `vendor/oneplus/infiniti/infiniti-vendor.mk`, and `vendor/oneplus/camera-infiniti/camera-infiniti.mk`; implementations remain in their owning hardware or framework projects.

### Build graph and current delivery layout

- `lineage_infiniti.mk` is the lunch/product entry; `AndroidProducts.mk` advertises it and `BoardConfig.mk` supplies board-level composition.
- `device.mk` is the main product graph. It copies display/audio data, selects packages, sets typed Soong variables, and performs the inheritance edges.
- Root `Android.bp` exports the Soong namespace. Current local module entry points also exist under `overlay/` and `parts/PlusKey/`.
- `configs/display/` contains display config, panel-feature publisher, and ADFR floor inputs. `configs/audio/` is device calibration/policy data, not an audio HAL.
- `overlay/` targets platform/Oplus resources; `overlay-lineage/` targets Lineage components.
- `sepolicy/private/` and `sepolicy/vendor/` contain the delivery-parent policy additions for the panel publisher and SystemServer integration.
- `proprietary-files.txt`, `proprietary-firmware.txt`, `extract-files.py`, and `setup-makefiles.py` are one extraction workflow; generated vendor output is not hand-maintained here.

### Seed-only facts versus delivery-parent facts

At seeded base `ba57c74670f92f0a8870dfe2ab003aecfa2628cd`, device-local vibrator effect source existed at `vibrator/effect/`. That path does not exist at delivery parent `d396f62bd5ebeb0b08087c54a176c6b08f3bca34`; it is base-derived historical context, not current path guidance. The delivery parent instead selects `vendor.qti.hardware.vibrator.service.oplus` and uses `OPLUS_LINEAGE_VIBRATOR_HAL`. Regional files under `recovery/root/vendor/odm/etc/` exist at both seed and delivery parent and back the copy-file entries in `device.mk`.

### Interfaces, init, and sepolicy

The delivery tree declares no local AIDL/HIDL API. It composes AOD, display-panel publisher, health, KeyMint/Weaver, LiveDisplay, PowerShare, eUICC, touch, and vibrator packages from owner projects. New Binder services or HAL implementations belong with their implementation; add device VINTF/init/sepolicy declarations here only when product composition requires them.

### Extension precedents

Current Lineage/crDroid extension points are package selection and typed Soong configuration: `OPLUS_LINEAGE_LIVEDISPLAY_HAL`, `OPLUS_LINEAGE_TOUCH_HAL`, `OPLUS_LINEAGE_VIBRATOR_HAL`, `oplus_health`, and `recovery`. Follow those typed knobs and resource overlays before adding product properties or raw node handling.

### Conventions and verification

Upstream subjects use `infiniti: <imperative description>`. Preserve Apache-2.0 headers and backslash-continued make lists. For composition changes, inspect `device.mk`, the external inherited projects, and generated vendor makefiles together. Validate path existence, affected Soong modules, product graph, VINTF/init/policy joins, and extraction coherence rather than treating a makefile reference as runtime or filesystem proof.

## OUR DELTAS

None at seed. Later entries must name topic commit OIDs and must not rewrite upstream truth.
