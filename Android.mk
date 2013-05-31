LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := AutoTest
LOCAL_CERTIFICATE := platform

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

