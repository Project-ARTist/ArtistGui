/*
 * The ARTist Project (https://artist.cispa.saarland)
 *
 * Copyright (C) 2017 CISPA (https://cispa.saarland), Saarland University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package saarland.cispa.artist.artistgui.instrumentation.stages;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.util.List;

import saarland.cispa.apksigner.ApkSigner;
import saarland.cispa.apksigner.ApkZipSir;
import saarland.cispa.artist.artistgui.instrumentation.config.ArtistRunConfig;
import saarland.cispa.artist.artistgui.instrumentation.exceptions.InstrumentationException;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressListener;
import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;
import saarland.cispa.artist.artistgui.utils.AndroidUtils;
import saarland.cispa.artist.artistgui.utils.ArtistUtils;
import saarland.cispa.artist.artistgui.utils.ProcessExecutor;
import saarland.cispa.dexterous.Dexterous;
import saarland.cispa.dexterous.MergeConfig;
import saarland.cispa.utils.LogA;

public class InstrumentationStagesImpl implements InstrumentationStages {

    private static final String TAG = "InstrumentationStages";

    private static final boolean PIXEL_PHONE_ANDROID_8 = false;

    private Context mContext;
    private ArtistRunConfig mRunConfig;
    private List<ProgressListener> mProgressListeners;

    public InstrumentationStagesImpl(Context mContext, ArtistRunConfig mRunConfig,
                                     List<ProgressListener> mProgressListeners) {
        this.mContext = mContext;
        this.mRunConfig = mRunConfig;
        this.mProgressListeners = mProgressListeners;
    }

    @Override
    public String prepareEnvironment() throws InstrumentationException {
        createArtistFolders();
        cleanOldBuildFiles();
        setupKeystore();

        String pathDex2oat = extractDex2Oat();
        setupArtistLibraries();

        return pathDex2oat;
    }

    private void createArtistFolders() {
        AndroidUtils.createFoldersInFilesDir(mContext, ArtistAppConfig.APP_FOLDER_APK_BACKUP);
        AndroidUtils.createFoldersInFilesDir(mContext, ArtistAppConfig.APP_FOLDER_CODELIBS);
        AndroidUtils.createFoldersInFilesDir(mContext, mRunConfig.artist_exec_path);
        AndroidUtils.createFoldersInFilesDir(mContext, mRunConfig.artist_exec_path_libs_dir);
    }

    private void cleanOldBuildFiles() {
        reportProgressDetails("Deleting: " + mRunConfig.app_apk_merged_file_path);
        AndroidUtils.deleteExistingFile(mRunConfig.app_apk_merged_file_path);

        reportProgressDetails("Deleting: " + mRunConfig.app_apk_merged_signed_file_path);
        AndroidUtils.deleteExistingFile(mRunConfig.app_apk_merged_signed_file_path);
        if (mRunConfig.isAssetCodeLib()) {
            reportProgressDetails("Deleting: " + mRunConfig.codeLib);
            AndroidUtils.deleteExistingFile(mRunConfig.codeLib);
        }
    }

    private void setupKeystore() {
        LogA.d(TAG, "setupKeystore()");
        reportProgressDetails("KeyStore: " + mRunConfig.keystore);
        AndroidUtils.copyAsset(mContext, mRunConfig.asset_path_keystore,
                mRunConfig.keystore.getAbsolutePath());
    }

    private String extractDex2Oat() throws InstrumentationException {
        final String pathDex2oat = copyAssetToFilesDir(mContext,
                mRunConfig.asset_path_dex2oat,
                mRunConfig.artist_exec_path_dex2oat);
        LogA.i(TAG, "> pathDex2oat: " + pathDex2oat);
        LogA.i(TAG, "  > config:    " + mRunConfig.artist_exec_path_dex2oat);

        if (pathDex2oat.isEmpty()) {
            throw new InstrumentationException("Artist: Dex2oat Setup failed");
        }

        return pathDex2oat;
    }

    private String copyAssetToFilesDir(final Context context,
                                       final String assetPathRelative,
                                       final String filePathRelative) {
        final String filesDirPath = AndroidUtils.getFilesDirLocation(context, filePathRelative);
        // AndroidUtils.deleteExistingFile(filesDirPath);
        AndroidUtils.copyAsset(context, assetPathRelative, filesDirPath);
        boolean success = AndroidUtils.chmodExecutable(filesDirPath);
        if (success) {
            return filesDirPath;
        } else {
            return "";
        }
    }

    private void setupArtistLibraries() {
        LogA.d(TAG, "setupArtistLibraries()");
        reportProgressDetails("Copying Libraries to: " + mRunConfig.artist_exec_path_libs_dir);
        // Example Permissions: /data/app/com.deepinc.arte360-1/lib/arm/
        // -rwxr-xr-x 1 system system  511044 2016-05-05 17:30 libTBAudioEngine.so
        // -rwxr-xr-x 1 system system  771432 2016-05-05 17:30 libmetadataparser.so
        // -rwxr-xr-x 1 system system 1117296 2016-05-05 17:30 libvideorenderer.so
        // -rwxr-xr-x 1 system system  677784 2016-05-05 17:30 libvrtoolkit.so
        AndroidUtils.copyAssetFolderContent(mContext, mRunConfig.asset_path_dex2oat_libs,
                mRunConfig.artist_exec_path_libs_dir);
    }

    @Override
    public void probePermissionAndDeleteOatFile() {
        reportProgressDetails("Probing oat file permissions: " + mRunConfig.app_oat_file_path);
        mRunConfig.oatOwner = AndroidUtils.getFileOwnerUid(mRunConfig.app_oat_file_path);
        mRunConfig.oatGroup = AndroidUtils.getFileGroupId(mRunConfig.app_oat_file_path);
        mRunConfig.oatPermissions = AndroidUtils.getFilePermissions(mRunConfig.app_oat_file_path);
        mRunConfig.stats.oatFileSizeOriginal = new File(mRunConfig.app_oat_file_path).length();
        LogA.d(TAG, String.format("base.odex UID: %s GID: %s Permissions: %s Size: %s",
                mRunConfig.oatOwner,
                mRunConfig.oatGroup,
                mRunConfig.oatPermissions,
                mRunConfig.stats.oatFileSizeOriginal));

        reportProgressDetails("Deleting existing oat file: " + mRunConfig.app_oat_file_path);
        boolean success = deleteRootFile(mRunConfig.app_oat_file_path);
        if (!success) {
            LogA.d(TAG, String.format("Failed to delete old base oat: %s - Continue", mRunConfig.app_oat_file_path));
        }
    }

    private boolean deleteRootFile(final String filePath) {
        final String cmd_rm_root_file = "rm " + filePath;
        return ProcessExecutor.execute(cmd_rm_root_file, true,
                ProcessExecutor.processName(mRunConfig.app_package_name, "rm_rootfile"));
    }

    @Override
    public void mergeCodeLib() throws InstrumentationException {
        LogA.d(TAG, "MergeCodeLib into: " + mRunConfig.app_apk_file_path);

        String pathToApkSigned;
        // deactivate injection upon user wish or if no code lib is provided
        if (mRunConfig.codeLib != null) {
            reportProgressDetails("Injecting CodeLib");
            final File appApk = new File(mRunConfig.app_apk_file_path);
            final File codeLibApk = mRunConfig.codeLib;

            setupCodeLib();

            final MergeConfig mergeConfig = new MergeConfig(mRunConfig.codeLib.getName(),
                    mRunConfig.app_apk_merged_file_path, mRunConfig.app_apk_file_path);

            Dexterous dexterous = new Dexterous(mergeConfig);
            dexterous.init(appApk, codeLibApk);
            dexterous.mergeCodeLib();
            final String pathToApk = dexterous.buildApk();
            reportProgressDetails("Resigning APK");
            LogA.d(TAG, String.format("MergeCodeLib DONE (%s)", pathToApk));

            pathToApkSigned = resignApk(pathToApk);
            LogA.d(TAG, String.format("MergeCodeLib Signing DONE (%s)", pathToApkSigned));

            if (pathToApkSigned.isEmpty()) {
                throw new InstrumentationException("Codelib Merge Failed");
            }
        } else {
            reportProgressDetails("Not Injecting CodeLib");
            LogA.i(TAG, "Skip CodeLib Injection");
            LogA.d(TAG, "MergeCodeLib SKIPPED");
        }
    }

    private void setupCodeLib() {
        if (mRunConfig.codeLibName.startsWith(ArtistUtils.CODELIB_ASSET)) {
            LogA.d(TAG, "setupCodeLib() " + mRunConfig.codeLibName);
            final String assetName = mRunConfig.codeLibName.replaceFirst(ArtistUtils.CODELIB_ASSET, "");
            AndroidUtils.copyAsset(mContext, "codelib" + File.separator + assetName,
                    mRunConfig.codeLib.getAbsolutePath());
            if (!mRunConfig.codeLib.exists()) {
                LogA.e(TAG, " setupCodeLib: " + mRunConfig.codeLib + " FAILED");
            } else {
                LogA.d(TAG, " setupCodeLib: " + mRunConfig.codeLib + " READY");
            }
        }
    }

    private String resignApk(final String unsignedApkPath) {
        LogA.d(TAG, "resignApk() " + unsignedApkPath);

        String signedApkPath;
        final ApkSigner apkSir = new ApkZipSir(mRunConfig.app_apk_merged_signed_file_path);
        try {
            signedApkPath = apkSir.signApk(mRunConfig.keystore.getAbsolutePath(), unsignedApkPath);
        } catch (final IllegalArgumentException e) {
            LogA.e(TAG, "> Signing of APK Failed", e);
            signedApkPath = "";
        }
        return signedApkPath;
    }

    @Override
    public void backupMergedApk() {
        LogA.v(TAG, "backupMergedApk()");
        final File externalStorage = mContext.getExternalFilesDir(null);
        if (externalStorage != null) {
            final String mergedApkBackupPath = externalStorage.getAbsolutePath() + File.separator
                    + "last_merged_signed_instrumented_app.apk";

            reportProgressDetails("Backing up Merged APK: " + mergedApkBackupPath);

            final String cmd_backup_merged_apk = "cp " + mRunConfig.app_apk_merged_signed_file_path +
                    " " + mergedApkBackupPath;

            boolean success = ProcessExecutor.execute(cmd_backup_merged_apk, true,
                    ProcessExecutor.processName(mRunConfig.app_package_name, "cp_backup_merged"));

            if (success) {
                LogA.d(TAG, "backupMergedApk() Success: " + mergedApkBackupPath);
            } else {
                LogA.e(TAG, "backupMergedApk() Failed:  " + mergedApkBackupPath);
            }
        }
    }

    @Override
    public void runDex2OatCompilation(String pathDex2oat) throws InstrumentationException {
        final String cmd_dex2oat_compile = setupDex2oatCommand(pathDex2oat);

        LogA.d(TAG, "dex2oat command:");
        LogA.d(TAG, cmd_dex2oat_compile);
        LogA.d(TAG, "Starting the compilation process!");
        LogA.d(TAG, "> Result will get placed at: " + mRunConfig.app_oat_file_path);

        final String divider = "########################################################";

        LogA.d(TAG, divider);
        LogA.d(TAG, divider);
        LogA.d(TAG, divider);

        boolean success = ProcessExecutor.execute(cmd_dex2oat_compile, true,
                ProcessExecutor.processName(mRunConfig.app_package_name, "dex2artist"));

        LogA.d(TAG, divider);
        LogA.d(TAG, divider);
        LogA.d(TAG, divider);

        if (success) {
            LogA.d(TAG, "Compilation was successfull");
        } else {
            LogA.d(TAG, "Compilation failed...");
            throw new InstrumentationException("Artist Injection Failed");
        }
    }

    //
    // TODO: instruction-set features & variant needs to get set "per device" or we use generic
    //       settings
    private String setupDex2oatCommand(final String pathDex2oat) {
        AndroidUtils.logBuildInformation();

        String cmd_dex2oat_compile =
                "export LD_LIBRARY_PATH=" + mContext.getApplicationInfo().nativeLibraryDir + ":"
                        + AndroidUtils.getFilesDirLocation(mContext, mRunConfig.artist_exec_path_libs_dir) + ";"
                        + pathDex2oat
                        + " --oat-file=" + mRunConfig.app_oat_file_path
                        + " --compiler-backend=Optimizing"
                        + " --compiler-filter=everything"
                        + " --generate-debug-info"
                        + " --compile-pic";

        cmd_dex2oat_compile += " --dex-file=" + (mRunConfig.codeLib != null ?
                mRunConfig.app_apk_merged_signed_file_path : mRunConfig.app_apk_file_path);
        cmd_dex2oat_compile += " --checksum-rewriting";
        cmd_dex2oat_compile += " --dex-location=" + mRunConfig.app_apk_file_path;

        if (mRunConfig.COMPILER_THREADS != -1) {
            cmd_dex2oat_compile += " -j" + mRunConfig.COMPILER_THREADS;
            LogA.d(TAG, "Dex2oat: Compiler Threads: " + mRunConfig.COMPILER_THREADS);
        } else {
            LogA.d(TAG, "Dex2oat: Compiler Threads: <DEFAULT>");
        }

        LogA.d(TAG, "Dex2oat: app_oat_architecture: " + mRunConfig.app_oat_architecture);
        if (mRunConfig.app_oat_architecture.contains("x86_64")) {
            LogA.d(TAG, "Dex2oat: Architecture: x86_64");
            // @FYI: ARTist only compiles on the x86_64 emulator with: " --instruction-set=x86"
            //       but doesn't start the instrumented app
            cmd_dex2oat_compile += " --instruction-set=x86_64";
            cmd_dex2oat_compile += " --instruction-set-variant=atom";
        } else if (mRunConfig.app_oat_architecture.contains("x86")
                && !mRunConfig.app_oat_architecture.contains("x86_64")) {
            LogA.d(TAG, "Dex2oat: Architecture: x86");
            cmd_dex2oat_compile += " --instruction-set=x86";
            cmd_dex2oat_compile += " --instruction-set-variant=atom";
        } else if (mRunConfig.app_oat_architecture.contains("arm64")) {
            LogA.d(TAG, "Dex2oat: Architecture: arm64");
            // ARM64 Special Flags
            cmd_dex2oat_compile += " --instruction-set=arm64";
            if (PIXEL_PHONE_ANDROID_8) {
                cmd_dex2oat_compile += " --instruction-set-variant=kryo";
            } else {
                cmd_dex2oat_compile += " --instruction-set-variant=denver64";
            }
            // ARM64 Special Flags END
            LogA.d(TAG, "Compiling for 64bit Architecture!");
        } else {
            LogA.w(TAG, "Dex2oat: Architecture: <Unsupported Architecture>");
        }
        // ////////////////////////////////////////////
        //
        // smp does not exist anymore on Android 8.0 Oreo !
        //
        if (Build.HARDWARE.equals("ranchu")) {
            LogA.d(TAG, "Dex2oat: InstructionSet: <emulator>");
//            cmd_dex2oat_compile += " --instruction-set-features=smp";
            cmd_dex2oat_compile += " --instruction-set-features=default";
//
//            --boot-image=<file.art>: provide the image file for the boot class path.
//                                     Do not include the arch as part of the name, it is added automatically.
//                                     Example: --boot-image=/system/framework/boot.art
//                                     (specifies /system/framework/<arch>/boot.art as the image file)
//
//                Default: $ANDROID_ROOT/system/framework/boot.art
//            cmd_dex2oat_compile += " --boot-image=/system/framework/boot.art";
//            cmd_dex2oat_compile += " --boot-image=/system/framework/x86_64/boot.art";
//            cmd_dex2oat_compile += " --instruction-set-features=smp,ssse3,sse4.1,sse4.2,-avx,-avx2";
//            cmd_dex2oat_compile += " --instruction-set-features=default";
        } else if (mRunConfig.app_oat_architecture.contains("x86")) {
            LogA.d(TAG, "Dex2oat: InstructionSet: x86*");
            cmd_dex2oat_compile += " --instruction-set-features=smp,ssse3,sse4.1,sse4.2,-avx,-avx2";
            cmd_dex2oat_compile += " --instruction-set-features=default";
        } else if (mRunConfig.app_oat_architecture.contains("arm64") && PIXEL_PHONE_ANDROID_8) {
            LogA.d(TAG, "Dex2oat: InstructionSet: arm64 (Pixel Phone)");
            // ARM64 Special Flags
            cmd_dex2oat_compile += " --instruction-set-features=default";
        } else if (mRunConfig.app_oat_architecture.contains("arm64")) {
            LogA.d(TAG, "Dex2oat: InstructionSet: arm64*");
            // smp does not exist anymore on Android 8.0 Oreo
            cmd_dex2oat_compile += " --instruction-set-features=smp,a53";
            cmd_dex2oat_compile += " --instruction-set-features=default";
            LogA.d(TAG, "Dex2oat: ranchu (emulator)");
        } else {
            LogA.w(TAG, "Dex2oat: InstructionSet: <Unsupported InstructionSet>");
            cmd_dex2oat_compile += " --instruction-set-features=default";
        }
        return cmd_dex2oat_compile;
    }

    @Override
    public boolean setOatFilePermissions() {
        boolean success = false;
        final File oatFile = new File(this.mRunConfig.app_oat_file_path);
        if (oatFile.exists() && !oatFile.isDirectory()) {
            LogA.d(TAG, "Success! Oat file created.");
            reportProgressDetails("Fixing oat file permissions");

            mRunConfig.stats.oatFileSizeRecompiled = oatFile.length();

            reportProgressDetails("odex OLD size: " + mRunConfig.stats.oatFileSizeOriginal);
            LogA.d(TAG, "odex OLD size: " + mRunConfig.stats.oatFileSizeOriginal);
            reportProgressDetails("odex NEW size: " + mRunConfig.stats.oatFileSizeRecompiled);
            LogA.d(TAG, "odex NEW size: " + mRunConfig.stats.oatFileSizeRecompiled);

            LogA.d(TAG, "Changing the owner of the oat file to " + mRunConfig.oatOwner);

            final String cmd_chown_oat = "chown " + mRunConfig.oatOwner + " " + this.mRunConfig.app_oat_file_path;
            success = ProcessExecutor.execute(cmd_chown_oat, true, ProcessExecutor.processName(mRunConfig.app_package_name, "chown_oatfile"));

            if (!success) {
                LogA.d(TAG, "Could not change oat owner to " + mRunConfig.oatOwner + "... ");
                LogA.d(TAG, "... for path " + this.mRunConfig.app_oat_file_path);
                return success;
            }
            LogA.d(TAG, "Changing the group of the oat file to " + mRunConfig.oatGroup);

            final String cmd_chgrp_oat = "chgrp " + mRunConfig.oatGroup + " " + this.mRunConfig.app_oat_file_path;
            success = ProcessExecutor.execute(cmd_chgrp_oat, true, ProcessExecutor.processName(mRunConfig.app_package_name, "chgrp_oatfile"));

            if (!success) {
                LogA.d(TAG, "Could not change oat group to " + mRunConfig.oatGroup + "... ");
                LogA.d(TAG, "... for path " + this.mRunConfig.app_oat_file_path);
                return success;
            }

            success = AndroidUtils.chmodExecutable(this.mRunConfig.app_oat_file_path);

            if (!success) {
                LogA.d(TAG, "Could not change oat permissions to 777");
                LogA.d(TAG, "... for path " + this.mRunConfig.app_oat_file_path);
                return success;
            }
            LogA.d(TAG, "Everything worked out as expected!!!");
        } else {
            LogA.d(TAG, "Fail! Oat file not created.");
        }
        return success;
    }

    private void reportProgressDetails(String message) {
        for (ProgressListener listener : mProgressListeners) {
            listener.reportProgressDetails(mRunConfig.app_package_name, message);
        }
    }
}
