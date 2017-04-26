#!/system/bin/sh
echo "Remounting /System as rw"
/system/bin/mount -oremount,rw /system
echo ""
/system/bin/mount | /system/bin/grep /system
echo ""
#
# dex2oat
echo "Copying dex2oat"

cp -f /sdcard/art-bin/dex2oat /system/bin/dex2oat
chown 0:2000 /system/bin/dex2oat
chmod 755 /system/bin/dex2oat

echo ""
#
# dex2oat Libraries
#
echo "Copying dex2oat's needed libraries"

cp /sdcard/art-libs/libart-compiler.so /system/lib/libart-compiler.so
chown 0:0 /system/lib/libart-compiler.so
chmod 644 /system/lib/libart-compiler.so
#
cp /sdcard/art-libs/libart.so /system/lib/libart.so
chown 0:0 /system/lib/libart.so
chmod 644 /system/lib/libart.so
#
cp /sdcard/art-libs/libbacktrace.so /system/lib/libbacktrace.so
chown 0:0 /system/lib/libbacktrace.so
chmod 644 /system/lib/libbacktrace.so
#
cp /sdcard/art-libs/libbase.so /system/lib/libbase.so
chown 0:0 /system/lib/libbase.so
chmod 644 /system/lib/libbase.so
#
cp /sdcard/art-libs/libc++.so /system/lib/libc++.so
chown 0:0 /system/lib/libc++.so
chmod 644 /system/lib/libc++.so
#
cp /sdcard/art-libs/libc.so /system/lib/libc.so
chown 0:0 /system/lib/libc.so
chmod 644 /system/lib/libc.so
#
cp /sdcard/art-libs/libcutils.so /system/lib/libcutils.so
chown 0:0 /system/lib/libcutils.so
chmod 644 /system/lib/libcutils.so
#
cp /sdcard/art-libs/libdl.so /system/lib/libdl.so
chown 0:0 /system/lib/libdl.so
chmod 644 /system/lib/libdl.so
#
cp /sdcard/art-libs/liblog.so /system/lib/liblog.so
chown 0:0 /system/lib/liblog.so
chmod 644 /system/lib/liblog.so
#
cp /sdcard/art-libs/libm.so /system/lib/libm.so
chown 0:0 /system/lib/libm.so
chmod 644 /system/lib/libm.so
#
cp /sdcard/art-libs/libnativebridge.so /system/lib/libnativebridge.so
chown 0:0 /system/lib/libnativebridge.so
chmod 644 /system/lib/libnativebridge.so
#
cp /sdcard/art-libs/libnativehelper.so /system/lib/libnativehelper.so
chown 0:0 /system/lib/libnativehelper.so
chmod 644 /system/lib/libnativehelper.so
#
cp /sdcard/art-libs/libsigchain.so /system/lib/libsigchain.so
chown 0:0 /system/lib/libsigchain.so
chmod 644 /system/lib/libsigchain.so
#
cp /sdcard/art-libs/libunwind.so /system/lib/libunwind.so
chown 0:0 /system/lib/libunwind.so
chmod 644 /system/lib/libunwind.so
#
cp /sdcard/art-libs/libutils.so /system/lib/libutils.so
chown 0:0 /system/lib/libutils.so
chmod 644 /system/lib/libutils.so
#
cp /sdcard/art-libs/libvixl.so /system/lib/libvixl.so
chown 0:0 /system/lib/libvixl.so
chmod 644 /system/lib/libvixl.so

echo ""
