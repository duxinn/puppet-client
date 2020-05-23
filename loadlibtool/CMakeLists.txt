# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY    "${CMAKE_CURRENT_SOURCE_DIR}/src/main/assets/${ANDROID_ABI}")
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY    "${CMAKE_CURRENT_SOURCE_DIR}/src/main/assets/${ANDROID_ABI}")

add_library(loaddex

            # Sets the library as a shared library.
             SHARED
             src/main/cpp/native-lib.cpp
             src/main/cpp/fake_dlfcn.cpp)

add_executable(exelib
        src/main/jni/injector.c
        src/main/jni/main.c
        src/main/jni/ptrace.c
        src/main/jni/utils.c)

target_include_directories (loaddex PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp)

target_include_directories (exelib PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jni)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.


target_link_libraries( # Specifies the target library.
        loaddex
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )

target_link_libraries( # Specifies the target library.
        exelib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )