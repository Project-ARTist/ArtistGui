#!/usr/bin/env bash
#
# set -x # Prints A LOT of stuff we don't want to see, so prefix commands wit `exe` if you want to
#        # have them printed
#
exe() { echo "\$ $@" ; "$@" ; }

### Building ARTist on a remote machine that is included locally via ssh.
### First argument is expected to be the file path of the corresponding ssh config for a particular sdk level.
### Execute from ArtistGUI root dir!

# read parameter as configuration file path
config=$1
# include the config file

if [ ! -f ${config} ]; then
    echo "No configuration for make script found: file '${config}' is missing."
    exit
fi

echo "ARTist SSH build started"

# set the configuration values
source $config

api_level_string="android-${api_level}"

dex2oat_path="./assets/artist/${api_level_string}/dex2oat"
art_version_file="assets/VERSION_ARTIST-${api_level_string}.md"

server_art_path="${server_aosp}/art/"
server_art_git_path="${server_art_path}/.git"

working_dir=`pwd`
lib="lib"

dexToOatLibs=(
    "libc.so"
    "libc++.so"
    "libnativebridge.so"
    "libnativehelper.so"
    "libnativeloader.so "
    "libart.so"
    "libart-compiler.so"
    "libvixl.so"
    "libbacktrace.so"
    "libbase.so"
    "liblog.so"
    "libcutils.so"
    "libsigchain.so"
    "libunwind.so"
    "libutils.so"
    "libdl.so"
    "libm.so"
    "liblzma.so"
    "liblz4.so"
)

set -e # fail on errors in subcommands
set -u # treat missing variables as errors

# Setup architecture Specific variables & log
if [ "${arch}" = "x86" ]; then
    if [ "${arch_64}" = true ]; then
        ndk_binary_strip="${ndk_path}/toolchains/x86_64-4.9/prebuilt/linux-x86_64/bin/x86_64-linux-android-strip"
        arch_path='generic_x86_64'
        lunch_arch='aosp_x86_64-eng'
    else
        ndk_binary_strip="${ndk_path}/toolchains/x86-4.9/prebuilt/linux-x86_64/bin/i686-linux-android-strip"
        arch_path='generic_x86'
        lunch_arch='aosp_x86-eng'
    fi
elif [ "${arch}" = "arm" ]; then
    if [ "${arch_64}" = true ]; then
        ndk_binary_strip="${ndk_path}/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64/bin/aarch64-linux-android-strip"
        arch_path='generic_arm64'
        lunch_arch='aosp_arm64-eng'
    else
        ndk_binary_strip="${ndk_path}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-strip"
        arch_path='generic'
        lunch_arch='aosp_arm-eng'
    fi
else
    echo "unsupported architecture"
    exit 1
fi
echo "Connecting to ${server_alias}, building Android '${arch}' 64bit: '${arch_64}' target-path: '${arch_path}'"

# do the actual building
if [ "${debug_binaries}" = true ]; then
    [ -d "debug/android-${api_level}" ] || mkdir -p "debug/android-${api_level}"
fi

ssh ${server_alias} "cd ${server_aosp} ; . build/envsetup.sh; lunch ${lunch_arch}; mmm art/ -j${threads}"
#ssh ${server_alias} "cd ${server_aosp} ; . build/envsetup.sh; lunch ${lunch_arch}; mmma art/ -j${threads}"

if [ $? -eq 0 ]; then
    echo ""
    echo "Build ARTist succeeded!"
    echo ""
    cd ./app/src/main

    echo "Removing old binaries and shared objects"
    echo ""

    # delete files if they exist but do not fail if they don't (first compilation)
    rm ./assets/artist/${api_level_string}/dex2oat || true 
    rm ./assets/artist/${api_level_string}/lib/*.so || true

    echo "Creating folders if necessary: ./assets/artist/${api_level_string}/lib/"
    mkdir -p ./assets/artist/${api_level_string}/lib/ || true

    echo ""
    echo "Debug binaries will get copied to ${working_dir}/debug/android-${api_level}/${lib}"
    mkdir -p ${working_dir}/debug/android-${api_level}/${lib} || true
    echo ""

    echo "Copying new binaries and shared objects"
    echo ""

    echo "Copy dex2oat -> ./assets/artist/${api_level_string}/dex2oat"
    exe cp ${mounted_aosp}/out/target/product/${arch_path}/symbols/system/bin/dex2oat ./assets/artist/${api_level_string}/dex2oat
    exe cp ./assets/artist/${api_level_string}/dex2oat ${working_dir}/debug/android-${api_level}/dex2oat

    ## now loop through the above array
    for lib in "${dexToOatLibs[@]}"
    do
        echo "Copy ${lib} -> './assets/artist/${api_level_string}/lib/'"
        exe cp ${mounted_aosp}/out/target/product/${arch_path}/symbols/system/lib/${lib} ./assets/artist/${api_level_string}/lib/ || true
        exe cp ./assets/artist/${api_level_string}/lib/${lib} ${working_dir}/debug/android-${api_level}/${lib} || true

        if [ "${debug_binaries}" = true ]; then
            echo " > ${lib}: Keeping debug symbols"
        else
            echo " > ${lib}: Stripping debug symbols"
            exe ${ndk_binary_strip} ./assets/artist/${api_level_string}/lib/${lib} || true
        fi
    done
    echo ""
    echo "Copying files DONE"

    echo ""
    echo "Saving Git stats"
    exe ssh ${server_alias} "git --git-dir ${server_art_git_path} --work-tree ${server_art_path} log -1 | grep commit" > ${art_version_file}
    exe ssh ${server_alias} "git --git-dir ${server_art_git_path} --work-tree ${server_art_path} status --porcelain" >> ${art_version_file}
else
    echo "Building ARTist failed..."
fi

echo "" && date
