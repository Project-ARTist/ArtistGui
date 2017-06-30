#!/usr/bin/env bash


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



# set the configuration values
source $config

ndk_binary_strip="${ndk_path}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-strip"

#server_aosp_path = ${server_mount_path}
#server_art_path="${server_mount_path}/art/"
#server_art_git_path="${server_art_path}/.git"

api_level_string="android-${api_level}"

dex2oat_path="./assets/artist/${api_level_string}/dex2oat"
art_version_file="assets/VERSION_ARTIST-${api_level_string}.md"

mounted_art_path="${mounted_aosp}/art/"
mounted_art_git_path="${mounted_art_path}/.git"

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

# do the actual building
echo "Connecting to ${server_alias}, building Android"

ssh ${server_alias} "cd ${server_aosp} ; . build/envsetup.sh; mmma art/ -j${threads}"

if [ $? -eq 0 ]; then
    echo ""
    echo "Build ARTist succeeded!"
    echo ""
    cd ./app/src/main

    echo "Removing old binaries and shared objects"
    echo ""

    rm ./assets/artist/${api_level_string}/dex2oat
    rm ./assets/artist/${api_level_string}/lib/*.so

    echo "Creating folders if necessary: ./assets/artist/${api_level_string}/lib/"
    mkdir -p ./assets/artist/${api_level_string}/lib/

    echo "Copying new binaries and shared objects"
    echo ""

    echo "Copy dex2oat -> ./assets/artist/${api_level_string}/dex2oat"
    cp ${mounted_aosp}/out/target/product/generic/symbols/system/bin/dex2oat ./assets/artist/${api_level_string}/dex2oat

    ## now loop through the above array
    for lib in "${dexToOatLibs[@]}"
    do
        echo "Copy ${lib} -> './assets/artist/${api_level_string}/lib/'"
        cp ${mounted_aosp}/out/target/product/generic/symbols/system/lib/${lib} ./assets/artist/${api_level_string}/lib/
        ${ndk_binary_strip} ./assets/artist/${api_level_string}/lib/${lib}
    done
    echo ""
    echo "Copying files DONE"

    echo ""
    echo "Saving Git stats"
    git --git-dir ${mounted_art_git_path} --work-tree ${mounted_art_path} log -1 | grep commit > ${art_version_file}
    git --git-dir ${mounted_art_git_path} --work-tree ${mounted_art_path} status --porcelain >> ${art_version_file}

else
    echo "Building ARTist failed..."
fi

echo "" && date
