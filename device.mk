#
# Copyright (C) 2021-2026 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# AAPT
PRODUCT_AAPT_CONFIG := normal
PRODUCT_AAPT_PREF_CONFIG := xxxhdpi

# AOD
PRODUCT_PACKAGES += \
    oplus_aod_panel_state \
    oplus_ulp_aod

# Audio
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/audio/audio_module_config_primary.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio/audio_module_config_primary.xml \
    $(LOCAL_PATH)/configs/audio/audio_policy_volumes.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy_volumes.xml \
    $(LOCAL_PATH)/configs/audio/default_volume_tables.xml:$(TARGET_COPY_OUT_VENDOR)/etc/default_volume_tables.xml

# Boot animation
TARGET_SCREEN_HEIGHT := 2772
TARGET_SCREEN_WIDTH := 1272

# Display
PRODUCT_PACKAGES += \
    OplusLtpo

PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/display/displaypanelfeature_publisher.xml:$(TARGET_COPY_OUT_VENDOR)/etc/display/displaypanelfeature_publisher.xml \
    $(LOCAL_PATH)/configs/display/multimedia_display_adfr2minfps_config.xml:$(TARGET_COPY_OUT_VENDOR)/etc/display/multimedia_display_adfr2minfps_config.xml \
    $(LOCAL_PATH)/configs/display/displayconfig.xml:$(TARGET_COPY_OUT_VENDOR)/etc/displayconfig/display_id_4630946903293830803.xml \
    $(LOCAL_PATH)/configs/display/displayconfig.xml:$(TARGET_COPY_OUT_VENDOR)/etc/displayconfig/display_id_4630946982335253651.xml

PRODUCT_PACKAGES += \
    adfr-floor-daemon \
    displaypanelfeature-publisher \
    vendor.oplus.hardware.displaypanelfeature-service.oplus

# Health
PRODUCT_PACKAGES += \
    android.hardware.health-service.oplus

$(call soong_config_set,oplus_health,battery_state_of_health_path,/sys/class/oplus_chg/battery/battery_ui_soh)
$(call soong_config_set,oplus_health,battery_cycle_count_path,/sys/class/power_supply/battery/cycle_count)
$(call soong_config_set,oplus_health,battery_full_charge_path,/sys/class/power_supply/battery/charge_full)
$(call soong_config_set,oplus_health,battery_full_charge_design_capacity_uah_path,/sys/class/power_supply/battery/charge_full_design)
$(call soong_config_set,oplus_health,battery_full_charge_requires_independent_value,true)

# Plus Key
PRODUCT_PACKAGES += \
    PlusKey

# Keymint
PRODUCT_PACKAGES += \
    android.hardware.security.keymint3-service.strongbox.nxp \
    android.hardware.weaver-service.nxp

# LiveDisplay
$(call soong_config_set_bool,OPLUS_LINEAGE_LIVEDISPLAY_HAL,ENABLE_AB,true)
$(call soong_config_set_bool,OPLUS_LINEAGE_LIVEDISPLAY_HAL,ENABLE_DC,true)
$(call soong_config_set_bool,OPLUS_LINEAGE_LIVEDISPLAY_HAL,ENABLE_SE,true)
$(call soong_config_set_bool,OPLUS_LINEAGE_LIVEDISPLAY_HAL,ENABLE_AF,true)
$(call soong_config_set_bool,OPLUS_LINEAGE_LIVEDISPLAY_HAL,ENABLE_DM,true)

# Overlays
DEVICE_PACKAGE_OVERLAYS += \
    $(LOCAL_PATH)/overlay-lineage

PRODUCT_PACKAGES += \
    FrameworksResEuicc \
    OPlusFrameworksResTarget \
    OPlusSettingsProviderResTarget \
    OPlusSettingsResTarget \
    OPlusSystemUIResTarget \
    OPlusWifiResTarget

# PowerShare
PRODUCT_PACKAGES += \
    vendor.lineage.powershare-service.oplus

# Recovery
$(call soong_config_set_bool,recovery,target_recovery_uses_qti_drm,true)

# Regional properties
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/recovery/root/vendor/odm/etc/24831/build.default.prop:$(TARGET_COPY_OUT_ODM)/etc/24831/build.default.prop \
    $(LOCAL_PATH)/recovery/root/vendor/odm/etc/24863/build.EU.prop:$(TARGET_COPY_OUT_ODM)/etc/24863/build.EU.prop \
    $(LOCAL_PATH)/recovery/root/vendor/odm/etc/24863/build.IN.prop:$(TARGET_COPY_OUT_ODM)/etc/24863/build.IN.prop \
    $(LOCAL_PATH)/recovery/root/vendor/odm/etc/24863/build.NA.prop:$(TARGET_COPY_OUT_ODM)/etc/24863/build.NA.prop \
    $(LOCAL_PATH)/recovery/root/vendor/odm/etc/24863/build.ROW.prop:$(TARGET_COPY_OUT_ODM)/etc/24863/build.ROW.prop \
    $(LOCAL_PATH)/recovery/root/vendor/odm/etc/24863/build.default.prop:$(TARGET_COPY_OUT_ODM)/etc/24863/build.default.prop

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    $(LOCAL_PATH)

# Telephony
PRODUCT_PACKAGES += \
    OplusEsimSwitcher \
    OplusEuicc

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.telephony.euicc.mep.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/permissions/android.hardware.telephony.euicc.mep.xml \
    frameworks/native/data/etc/android.hardware.telephony.euicc.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/permissions/android.hardware.telephony.euicc.xml

# Touch features
$(call soong_config_set_bool,OPLUS_LINEAGE_TOUCH_HAL,ENABLE_GM,true)

# Vibrator
PRODUCT_PACKAGES += \
    vendor.qti.hardware.vibrator.service.oplus

$(call soong_config_set_bool,OPLUS_LINEAGE_VIBRATOR_HAL,USE_EFFECT_STREAM,true)

# Inherit from the common OEM chipset makefile.
$(call inherit-product, device/oneplus/sm8850-common/common.mk)

# Inherit from the proprietary files makefile.
$(call inherit-product, vendor/oneplus/infiniti/infiniti-vendor.mk)

# Camera (Oplus camera port)
$(call inherit-product, vendor/oneplus/camera-infiniti/camera-infiniti.mk)
