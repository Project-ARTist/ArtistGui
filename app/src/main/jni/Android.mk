LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := art-compiler
LOCAL_STRIP_MODULE=false
LOCAL_SRC_FILES := libart-compiler.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := art
LOCAL_SRC_FILES := libart.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := vixl
LOCAL_SRC_FILES := libvixl.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := nativebridge
LOCAL_SRC_FILES := libnativebridge.so
include $(PREBUILT_SHARED_LIBRARY)





#####################################################

include $(CLEAR_VARS)
LOCAL_MODULE := backtrace
LOCAL_SRC_FILES := libbacktrace.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := base
LOCAL_SRC_FILES := libbase.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := c++
LOCAL_SRC_FILES := libc++.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := log
LOCAL_SRC_FILES := liblog.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := cutils
LOCAL_SRC_FILES := libcutils.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := nativehelper
LOCAL_SRC_FILES := libnativehelper.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := sigchain
LOCAL_SRC_FILES := libsigchain.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := unwind
LOCAL_SRC_FILES := libunwind.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := utils
LOCAL_SRC_FILES := libutils.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := c
LOCAL_SRC_FILES := libc.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := dl
LOCAL_SRC_FILES := libdl.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := m
LOCAL_SRC_FILES := libm.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := lz4
LOCAL_SRC_FILES := liblz4.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := lzma
LOCAL_SRC_FILES := liblzma.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := nativeloader
LOCAL_SRC_FILES := libnativeloader.so
include $(PREBUILT_SHARED_LIBRARY)

