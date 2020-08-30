# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
message("CMAKE_SOURCE_DIR:" ${CMAKE_SOURCE_DIR})
#include头文件目录
include_directories(include/ncnn)

#source directory源文件目录
file(GLOB FACE_SRC *.h
                   *.cpp)


set(FACE_COMPILE_CODE ${FACE_SRC})
message("FACE_SRC:" ${FACE_SRC})
#添加ncnn库
add_library(libncnn STATIC IMPORTED )
set_target_properties(libncnn
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../../../libs/${ANDROID_ABI}/libncnn.a)
message("CMAKE_SOURCE_DIR:" ${CMAKE_SOURCE_DIR})
##编译为动态库
add_library(Face SHARED ${FACE_COMPILE_CODE})
#
find_library(  log-lib log )
##添加工程所依赖的库
target_link_libraries( Face
        libncnn
        android
        ${log-lib}
        )
