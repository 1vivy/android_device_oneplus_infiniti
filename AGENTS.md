# android_device_oneplus_infiniti — agent carrier

<!-- rom-ops:carrier
project = "android_device_oneplus_infiniti"
seeded_at_ref = "oneplus/lineage-23.2"
seeded_at_oid = "ba57c74670f92f0a8870dfe2ab003aecfa2628cd"
date = "2026-08-15"
-->

## Upstream distillation

### Product and board graph

- `AndroidProducts.mk` exports the single product makefile `lineage_infiniti.mk`.
- `lineage_infiniti.mk` inherits, in order, AOSP `core_64_bit_only.mk`, AOSP `full_base_telephony.mk`, this tree's `device.mk`, and Lineage's `vendor/lineage/config/common_full_phone.mk`. It defines `lineage_infiniti` for OnePlus `infiniti`, model `CPH2745`, and supplies the `OP611FL1`/`CPH2745IN` Android 16 build description and fingerprint overrides plus the `android-oneplus` GMS client ID.
- `device.mk` is the device product layer. It sets normal/xxxhdpi AAPT configuration and a 1272x2772 boot-animation canvas; copies device audio, display, regional-property, and eUICC permission files; installs StrongBox KeyMint/Weaver, PowerShare, eSIM, overlay, and QTI vibrator packages; configures recovery DRM, Game Mode touch, LiveDisplay, and vibrator Soong variables; then inherits `device/oneplus/sm8850-common/common.mk` and generated `vendor/oneplus/infiniti/infiniti-vendor.mk`.
- `BoardConfig.mk` is deliberately a thin specialization over `device/oneplus/sm8850-common/BoardConfigCommon.mk`. Before that include it selects a prebuilt kernel by default and sets the 17,062,428,672-byte super partition; after it, it sets `DEVICE_PATH`, accepts OTA device IDs `OP60FFL1,OP611FL1`, uses density 540, selects `device/oneplus/infiniti-kernel/BoardConfig.mk` or `CONFIG_INFINITI_DTB=y`, registers the three property files, sets a 103-pixel recovery UI margin, and includes `vendor/oneplus/infiniti/BoardConfigVendor.mk`.
- `lineage.dependencies` declares `android_device_oneplus_sm8850-common` at `device/oneplus/sm8850-common`; the kernel and proprietary vendor trees are referenced by make includes rather than this dependency file. `board-info.txt` admits board names `canoe`, `OP60FFL1`, and `OP611FL1`.
- Root `Android.bp` opens a Soong namespace. `device.mk` adds that namespace so the local `vibrator/effect/Android.bp` module is visible.
- `vibrator/effect/Android.bp` builds vendor shared library `libqtivibratoreffect.oplus.infiniti`; `effect.cpp`, `standard_effect.h`, and `primitive_effect.h` provide the QTI effect-stream lookup tables selected by normal versus high-bit-tagged primitive IDs. `device.mk` points `vendor.qti.hardware.vibrator.service` at this library.
- `extract-files.py` uses `ExtractUtils.device_with_common(..., "sm8850-common", ...)`, imports the Oplus, SM8850 CAF, common-vendor, and QTI display namespaces, emits firmware from `proprietary-firmware.txt`, applies a vendor suffix to `libhcsutils`, and carries targeted camera, NFC, graphics-ABI, C++ runtime, and tinyxml fixups. `setup-makefiles.py` is its regenerate-makefiles shim.

### Device data and packages

- `configs/audio/audio_module_config_primary.xml` defines the default primary audio module: fast/raw/deep-buffer/MMAP/offload/spatial/telephony playback, primary/fast/raw/MMAP/hotword/VoIP/4-channel-AEC capture, and routes for speaker/earpiece, analog, USB, HDMI, FM, telephony, proxy/IP, Bluetooth SCO/A2DP/LE, haptics, and bit-perfect paths. `configs/audio/audio_policy_volumes.xml` supplies stream/device-category curves, while `configs/audio/default_volume_tables.xml` supplies their reusable full-scale, silent, voice, media, system, speaker, headset, hearing-aid, and non-mutable references. All three are copied to `vendor/etc` by `device.mk`.
- `configs/display/displayconfig.xml` is copied to both known panel display IDs, `4630946903293830803` and `4630946982335253651`. At the pin it maps 1080x2354 to density 450 and 1272x2772 to density 560, and enables HBM above 0.85 with 10,000-lux entry, a 30-minute accounting window, 5-minute maximum, 1-minute minimum, and 10% HDR-screen threshold.
- The six files under `recovery/root/vendor/odm/etc/` become project `24831` and `24863` regional property files. Project 24831 identifies `PLK110`/CN and its NFC config. Project 24863 has EU, IN, NA, and ROW model/name/region overrides; its default imports the hardware-revision property file, enables eSIM feature level 2, and selects the 24863 NFC config.
- `proprietary-files.txt` is the generated-vendor inventory, sourced by default from OnePlus 15 `CPH2745_16.0.9.400(EX01)`. Its sections cover ACDB/ADSP, ANT+, audio, camera, charging, display and Dolby Vision, DSP, eUICC, EVA/fingerprint/GPU/VPU firmware, neural networks, NFC, sensors, touch, UFS, and Wi-Fi. Entries use extract-utils annotations such as `MODULE_SUFFIX`, `FIX_SONAME`, `DISABLE_DEPS`, and `PRESIGNED`. `proprietary-firmware.txt` separately lists A/B boot-chain, modem, DSP, security, and platform firmware images.

### Overlays

- `overlay-lineage/` is a path overlay enabled through `DEVICE_PACKAGE_OVERLAYS`. `overlay-lineage/lineage-sdk/lineage/res/res/values/config.xml` reports wireless charging; `overlay-lineage/packages/apps/Aperture/app/src/main/res/values/config.xml` exposes auxiliary cameras, ignores camera ID 0 in that selector, and adds explicit high-frame-rate modes for IDs 0, 1, and 2.
- `overlay/FrameworksResTargetEuicc/` builds vendor RRO `FrameworksResEuicc`, active only for `ro.boot.prjname=24863`, and marks SIM slot 1 as the non-removable eUICC.
- `overlay/OPlusFrameworksResTarget/` builds a device RRO for `android`. It supplies automatic-brightness curves and hysteresis, brightness limits/default/doze values, color modes 303/301/307, the centered camera cutout and bounding approximation, fill-cutout behavior, stable 1272x2772 dimensions, per-smallest-width status-bar heights, 176-pixel rounded corners, and `power_profile.xml` (including a 7300 mAh battery model and CPU/display/radio estimates).
- `overlay/OPlusSettingsProviderResTarget/` changes the default device name to `OnePlus 15`. `overlay/OPlusSettingsResTarget/` exposes three vibrator intensity levels, Standard/Natural/Vivid color choices mapped to 301/303/307, and UDFPS enrollment radius 119.
- `overlay/OPlusSystemUIResTarget/` sets status-bar start padding to 24dp, links the keyguard carrier margin to it, locates the power button center at 1000px, and sets pixel pitch 49.49 microns. `overlay/OPlusWifiResTarget/` is a product RRO for `WifiCustomization` and makes `OnePlus 15` the default tethering SSID.
- Static device RROs use priority 350; the eUICC RRO uses 250 and the Wi-Fi RRO uses 700. Their module names are all installed explicitly from `device.mk`.

### Properties

- `odm.prop` imports `/odm/etc/${ro.boot.prjname}/build.default.prop` and makes ODM lead property resolution (`odm,vendor,product,system_ext,system`).
- `system_ext.prop` identifies the IFAA model as `ONEPLUS-R24831`.
- `vendor.prop` sets Bluetooth and USB presentation to `OnePlus 15`, enables SPR while disabling SPR bypass, and configures SurfaceFlinger display-power/idle/touch timers to 1000/80/200 ms. Regional product, regionmark, SAR, eSIM, and NFC properties live in the copied `recovery/root/vendor/odm/etc/*` files described above.

### Init, VINTF, and SELinux boundaries

- The pinned source tree contains no authored init `.rc` file and therefore declares no inspectable init service itself. `proprietary-files.txt` instead causes generated vendor output to carry `odm/etc/init/init.audio.rc`, `init.camera_debug_ui.rc`, `init.camera_process.rc`, misspelled stock `init.camera_upate.rc`, `vendor.oplus.hardware.cammidasservice-V1-service.rc`, and `vendor/etc/init/vendor.qti.camera.provider-service_64.rc`. Of these, the file names identify the proprietary audio/camera and QTI camera-provider service units; their exact `service` stanzas are opaque blob content, not present at the pinned commit. `extract-files.py` comments out `delete_recursion` in the extracted `init.camera_process.rc`.
- Likewise, there is no source-authored device manifest or compatibility matrix at the pin. `proprietary-files.txt` extracts ODM fragments `manifest_oplus_camera_rfi.xml`, `manifest_oplus_cammidasservice_aidl.xml`, `manifest_oplus_sendextcamcmd.xml`, and `vendor.oplus.camera.aon-impl.xml`, plus vendor fragments `vendor.qti.camera.aon-impl.xml`, `vendor.qti.camera.offlinecamera-impl.xml`, and `vendor.qti.camera.provider.xml`. These are the camera RFI, cammidas AIDL, external-camera-command, AON, offline-camera, and provider HAL declarations; interface/version/instance details remain inside the extracted artifacts. The tip commit removes only the obsolete HIDL cammidas service entries while retaining the AIDL fragment.
- There is no `sepolicy/` directory, no board sepolicy-directory registration, and no device-defined SELinux type or domain at the pinned commit. Policy therefore comes from the inherited SM8850-common/platform and generated vendor inputs.

### Upstream commit conventions

- The device-era history from `ae5cc0b` through the pin uses imperative subjects prefixed `infiniti:`; overlay-only changes commonly use `infiniti: overlay:`. Update commits use forms such as `infiniti: Update from OOS 11.A.43`, and focused commits name the changed behavior (`Enable`, `Adjust`, `Add`, `Remove`, `Switch`, `Drop`).
- Commits are small and topical, add explanatory bodies only when rationale is useful, and end with a Gerrit `Change-Id:` trailer. The pinned history retains older `waffle:` commits below the `infiniti` conversion, so new work should follow the current device prefix rather than those ancestral names.

## Our deltas

Relative to `ba57c74670f92f0a8870dfe2ab003aecfa2628cd`, topic HEAD contains 61 commits affecting 77 files (3,562 insertions, 695 deletions):

- Display support is substantially expanded. `configs/display/displayconfig.xml` gains a measured linear brightness-to-nits map, SDR/HDR ratio map, stock ramp rates, and VRR declaration; new `displaypanelfeature_publisher.xml` and `multimedia_display_adfr2minfps_config.xml` describe the live panel feature registry and ADFR policy. `device.mk` installs `OplusLtpo`, `displaypanelfeature-publisher`, and `oplus_ulp_aod`; a new LTPO Lineage overlay and framework/SystemUI resource changes enable LTPO, DWB, panel-driven AOD brightness, VOOC indication, GameSpace FPS, DC dimming, LiveDisplay display modes/adaptive backlight/sunlight enhancement, and disable the SurfaceFlinger idle timer in `vendor.prop`.
- Health now installs `android.hardware.health-service.oplus` and points state-of-health, cycle-count, full-charge, and design-capacity inputs at live `oplus_chg`/power-supply nodes. `BoardConfig.mk` now registers `sepolicy/vendor` as vendor/device policy and `sepolicy/private` as system-ext private policy; the new vendor policy defines `vendor_sysfs_oplus_chg`, labels the two fast-charge sysfs attributes, and grants `system_server` read access. No new process domain is defined.
- `parts/PlusKey/` adds a platform-signed privileged `system_ext` app, settings UI, broadcast receiver, action dispatchers (camera, DND, flashlight, app launch, recorder, screenshot, sound/vibration, and translate), resources, allowlist, and device test. `device.mk` installs `PlusKey`; Lineage overlay commentary records that the generic KeyHandler must not be loaded in parallel.
- Audio adds the spatializer route to the speaker and declares proprietary AudioX factories, room models, smart-PA calibration, and TFA98xx payloads. Haptics switches from local `libqtivibratoreffect.oplus.infiniti`/`vendor.qti.hardware.vibrator.service` to the consolidated Oplus QTI service and extracted complete effect banks; the four local `vibrator/effect/` source files are removed.
- Camera ownership is split: `device.mk` inherits `vendor/oneplus/camera-infiniti/camera-infiniti.mk`, `extract-files.py` and `proprietary-files.txt` move/drop duplicated camera assets and restore the OEM camera-layer closure, and the extracted `init.camera_process.rc` entry is removed. ConsumerIr's duplicate ODM declaration is also retired in favor of the source service inherited from common.
- The proprietary inventory additionally gains display color-management/ADFR data, AudioX spatializer assets, haptic banks, and supporting blobs while removing duplicate camera and `libsharebuffer_impl` extraction. Regional 24831 properties and comments in `odm.prop` are updated; the original product identity, sm8850-common inheritance, kernel selection, partition sizing, and product graph remain intact.

## Known defects

### DEF-PAIR-01 — eight declared display policy blobs were absent
`proprietary-files.txt` declared eight `my_product` VRR/brightness files that the paired Infiniti vendor repository did not carry, despite the payloads existing in the verified corpus. Done means extraction adds all declared payloads and regenerated packaging in the paired vendor commit, with declaration coherence passing. See `docs/history/DEFECTS-2026-08.md` for the full investigation.

### DEF-EUICC-01 — the product composes competing eUICC providers
The failing artifact packaged Lineage eUICC, EuiccGoogle, EuiccPolicy, and eSIM-switcher surfaces while `EuiccConnector` looped and pegged `com.android.phone`. Done means `device.mk` and proprietary declarations select one coherent provider/policy composition and a sustained device run has no bind loop. See `docs/history/DEFECTS-2026-08.md` for the full investigation.

### DEF-AOD-01 — device AOD wiring does not prove a 1 Hz floor
The product installs ULP AOD and partial DPF/ADFR configuration, but the observed panel remains at 60 Hz and required policy/consumer links are incomplete. Done means an automated sleep/wake run captures the expected edge pair and physical `test_te` telemetry below 60 during AOD. See `docs/history/DEFECTS-2026-08.md` for the full investigation.

## Owner rulings

### DPF mappings are derived from OPLUS configuration (2026-08-12)
Do not maintain a hand-authored, hash-pinned feature-ID registry in this device tree. Ship the OPLUS panel configuration as input and let the open DPF implementation derive mappings joined with RE of the dispatch chain. See `docs/history/DIRECTIVES-2026-08.md`.

### Select one principal vibrator (2026-08-13)
Device composition must select our standard vibrator owner. OPLUS linear-motor compatibility may serve named camera/gallery consumers but must not install a competing principal writer. See `docs/history/DIRECTIVES-2026-08.md`.
