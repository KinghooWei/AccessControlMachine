########################################################################################################################

LOCAL_PATH:= $(call my-dir)

$(info DIR: $(LOCAL_PATH) )

########################################################################################################################

include $(CLEAR_VARS)
LOCAL_MODULE := libmelexis
LOCAL_SRC_FILES := prebuilt/libmelexis.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libmelexis-71
LOCAL_SRC_FILES := prebuilt/libmelexis-71.a
include $(PREBUILT_STATIC_LIBRARY)


########################################################################################################################

########################################################################################################################

include $(CLEAR_VARS)

$(info "# mlx906xx #")

LOCAL_MODULE := mlx906xx
LOCAL_SRC_FILES := jni-mlx906xx.cpp

#LOCAL_STATIC_LIBS += libmelexis
LOCAL_STATIC_LIBRARIES += libmelexis

#  -fPIE -fPIC -pie -fPIE
LOCAL_CFLAGS += $(TOOL_CFLAGS) -DANDROID_NDK
LOCAL_LDFLAGS := $(TOOL_LDFLAGS) -lstdc++
LOCAL_SHARED_LIBRARIES := libstdc++

LOCAL_LDLIBS += -lz
LOCAL_LDLIBS += -llog
LOCAL_MULTILIB  := 32
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)

########################################################################################################################


########################################################################################################################

include $(CLEAR_VARS)

$(info "# mlx906xx-71 #")

LOCAL_MODULE := mlx906xx-71
LOCAL_SRC_FILES := jni-mlx906xx.cpp

#LOCAL_STATIC_LIBS += libmelexis-71
LOCAL_STATIC_LIBRARIES += libmelexis-71

#  -fPIE -fPIC -pie -fPIE
LOCAL_CFLAGS += $(TOOL_CFLAGS) -DANDROID_NDK
LOCAL_LDFLAGS := $(TOOL_LDFLAGS) -lstdc++
LOCAL_SHARED_LIBRARIES := libstdc++

LOCAL_LDLIBS += -lz
LOCAL_LDLIBS += -llog
LOCAL_MULTILIB  := 32
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)

########################################################################################################################


