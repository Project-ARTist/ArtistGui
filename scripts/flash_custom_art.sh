#!/usr/bin/env bash

# On System
# chmod 644 /system/lib/*.so
adb push libs/armeabi/libart-compiler.so /sdcard/art-libs/
adb push libs/armeabi/libbase.so /sdcard/art-libs/
adb push libs/armeabi/libcutils.so /sdcard/art-libs/
adb push libs/armeabi/libm.so /sdcard/art-libs/
adb push libs/armeabi/libsigchain.so /sdcard/art-libs/
adb push libs/armeabi/libvixl.so /sdcard/art-libs/
adb push libs/armeabi/libart.so /sdcard/art-libs/
adb push libs/armeabi/libc.so /sdcard/art-libs/
adb push libs/armeabi/libdl.so /sdcard/art-libs/
adb push libs/armeabi/libnativebridge.so /sdcard/art-libs/
adb push libs/armeabi/libunwind.so /sdcard/art-libs/
adb push libs/armeabi/libbacktrace.so /sdcard/art-libs/
adb push libs/armeabi/libc++.so /sdcard/art-libs/
adb push libs/armeabi/liblog.so /sdcard/art-libs/
adb push libs/armeabi/libnativehelper.so /sdcard/art-libs/
adb push libs/armeabi/libutils.so /sdcard/art-libs/

# On System
#chown 0:2000 /system/bin/dex2oat
#chmod 755 /system/bin/dex2oat
adb push assets/dex2oat /sdcard/art-bin/

