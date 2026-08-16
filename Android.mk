#
# Copyright (C) 2026 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

ifeq ($(TARGET_DEVICE),infiniti)
infiniti_build_manifest_sentinel := $(PRODUCT_OUT)/.build-manifest-source-state
$(INSTALLED_BUILD_MANIFEST_XML_TARGET): $(infiniti_build_manifest_sentinel)
$(infiniti_build_manifest_sentinel):
endif
