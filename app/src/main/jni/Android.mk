LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := injector
LOCAL_SRC_FILES := injector.c
LOCAL_LDLIBS := -llog -ldl
include $(BUILD_EXECUTABLE)
