LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JAVA_LIBRARIES := ucache com.noahedu
LOCAL_PACKAGE_NAME := ResDownloadCenter
include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
