cmake_minimum_required(VERSION 3.6)

project(XASH_ANDROID)

# armeabi-v7a requires cpufeatures library
include(AndroidNdkModules)
android_ndk_import_module_cpufeatures()

find_package(PythonInterp 2.7 REQUIRED)

get_filename_component(C_COMPILER_ID ${CMAKE_C_COMPILER} NAME_WE)
get_filename_component(CXX_COMPILER_ID ${CMAKE_CXX_COMPILER} NAME_WE)

if(${CMAKE_BUILD_TYPE} STREQUAL "Debug")
	set(BUILD_TYPE "debug")
else()
	set(BUILD_TYPE "release")
endif()

set(CMAKE_VERBOSE_MAKEFILE ON)

set(WAF_CC "${CMAKE_C_COMPILER} --target=${CMAKE_C_COMPILER_TARGET}")
set(WAF_CXX "${CMAKE_CXX_COMPILER} --target=${CMAKE_CXX_COMPILER_TARGET}")

execute_process(
		COMMAND ${CMAKE_COMMAND} -E env
		CC=${WAF_CC} CXX=${WAF_CXX}
		AR=${CMAKE_AR} STRIP=${CMAKE_STRIP}
		${PYTHON_EXECUTABLE} waf configure -vvv -T ${BUILD_TYPE} cmake
		--check-c-compiler=${C_COMPILER_ID} --check-cxx-compiler=${CXX_COMPILER_ID}
		-s "${CMAKE_CURRENT_SOURCE_DIR}/SDL" --skip-sdl2-sanity-check
		WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/xash3d-fwgs"
)

if(CMAKE_SIZEOF_VOID_P MATCHES "8")
	set(64BIT ON CACHE BOOL "" FORCE)
endif()
add_subdirectory("hlsdk-portable")
add_subdirectory("SDL")
add_subdirectory("xash3d-fwgs")
add_subdirectory("xash3d-fwgs/3rdparty/mainui")