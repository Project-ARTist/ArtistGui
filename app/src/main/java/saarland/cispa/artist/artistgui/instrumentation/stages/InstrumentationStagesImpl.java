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
import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import saarland.cispa.apksigner.ApkSigner;
import saarland.cispa.apksigner.ApkZipSir;
import saarland.cispa.artist.artistgui.instrumentation.exceptions.InstrumentationException;
import saarland.cispa.artist.artistgui.instrumentation.config.ArtistRunConfig;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressListener;
import saarland.cispa.artist.artistgui.utils.AndroidUtils;
import saarland.cispa.artist.artistgui.utils.ArtistUtils;
import saarland.cispa.artist.artistgui.utils.ProcessExecutor;
import saarland.cispa.dexterous.Dexterous;
import saarland.cispa.dexterous.MergeConfig;
import trikita.log.Log;

public class InstrumentationStagesImpl implements InstrumentationStages {

    private static final String TAG = "InstrumentationStages";

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
        AndroidUtils.createFoldersInFilesDir(mContext, mRunConfig.artist_exec_path);
        AndroidUtils.createFoldersInFilesDir(mContext, mRunConfig.artist_exec_path_libs_dir);

        cleanOldBuildFiles();
        setupKeystore();

        String pathDex2oat = extractDex2Oat();
        setupArtistLibraries();

        return pathDex2oat;
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
        Log.d(TAG, "setupKeystore()");
        reportProgressDetails("KeyStore: " + mRunConfig.keystore);
        AndroidUtils.copyAsset(mContext, mRunConfig.asset_path_keystore,
                mRunConfig.keystore.getAbsolutePath());
    }

    private String extractDex2Oat() throws InstrumentationException {
        final String pathDex2oat = copyAssetToFilesDir(mContext,
                mRunConfig.asset_path_dex2oat,
                mRunConfig.artist_exec_path_dex2oat);
        Log.i(TAG, "> pathDex2oat: " + pathDex2oat);
        Log.i(TAG, "  > config:    " + mRunConfig.artist_exec_path_dex2oat);

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
        Log.d(TAG, "setupArtistLibraries()");
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
        Log.d(TAG, String.format("base.odex UID: %s GID: %s Permissions: %s Size: %s",
                mRunConfig.oatOwner,
                mRunConfig.oatGroup,
                mRunConfig.oatPermissions,
                mRunConfig.stats.oatFileSizeOriginal));

        reportProgressDetails("Deleting existing oat file: " + mRunConfig.app_oat_file_path);
        boolean success = deleteRootFile(mRunConfig.app_oat_file_path);
        if (!success) {
            Log.d(TAG, String.format("Failed to delete old base oat: %s - Continue", mRunConfig.app_oat_file_path));
        }
    }

    private boolean deleteRootFile(final String filePath) {
        final String cmd_rm_root_file = "rm " + filePath;
        return ProcessExecutor.execute(cmd_rm_root_file, true,
                ProcessExecutor.processName(mRunConfig.app_package_name, "rm_rootfile"));
    }

    @Override
    public void mergeCodeLib() throws InstrumentationException {
        Log.d(TAG, "MergeCodeLib into: " + mRunConfig.app_apk_file_path);

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
            Log.d(TAG, String.format("MergeCodeLib DONE (%s)", pathToApk));

            pathToApkSigned = resignApk(pathToApk);
            Log.d(TAG, String.format("MergeCodeLib Signing DONE (%s)", pathToApkSigned));

            if (pathToApkSigned.isEmpty()) {
                throw new InstrumentationException("Codelib Merge Failed");
            }
        } else {
            reportProgressDetails("Not Injecting CodeLib");
            Log.i(TAG, "Skip CodeLib Injection");
            Log.d(TAG, "MergeCodeLib SKIPPED");
        }
    }

    private void setupCodeLib() {
        if (mRunConfig.codeLibName.startsWith(ArtistUtils.CODELIB_ASSET)) {
            Log.d(TAG, "setupCodeLib() " + mRunConfig.codeLibName);
            final String assetName = mRunConfig.codeLibName.replaceFirst(ArtistUtils.CODELIB_ASSET, "");
            AndroidUtils.copyAsset(mContext, "codelib" + File.separator + assetName,
                    mRunConfig.codeLib.getAbsolutePath());
            if (!mRunConfig.codeLib.exists()) {
                Log.e(TAG, " setupCodeLib: " + mRunConfig.codeLib + " FAILED");
            } else {
                Log.d(TAG, " setupCodeLib: " + mRunConfig.codeLib + " READY");
            }
        }
    }

    private String resignApk(final String unsignedApkPath) {
        Log.d(TAG, "resignApk() " + unsignedApkPath);

        String signedApkPath;
        final ApkSigner apkSir = new ApkZipSir(mRunConfig.app_apk_merged_signed_file_path);
        try {
            signedApkPath = apkSir.signApk(mRunConfig.keystore.getAbsolutePath(), unsignedApkPath);
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "> Signing of APK Failed", e);
            signedApkPath = "";
        }
        return signedApkPath;
    }

    @Override
    public void backupMergedApk() {
        if (!mRunConfig.BACKUP_APK_MERGED) {
            Log.v(TAG, "Skip: backupMergedApk()");
            return;
        }
        Log.v(TAG, "backupMergedApk()");

        final File sdcard = Environment.getExternalStorageDirectory();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date date = new Date();

        final String mergedApkBackupPath = sdcard.getAbsolutePath() + File.separator
                + mRunConfig.app_package_name + "_merged_signed_" + dateFormat.format(date) + ".apk";

        reportProgressDetails("Backing up Merged APK: " + mergedApkBackupPath);

        final String cmd_backup_merged_apk = "cp " + mRunConfig.app_apk_merged_signed_file_path +
                " " + mergedApkBackupPath;

        boolean success = ProcessExecutor.execute(cmd_backup_merged_apk, true,
                ProcessExecutor.processName(mRunConfig.app_package_name, "cp_backup_merged"));

        if (success) {
            Log.d(TAG, "backupMergedApk() Success: " + mergedApkBackupPath);
        } else {
            Log.e(TAG, "backupMergedApk() Failed:  " + mergedApkBackupPath);
        }
    }

    @Override
    public void runDex2OatCompilation(String pathDex2oat) throws InstrumentationException {
        final String cmd_dex2oat_compile = setupDex2oatCommand(pathDex2oat);

        Log.d(TAG, "dex2oat command:");
        Log.d(TAG, cmd_dex2oat_compile);
        Log.d(TAG, "Starting the compilation process!");
        Log.d(TAG, "> Result will get placed at: " + mRunConfig.app_oat_file_path);

        final String divider = "########################################################";

        Log.d(TAG, divider);
        Log.d(TAG, divider);
        Log.d(TAG, divider);

        boolean success = ProcessExecutor.execute(cmd_dex2oat_compile, true,
                ProcessExecutor.processName(mRunConfig.app_package_name, "dex2artist"));

        Log.d(TAG, divider);
        Log.d(TAG, divider);
        Log.d(TAG, divider);

        if (success) {
            Log.d(TAG, "Compilation was successfull");
        } else {
            Log.d(TAG, "Compilation failed...");
            throw new InstrumentationException("Artist Injection Failed");
        }
    }

    private String setupDex2oatCommand(final String pathDex2oat) {
        String cmd_dex2oat_compile =
                "export LD_LIBRARY_PATH=" + mContext.getApplicationInfo().nativeLibraryDir + ":"
                        + AndroidUtils.getFilesDirLocation(mContext, mRunConfig.artist_exec_path_libs_dir) + ";"
                        + pathDex2oat
                        + " --oat-file=" + mRunConfig.app_oat_file_path
                        + " --compiler-backend=Optimizing"
                        + " --compiler-filter=everything"
                        + " --generate-debug-info"
                        + " --compile-pic";

        cmd_dex2oat_compile += " --dex-file=" + (mRunConfig.app_apk_file_path);
        cmd_dex2oat_compile += " --checksum-rewriting";
        cmd_dex2oat_compile += " --dex-location=" + mRunConfig.app_apk_file_path;

        if (mRunConfig.COMPILER_THREADS != -1) {
            cmd_dex2oat_compile += " -j" + mRunConfig.COMPILER_THREADS;
        }

        Log.d(TAG, "Dex2oat: Compiler Threads: " + mRunConfig.COMPILER_THREADS);


        if (mRunConfig.app_oat_architecture.contains("arm64")) {
            // ARM64 Special Flags
            cmd_dex2oat_compile += " --instruction-set=arm64";
            cmd_dex2oat_compile += " --instruction-set-features=smp,a53";
            cmd_dex2oat_compile += " --instruction-set-variant=denver64";
            cmd_dex2oat_compile += " --instruction-set-features=default";
            // ARM64 Special Flags END
            Log.d(TAG, "Compiling for 64bit Architecture!");
        }
        return cmd_dex2oat_compile;
    }

    @Override
    public boolean setOatFilePermissions() {
        boolean success = false;
        final File oatFile = new File(this.mRunConfig.app_oat_file_path);
        if (oatFile.exists() && !oatFile.isDirectory()) {
            Log.d(TAG, "Success! Oat file created.");
            reportProgressDetails("Fixing oat file permissions");

            mRunConfig.stats.oatFileSizeRecompiled = oatFile.length();

            reportProgressDetails("odex OLD size: " + mRunConfig.stats.oatFileSizeOriginal);
            Log.d(TAG, "odex OLD size: " + mRunConfig.stats.oatFileSizeOriginal);
            reportProgressDetails("odex NEW size: " + mRunConfig.stats.oatFileSizeRecompiled);
            Log.d(TAG, "odex NEW size: " + mRunConfig.stats.oatFileSizeRecompiled);

            Log.d(TAG, "Changing the owner of the oat file to " + mRunConfig.oatOwner);

            final String cmd_chown_oat = "chown " + mRunConfig.oatOwner + " " + this.mRunConfig.app_oat_file_path;
            success = ProcessExecutor.execute(cmd_chown_oat, true, ProcessExecutor.processName(mRunConfig.app_package_name, "chown_oatfile"));

            if (!success) {
                Log.d(TAG, "Could not change oat owner to " + mRunConfig.oatOwner + "... ");
                Log.d(TAG, "... for path " + this.mRunConfig.app_oat_file_path);
                return success;
            }
            Log.d(TAG, "Changing the group of the oat file to " + mRunConfig.oatGroup);

            final String cmd_chgrp_oat = "chgrp " + mRunConfig.oatGroup + " " + this.mRunConfig.app_oat_file_path;
            success = ProcessExecutor.execute(cmd_chgrp_oat, true, ProcessExecutor.processName(mRunConfig.app_package_name, "chgrp_oatfile"));

            if (!success) {
                Log.d(TAG, "Could not change oat group to " + mRunConfig.oatGroup + "... ");
                Log.d(TAG, "... for path " + this.mRunConfig.app_oat_file_path);
                return success;
            }

            success = AndroidUtils.chmodExecutable(this.mRunConfig.app_oat_file_path);

            if (!success) {
                Log.d(TAG, "Could not change oat permissions to 777");
                Log.d(TAG, "... for path " + this.mRunConfig.app_oat_file_path);
                return success;
            }
            Log.d(TAG, "Everything worked out as expected!!!");
        } else {
            Log.d(TAG, "Fail! Oat file not created.");
        }
        return success;
    }

    private void reportProgressDetails(String message) {
        for (ProgressListener listener : mProgressListeners) {
            listener.reportProgressDetails(mRunConfig.app_package_name, message);
        }
    }
}
