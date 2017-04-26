/**
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
 * @author "Oliver Schranz <oliver.schranz@cispa.saarland>"
 * @author "Sebastian Weisgerber <weisgerber@cispa.saarland>"
 *
 */
package saarland.cispa.artist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com2.android.dex.Dex;
import com2.android.dx.merge.CollisionPolicy;
import com2.android.dx.merge.DexMerger;
import saarland.cispa.apksigner.ApkSigner;
import saarland.cispa.apksigner.ApkZipSir;
import saarland.cispa.artist.gui.artist.ArtistGuiProgress;
import saarland.cispa.artist.log.Logg;
import saarland.cispa.artist.settings.ArtistRunConfig;
import saarland.cispa.artist.utils.AndroidUtils;
import saarland.cispa.artist.utils.ArtistInterruptedException;
import saarland.cispa.artist.utils.ArtistUtils;
import saarland.cispa.artist.utils.CompilationException;
import saarland.cispa.artist.utils.ProcessExecutor;
import saarland.cispa.dexterous.Dexterous;
import trikita.log.Log;

/**
 * @author Sebastian Weisgerber (weisgerber@cispa.saarland)
 * @author Oliver Schranz (oliver.schranz@cispa.saarland)
 */
public class ArtistImpl implements Artist {

    private static final String TAG = "ArtistImpl";

    public static final String INTENT_EXTRA_APP_NAME = "AppPackageName";
    public static final String INTENT_EXTRA_FAIL_REASON = "FailReason";

    public final static int COMPILATION_SUCCESS = 1;
    public final static int COMPILATION_CANCELED = 0;
    public final static int COMPILATION_ERROR = -1;

    private ArtistRunConfig config;

    private ArtistGuiProgress guiProgress = null;

    public ArtistImpl(final ArtistRunConfig artistRunConfig) {
        this.config = artistRunConfig;
    }

    @Override
    public void addGuiProgressListener(@Nullable final ArtistGuiProgress callback) {
        this.guiProgress = callback;
    }

    public void progressUpdate(final int progress, final String message) {
        if (guiProgress != null) {
            guiProgress.updateProgress(progress, message);
        }
    }

    public void progressUpdateVerbose(final int progress, final String message) {
        if (guiProgress != null) {
            guiProgress.updateProgressVerbose(progress, message);
        }
    }

    public void progressFailed(final String message) {
        if (guiProgress != null) {
            guiProgress.doneFailed(message);
        }
    }

    public void progressSucess(final String message) {
        if (guiProgress != null) {
            guiProgress.doneSuccess(message);
        }
    }

    @Override
    public void init(final Context context) {
        AndroidUtils.createFoldersInFilesDir(context, config.artist_exec_path);
        AndroidUtils.createFoldersInFilesDir(context, config.artist_exec_path_libs_dir);
    }

    @Override
    public boolean TestRun(Activity activity) {
        Log.i(TAG, "TestRun() compiling and starting " + config.app_name);
        setupArtistLibraries(activity.getApplicationContext(), config);
        Log.i(TAG, "TestRun() compiling and starting " + config.app_name + " DONE");
        return true;
    }

    /**
     * @param context
     * @param config
     * @return
     * @throws IOException
     */
    @Override
    public boolean mergeCodeLib(final Context context,
                                final ArtistRunConfig config) {
        Log.d(TAG, "MergeCodeLib into: " + config.app_apk_file_path);

        String pathToApkSigned;
        // deactivate injection upon user wish or if no code lib is provided
        if (config.INJECT_CODELIB && config.codeLib != null) {
            progressUpdateVerbose(-1, "Injecting CodeLib");
            final File appApk = new File(config.app_apk_file_path);
            final File codeLibApk = config.codeLib;

            setupCodeLib(context);

            Dexterous dexterous = new Dexterous(config);
            dexterous.init(appApk, codeLibApk);
            dexterous.mergeCodeLib();
            final String pathToApk = dexterous.buildApk();
            progressUpdateVerbose(-1, "Resigning APK");
            Log.d(TAG, String.format("MergeCodeLib DONE (%s)", pathToApk));

            pathToApkSigned = resignApk(pathToApk);
            Log.d(TAG, String.format("MergeCodeLib Signing DONE (%s)", pathToApkSigned));
            return !pathToApkSigned.isEmpty();
        } else {
            progressUpdateVerbose(-1, "Not Injecting CodeLib");
            Log.i(TAG, String.format("Skip CodeLib Injection " +
                    "(Inject CodeLib: %b)",
                    config.INJECT_CODELIB));
            Log.d(TAG, "MergeCodeLib SKIPPED");
            return true;
        }
    }

    private String resignApk(final String unsignedApkPath) {
        Log.d(TAG, "resignApk() " + unsignedApkPath);

        String signedApkPath;
        final ApkSigner apkSir = new ApkZipSir(config);
        try {
            signedApkPath = apkSir.signApk(config.keystore.getAbsolutePath(), unsignedApkPath);
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "> Signing of APK Failed", e);
            signedApkPath = "";
        }
        return signedApkPath;
    }

    @Override
    public void signApk() {

    }

    @Override
    public boolean Run(final Activity activity) {
        return Run(activity, activity.getApplicationContext());
    }

    public boolean Run(final Context context) {
        return Run(null, context);
    }

    @Override
    public boolean Run(final Activity activity, final Context context) {
        Log.i(TAG, "Run() compiling and starting " + config.app_name);
        Log.i(TAG, "> app_name:    " + config.app_name);
        Log.i(TAG, "> apkPath:     " + config.app_apk_file_path);
        Log.i(TAG, "> packageName: " + config.app_package_name);
        Log.i(TAG, "> codeLibName: " + config.codeLibName);
        Log.i(TAG, "> Keystore:    " + config.keystore);

        boolean success = false;
        try {
            progressUpdate(10, "Cleaning Build Files");

            ArtistCompilationTask.checkThreadCancellation();

            cleanBuildFiles();

            progressUpdate(20, "Setup Keystore");

            ArtistCompilationTask.checkThreadCancellation();

            setupKeystore(context);

            progressUpdate(30, "Setup dex2artist");

            ArtistCompilationTask.checkThreadCancellation();

            final String pathDex2oat = copyAssetToFilesDir(context,
                    config.asset_path_dex2oat,
                    config.artist_exec_path_dex2oat);
            Log.i(TAG, "> pathDex2oat: " + pathDex2oat);
            Log.i(TAG, "  > config:    " + config.artist_exec_path_dex2oat);

            if (pathDex2oat.isEmpty()) {
                throw new CompilationException("Artist: Dex2oat Setup failed");
            }

            ArtistCompilationTask.checkThreadCancellation();

            setupArtistLibraries(context, config);

            ArtistCompilationTask.checkThreadCancellation();

            if (ArtistUtils.isMultiDex(config.app_apk_file_path) && config.MULTIDEX_ABORT) {
                progressUpdateVerbose(-1, "Aborting Compilation: MultiDex APK found");
                throw new CompilationException(String.format("Run() Multidex ABORT (APK: %s)", config.app_apk_file_path));
            }

            ArtistCompilationTask.checkThreadCancellation();

            probeOatFilePermissions();

            ArtistCompilationTask.checkThreadCancellation();

            deleteOatFile();

            progressUpdate(40, "Merging CodeLib");

            ArtistCompilationTask.checkThreadCancellation();

            success = mergeCodeLib(context, config);
            if (!success) {
                throw new CompilationException("Codelib Merge Failed");
            }

            ArtistCompilationTask.checkThreadCancellation();

            backupMergedApk(this.config);

            ArtistCompilationTask.checkThreadCancellation();

            backupOriginalApk(this.config);

            if (this.config.REPLACE_BASE_APK) {
                Log.i(TAG, "Replacing the original base.apk");
                replaceAppApkWithMergedApk(context, config);
            } else {
                Log.i(TAG, "Leaving the original base.apk untouched");
            }
            progressUpdate(50, "Compiling: " + config.app_name);

            ArtistCompilationTask.checkThreadCancellation();

            success = recompileApp(context, pathDex2oat);
            if (!success) {
                throw new CompilationException("Artist Injection Failed");
            }
            progressUpdate(90, "Compilation done, Cleaning");

            ArtistCompilationTask.checkThreadCancellation();

            success = fixOatFilePermissions(config);

            ArtistCompilationTask.checkThreadCancellation();

            cleanBuildFiles();

        } catch (final CompilationException|ArtistInterruptedException e) {
            Log.e(TAG, "Artist Run() FAILED " + e.getMessage());
            progressFailed(String.format("Injection: %s Failed (%s)", config.app_name, e.getMessage()));
            cleanBuildFiles();
            success = false;
            return success;
        } catch (final Exception e) {
            Log.e(TAG, "Artist Run() FAILED: ", e);
            progressFailed(String.format("Injection: %s Failed", config.app_name));
            success = false;
            return success;
        }
        final String userMessage = String.format("Injection: %s OK: %b", config.app_name, success);
        Log.d(TAG, userMessage);
        progressSucess(userMessage);
        return success;
    }

    private void deleteOatFile() {
        progressUpdateVerbose(-1, "Deleting existing oat file: " + config.app_oat_file_path);
        boolean success = deleteRootFile(config.app_oat_file_path);
        if (!success) {
            Log.d(TAG, String.format("Failed to delete old base oat: %s - Continue", config.app_oat_file_path));
        }
    }

    private void cleanBuildFiles() {
        progressUpdateVerbose(-1, "Deleting Build Relicts.");
        progressUpdateVerbose(-1, "Deleting: " + config.app_apk_merged_file_path);
        AndroidUtils.deleteExistingFile(config.app_apk_merged_file_path);
        progressUpdateVerbose(-1, "Deleting: " + config.app_apk_merged_signed_file_path);
        AndroidUtils.deleteExistingFile(config.app_apk_merged_signed_file_path);
        if (config.isAssetCodeLib()) {
            progressUpdateVerbose(-1, "Deleting: " + config.codeLib);
            AndroidUtils.deleteExistingFile(config.codeLib);
        }
    }

    private void setupArtistLibraries(final Context context, final ArtistRunConfig config) {
        Log.d(TAG, "setupArtistLibraries()");
        progressUpdateVerbose(-1, "Copying Libraries to: " + config.artist_exec_path_libs_dir);
        // Example Permissions: /data/app/com.deepinc.arte360-1/lib/arm/
        // -rwxr-xr-x 1 system system  511044 2016-05-05 17:30 libTBAudioEngine.so
        // -rwxr-xr-x 1 system system  771432 2016-05-05 17:30 libmetadataparser.so
        // -rwxr-xr-x 1 system system 1117296 2016-05-05 17:30 libvideorenderer.so
        // -rwxr-xr-x 1 system system  677784 2016-05-05 17:30 libvrtoolkit.so
        AndroidUtils.copyAssetFolderContent(context,
                config.asset_path_dex2oat_libs,
                config.artist_exec_path_libs_dir);
    }

    public boolean recompileApp(Context context, String pathDex2oat) {
        boolean success;
        final String cmd_dex2oat_compile = setupDex2oatCommand(context, pathDex2oat);

        Log.d(TAG, "dex2oat command:");
        Log.d(TAG, cmd_dex2oat_compile);
        Log.d(TAG, "Starting the compilation process!");
        Log.d(TAG, "> Result will get placed at: " + config.app_oat_file_path);

        Log.d(TAG, Logg.BigDivider());

        success = ProcessExecutor.execute(cmd_dex2oat_compile, true,
                ProcessExecutor.processName(config.app_name, "dex2artist"));

        Log.d(TAG, Logg.BigDivider());

        if (success) {
            Log.d(TAG, "Compilation was successfull");
        } else {
            Log.d(TAG, "Compilation failed...");
        }
        return success;
    }

    public void probeOatFilePermissions() {
        progressUpdateVerbose(-1, "Probing oat file permissions: " + config.app_oat_file_path);
        config.oatOwner = AndroidUtils.getFileOwnerUid(config.app_oat_file_path);
        config.oatGroup = AndroidUtils.getFileGroupId(config.app_oat_file_path);
        config.oatPermissions = AndroidUtils.getFilePermissions(config.app_oat_file_path);
        config.stats.oatFileSizeOriginal = new File(config.app_oat_file_path).length();
        Log.d(TAG, String.format("base.odex UID: %s GID: %s Permissions: %s Size: %s",
                config.oatOwner,
                config.oatGroup,
                config.oatPermissions,
                config.stats.oatFileSizeOriginal));
    }

    public boolean fixOatFilePermissions(final ArtistRunConfig config) {
        boolean success = false;
        final File oatFile = new File(this.config.app_oat_file_path);
        if (oatFile.exists() && !oatFile.isDirectory()) {
            Log.d(TAG, "Success! Oat file created.");
            progressUpdateVerbose(-1, "Fixing oat file permissions");

            config.stats.oatFileSizeRecompiled = oatFile.length();

            progressUpdateVerbose(-1, "odex OLD size: " + config.stats.oatFileSizeOriginal);
            Log.d(TAG, "odex OLD size: " + config.stats.oatFileSizeOriginal);
            progressUpdateVerbose(-1, "odex NEW size: " + config.stats.oatFileSizeRecompiled);
            Log.d(TAG, "odex NEW size: " + config.stats.oatFileSizeRecompiled);

            Log.d(TAG, "Changing the owner of the oat file to " + config.oatOwner);

            final String cmd_chown_oat = "chown " + config.oatOwner + " " + this.config.app_oat_file_path;
            success = ProcessExecutor.execute(cmd_chown_oat, true, ProcessExecutor.processName(config.app_name, "chown_oatfile"));

            if (!success) {
                Log.d(TAG, "Could not change oat owner to " + config.oatOwner + "... ");
                Log.d(TAG, "... for path " + this.config.app_oat_file_path);
                return success;
            }
            Log.d(TAG, "Changing the group of the oat file to " + config.oatGroup);

            final String cmd_chgrp_oat = "chgrp " + config.oatGroup + " " + this.config.app_oat_file_path;
            success = ProcessExecutor.execute(cmd_chgrp_oat, true, ProcessExecutor.processName(config.app_name, "chgrp_oatfile"));

            if (!success) {
                Log.d(TAG, "Could not change oat group to " + config.oatGroup + "... ");
                Log.d(TAG, "... for path " + this.config.app_oat_file_path);
                return success;
            }

            success = AndroidUtils.chmodExecutable(this.config.app_oat_file_path);

            if (!success) {
                Log.d(TAG, "Could not change oat permissions to 777");
                Log.d(TAG, "... for path " + this.config.app_oat_file_path);
                return success;
            }
            Log.d(TAG, "Everything worked out as expected!!!");
        } else {
            Log.d(TAG, "Fail! Oat file not created.");
        }
        return success;
    }

    public String setupDex2oatCommand(final Context context, final String pathDex2oat) {
        String cmd_dex2oat_compile =
                "export LD_LIBRARY_PATH=" + context.getApplicationInfo().nativeLibraryDir + ":"
                        + AndroidUtils.getFilesDirLocation(context, config.artist_exec_path_libs_dir) + ";"
                        + pathDex2oat
                        //+ " --runtime-arg -Xzygote"
                        //+ " --runtime-arg -Xnoimage-dex2oat"
                        //+ " --runtime-arg -Ximage:"+bootImageDir.getAbsolutePath()+File.separatorChar+bootImage+".art"
                        + " --oat-file=" + config.app_oat_file_path
                        + " --compiler-backend=Optimizing"
                        + " --compiler-filter=everything"
                        + " --generate-debug-info"
                        + " --compile-pic";
        if (this.config.REPLACE_BASE_APK) {
            cmd_dex2oat_compile += " --dex-file="  + config.app_apk_file_path;
            cmd_dex2oat_compile += " --dex-location="  + config.app_apk_file_path;
        } else {
            cmd_dex2oat_compile += " --dex-file="  + config.app_apk_merged_signed_file_path;
            cmd_dex2oat_compile += " --dex-location="  + config.app_apk_file_path;
        }

        if (config.COMPILER_THREADS != -1) {
            cmd_dex2oat_compile += " -j" + config.COMPILER_THREADS;
        }

        Log.d(TAG, "Dex2oat: Compiler Threads: " + config.COMPILER_THREADS);

        if (config.CODELIB_SETTINGS_APPCOMPART) {
            String launchActivity = getAppEntrance(context, config.app_name);
            Log.d(TAG, "Dex2oat: CodeLib Special: --launch-activity: " + launchActivity);
            cmd_dex2oat_compile += " --launch-activity=" + launchActivity;
        }

        if (config.app_oat_architecture.contains("arm64")) {
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

    public boolean deleteRootFile(final String filePath) {
        boolean success = false;
        final String cmd_rm_root_file = "rm " + filePath;
        success = ProcessExecutor.execute(cmd_rm_root_file, true, ProcessExecutor.processName(config.app_name, "rm_rootfile"));
        return success;
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

    private void setupKeystore(final Context context) {
        Log.d(TAG, "setupKeystore()");
        progressUpdateVerbose(-1, "KeyStore: " + config.keystore);
        AndroidUtils.copyAsset(context, config.asset_path_keystore, config.keystore.getAbsolutePath());
    }

    public void RunBootImage(final Context context, final String bootImgName) {

        File imageLibs = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + "ImageLibs" + File.separatorChar);
        imageLibs.mkdir();

        File bootImageDir = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + "BootImages" + File.separatorChar);
        File bootImageIsaDir = new File(bootImageDir.getAbsolutePath() + File.separatorChar + "arm" + File.separatorChar);
        bootImageDir.mkdir();
        bootImageIsaDir.mkdir();

        String imageLibsDir = imageLibs.getAbsolutePath();
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "core-libart.jar", imageLibsDir + File.separatorChar + "core-libart.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "TaintLib.jar", imageLibsDir + File.separatorChar + "TaintLib.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "conscrypt.jar", imageLibsDir + File.separatorChar + "conscrypt.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "okhttp.jar", imageLibsDir + File.separatorChar + "okhttp.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "core-junit.jar", imageLibsDir + File.separatorChar + "core-junit.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "bouncycastle.jar", imageLibsDir + File.separatorChar + "bouncycastle.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "ext.jar", imageLibsDir + File.separatorChar + "ext.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "framework.jar", imageLibsDir + File.separatorChar + "framework.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "telephony-common.jar", imageLibsDir + File.separatorChar + "telephony-common.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "voip-common.jar", imageLibsDir + File.separatorChar + "voip-common.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "ims-common.jar", imageLibsDir + File.separatorChar + "ims-common.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "apache-xml.jar", imageLibsDir + File.separatorChar + "apache-xml.jar");
        AndroidUtils.copyAsset(context, "ImageLibs" + File.separatorChar + "org.apache.http.legacy.boot.jar", imageLibsDir + File.separatorChar + "org.apache.http.legacy.boot.jar");


        String args = "--runtime-arg "
                + "-Xms64m "
                + "--runtime-arg"
                + "-Xmx64m "
                + "--image-classes=frameworks/base/preloaded-classes "
                /*"--image-classes=frameworks/base/preloaded-classes " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/conscrypt_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/okhttp_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/core-junit_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/bouncycastle_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/telephony-common_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/voip-common_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/ims-common_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/apache-xml_intermediates/javalib.jar " +
                "--dex-file=out/target/common/obj/JAVA_LIBRARIES/org.apache.http.legacy.boot_intermediates/javalib.jar " +
*/
/*
                "--dex-location=/system/framework/core-libart.jar " +
                "--dex-location=/system/framework/conscrypt.jar " +
                "--dex-location=/system/framework/okhttp.jar " +
                "--dex-location=/system/framework/core-junit.jar " +
                "--dex-location=/system/framework/bouncycastle.jar " +
                "--dex-location=/system/framework/ext.jar " +
                "--dex-location=/system/framework/framework.jar " +
                "--dex-location=/system/framework/telephony-common.jar " +
                "--dex-location=/system/framework/voip-common.jar " +
                "--dex-location=/system/framework/ims-common.jar " +
                "--dex-location=/system/framework/apache-xml.jar " +
                "--dex-location=/system/framework/org.apache.http.legacy.boot.jar " +
*/
                + "--dex-file=" + imageLibsDir + File.separatorChar + "core-libart.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "TaintLib.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "conscrypt.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "okhttp.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "core-junit.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "bouncycastle.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "ext.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "framework.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "telephony-common.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "voip-common.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "ims-common.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "apache-xml.jar " +
                "--dex-file=" + imageLibsDir + File.separatorChar + "org.apache.http.legacy.boot.jar " +
//                    "--dex-file=/system/framework/conscrypt.jar " +
//                    "--dex-file=/system/framework/okhttp.jar " +
//                    "--dex-file=/system/framework/core-junit.jar " +
//                    "--dex-file=/system/framework/bouncycastle.jar " +
//                    "--dex-file=/system/framework/ext.jar " +
//                    "--dex-file=/system/framework/framework.jar " +
//                    "--dex-file=/system/framework/telephony-common.jar " +
//                    "--dex-file=/system/framework/voip-common.jar " +
//                    "--dex-file=/system/framework/ims-common.jar " +
//                    "--dex-file=/system/framework/apache-xml.jar " +
//                    "--dex-file=/system/framework/org.apache.http.legacy.boot.jar " +

                //"--oat-symbols=out/target/product/hammerhead/symbols/system/framework/arm/boot.oat " +
                //"--oat-file=out/target/product/hammerhead/dex_bootjars/system/framework/arm/boot.oat " +
                //"--oat-location=/system/framework/arm/taint_aware_boot.oat " +
                "--oat-file=" + bootImageIsaDir + File.separatorChar + bootImgName + ".oat " +
                //"--image=out/target/product/hammerhead/dex_bootjars/system/framework/arm/boot.art " +
                "--image=" + bootImageIsaDir + File.separatorChar + bootImgName + ".art " +
                "--base=0x70000000 " +
                "--instruction-set=arm " +
                "--instruction-set-variant=krait " +
                "--instruction-set-features=default " +
                //"--android-root=out/target/product/hammerhead/system " +
                "--include-patch-information " +
                "--runtime-arg -Xnorelocate " +
                //"--no-generate-debug-info";
                "--generate-debug-info " +
                "--compiler-backend=Optimizing " +
                "--dump-timing ";
        if (config.COMPILER_THREADS != -1) {
            args += " -j" + config.COMPILER_THREADS;
        }
            /*
            dex2oat-cmdline =
            --runtime-arg -Xms64m
            --runtime-arg -Xmx64m
            --image-classes=frameworks/base/preloaded-classes
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/conscrypt_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/okhttp_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/core-junit_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/bouncycastle_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/telephony-common_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/voip-common_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/ims-common_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/apache-xml_intermediates/javalib.jar
            --dex-file=out/target/common/obj/JAVA_LIBRARIES/org.apache.http.legacy.boot_intermediates/javalib.jar

            --dex-location=/system/framework/core-libart.jar --dex-location=/system/framework/conscrypt.jar --dex-location=/system/framework/okhttp.jar --dex-location=/system/framework/core-junit.jar --dex-location=/system/framework/bouncycastle.jar --dex-location=/system/framework/ext.jar --dex-location=/system/framework/framework.jar --dex-location=/system/framework/telephony-common.jar --dex-location=/system/framework/voip-common.jar --dex-location=/system/framework/ims-common.jar --dex-location=/system/framework/apache-xml.jar --dex-location=/system/framework/org.apache.http.legacy.boot.jar --oat-symbols=out/target/product/hammerhead/symbols/system/framework/arm/boot.oat --oat-file=out/target/product/hammerhead/dex_bootjars/system/framework/arm/boot.oat --oat-location=/system/framework/arm/boot.oat --image=out/target/product/hammerhead/dex_bootjars/system/framework/arm/boot.art --base=0x70000000 --instruction-set=arm --instruction-set-variant=krait --instruction-set-features=default --android-root=out/target/product/hammerhead/system --include-patch-information --runtime-arg -Xnorelocate --no-generate-debug-info
             */


        String binTarget = AndroidUtils.getFilesDirLocation(context, "artist/android-25/dex2oat");
        Log.d(TAG, "Copying the dex2oatd binary from assets to app files dir");
        AndroidUtils.copyAsset(context, "artist/android-25/dex2oat", binTarget);

        final String cmd_chmod_777 = "chmod 777 " + binTarget;
        Log.d(TAG, "Changing rights of dex2oat file to 777");
        boolean success = ProcessExecutor.execute(cmd_chmod_777, true,ProcessExecutor.processName(config.app_name, "chmod_777"));
        if (success) {
            Log.d(TAG, "Successfully changed the file rights of dex2oat");
        } else {
            Log.d(TAG, "Changing the file rights for dex2oat failed.");
            return;
        }

        final String cmd_export_ld = "export LD_LIBRARY_PATH="
                + context.getApplicationInfo().nativeLibraryDir + ";" + binTarget + " " + args;

        Log.d(TAG, "dex2oat command:");
        Log.d(TAG, cmd_export_ld);
        Log.d(TAG, "Starting the compilation process!");


        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);

        success = ProcessExecutor.execute(cmd_export_ld, true, ProcessExecutor.processName(config.app_name, "dex2artist"));

        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);
        Log.d(TAG, Logg.HR);

        if (success) {
            Log.d(TAG, Logg.HR);
            Log.d(TAG, "Compilation was successfull");
        } else {
            Log.d(TAG, Logg.HR);
            Log.d(TAG, "Compilation failed...");
            return;
        }
    }

    public void RunExternalApk(final Context context, final String apkAssetFileName, final String bootImage) {
        try {

            ////////////////////////////////// copy policy files /////////////////////////////////////////////////////////////

            Log.d(TAG, "compiling and starting " + apkAssetFileName);

            final String originsFile = AndroidUtils.getFilesDirLocation(context, "config/TaintOriginsPolicyFile");
            final String sinksFile = AndroidUtils.getFilesDirLocation(context, "config/GlobalTaintSinksPolicyFile");

            AndroidUtils.copyAsset(context, "config/TaintOriginsPolicyFile", originsFile);
            Log.d(TAG, "Copied TaintOriginsPolicyFile to " + originsFile);

            AndroidUtils.copyAsset(context, "config/GlobalTaintSinksPolicyFile", sinksFile);
            Log.d(TAG, "Copied GlobalTaintSinksPolicyFile to " + sinksFile);

            ////////////////////////////////// prepare dex2oat ///////////////////////////////////////////////////////////////

            final String binDex2oat = AndroidUtils.getFilesDirLocation(context, "artist/android-25/dex2oat");

            File binDex2oatFile = new File(binDex2oat);

            if (binDex2oatFile.exists()) {
                binDex2oatFile.delete();
            }

            Log.d(TAG, "Copying the dex2oat binary from assets to app files dir");
            AndroidUtils.copyAsset(context, "artist/android-25/dex2oat", binDex2oat);


            final String cmd_chmod777 = "chmod 777 " + binDex2oat;
            Log.d(TAG, "Changing rights of dex2oat file to 777");
            boolean success = ProcessExecutor.execute(cmd_chmod777, true, ProcessExecutor.processName(config.app_name, "chmod_777"));
            if (success) {
                Log.d(TAG, "Successfully changed the file rights of dex2oat");
            } else {
                Log.d(TAG, "Changing the file rights for dex2oat failed.");
                return;
            }

            ////////////////////////////////// prepare APKs ///////////////////////////////////////////////////////////////////

            final String apkPath = AndroidUtils.getFilesDirLocation(context, apkAssetFileName);

            AndroidUtils.copyAsset(context, "Apps" + File.separatorChar + apkAssetFileName, apkPath);
            PackageInfo info = context.getPackageManager().getPackageArchiveInfo(apkPath, 0);
            String packageName = info.packageName;

            final String codeLibPath = AndroidUtils.getFilesDirLocation(context, "codelib.apk");
            AndroidUtils.copyAsset(context, "artist" + File.separatorChar + "codelib.apk", codeLibPath);

            Log.d(TAG, "apkPath: " + apkPath);
            Log.d(TAG, "assetPath: Apps" + File.separatorChar + apkAssetFileName);
            Log.d(TAG, "packageName: " + packageName);

            final Dex appApkBuffer = new Dex(new File(apkPath));
            final Dex codeLibApkBuffer = new Dex(new File(codeLibPath));

            final Dex[] allDexes = {
                    appApkBuffer,
                    codeLibApkBuffer
            };

            DexMerger merger = new DexMerger(allDexes, CollisionPolicy.FAIL);
            Dex merged = merger.merge();
            String mergedApkPath = apkPath.replace(".apk", "_merged.apk");

            InputStream is = new FileInputStream(new File(apkPath));
            ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(is));

            OutputStream os = new FileOutputStream(new File(mergedApkPath));
            ZipOutputStream zipOutput = new ZipOutputStream(new BufferedOutputStream(os));

            try {
                ZipEntry apkContent;
                while ((apkContent = zipInput.getNextEntry()) != null) {

                    if (apkContent.getName().contains("classes.dex")) {

                        Log.d(TAG, "Replacing original " + apkContent.getName() + " with merged one");
                        ZipEntry newClassesEntry = new ZipEntry("classes.dex");
                        zipOutput.putNextEntry(newClassesEntry);
                        zipOutput.write(merged.getBytes(), 0, merged.getLength());
                        zipOutput.closeEntry();

                    } else {
                        Log.d(TAG, "copying " + apkContent.getName() + " without modification");
                        byte[] buffer = new byte[1024];
                        int count;
                        zipOutput.putNextEntry(apkContent);
                        while ((count = zipInput.read(buffer)) != -1) {
                            zipOutput.write(buffer, 0, count);
                        }
                        zipOutput.closeEntry();
                    }
                }
            } finally {
                zipInput.close();
                zipOutput.close();
            }

            final String oatbaseFolder = File.separator + "data"
                    + File.separator + "app"
                    + File.separator + packageName + "-1"
                    + File.separator + "oat";
            // --oat-file=/data/dalvik-cache/arm/data@app@de.infsec.tainttracking.taintmeapp-1@base.apk@classes.dex
            final String odexDest = oatbaseFolder + File.separator + AndroidUtils.probeArchitetureFolderName(oatbaseFolder) + File.separator  + "base.odex";

            // odexDest = "/data/dalvik-cache/arm/data@app@de.infsec.tainttracking.taintmeapp-1@base.apk@classes.dex";
            String oatOwner = AndroidUtils.getFileOwnerUid(odexDest);
            String oatGroup = AndroidUtils.getFileGroupId(odexDest);

            final long odexOriginalSize = new File(odexDest).length();

            // Move compiled dex here: >>odexDest
            success = deleteRootFile(odexDest);

            if (!success) {
                Log.d(TAG, "Failed to delete old base oat:");
                Log.d(TAG, odexDest);
                return;
            }

            String cmd_dex2oat_compile =
                    "export LD_LIBRARY_PATH=" + context.getApplicationInfo().nativeLibraryDir + ";"
                            + binDex2oat
                            //+ " --runtime-arg -Xzygote"
                            //+ " --runtime-arg -Xnoimage-dex2oat"
                            //+ " --runtime-arg -help"
                            //+ " --runtime-arg -Ximage:"+bootImageDir.getAbsolutePath()+File.separatorChar+bootImage+".art"
                            //+ " --dex-file=" + taintLibDexTarget
                            + " --dex-file=" + mergedApkPath
                            //+ " --dex-file=" + installedDexTarget
                            + " --oat-file=" + odexDest
                            + " --compiler-backend=Optimizing"
                            //+ " --boot-image="+bootImageFile.getAbsolutePath()
                            + " --compiler-filter=everything"
                            + " --generate-debug-info"
                            + " --compile-pic";
            if (config.COMPILER_THREADS != -1) {
                cmd_dex2oat_compile += " -j" + config.COMPILER_THREADS;
            }

            Log.d(TAG, "dex2oat command:");
            Log.d(TAG, cmd_dex2oat_compile);
            Log.d(TAG, "Starting the compilation process!");
            Log.d(TAG, "> Result will get placed at: " + odexDest);


            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);

            success = ProcessExecutor.execute(cmd_dex2oat_compile, true, ProcessExecutor.processName(config.app_name, "dex2artist"));

            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);
            Log.d(TAG, Logg.HR);


            if (success) {
                Log.d(TAG, Logg.HR);
                Log.d(TAG, "Compilation was successfull");
            } else {
                Log.d(TAG, Logg.HR);
                Log.d(TAG, "Compilation failed...");
                return;
            }

            File oatFile = new File(odexDest);
            if (oatFile.exists() && !oatFile.isDirectory()) {
                Log.d(TAG, "Success! Oat file created.");

                final long odexNewSize = oatFile.length();

                Log.d(TAG, "odex OLD size: " + odexOriginalSize);
                Log.d(TAG, "odex NEW size: " + odexNewSize);

                Log.d(TAG, "Changing the owner of the oat file to " + oatOwner);

                final String cmd_chown_oat = "chown " + oatOwner + " " + odexDest;
                success = ProcessExecutor.execute(cmd_chown_oat, true, ProcessExecutor.processName(config.app_name, "chown_oat"));

                if (!success) {
                    Log.d(TAG, "Could not change oat owner to " + oatOwner + "... ");
                    Log.d(TAG, "... for path " + odexDest);
                    return;
                }
                Log.d(TAG, "Changing the group of the oat file to " + oatGroup);

                final String cmd_chgrp_oat = "chgrp " + oatGroup + " " + odexDest;
                success = ProcessExecutor.execute(cmd_chgrp_oat, true, ProcessExecutor.processName(config.app_name, "chgrp_oat"));

                if (!success) {
                    Log.d(TAG, "Could not change oat group to " + oatGroup + "... ");
                    Log.d(TAG, "... for path " + odexDest);
                    return;
                }

                final String oat_permissions = "777";

                final String cmd_chmod_oat = "chmod " + oat_permissions + " " + odexDest;
                success = ProcessExecutor.execute(cmd_chmod_oat, true, ProcessExecutor.processName(config.app_name, "chmod_777"));

                if (!success) {
                    Log.d(TAG, "Could not change oat permissions to " + oat_permissions);
                    Log.d(TAG, "... for path " + odexDest);
                    return;
                }

                Log.d(TAG, "Everything worked out as expected!!!");

            } else {
                Log.d(TAG, "Fail! Oat file not created.");
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private String replaceAppApkWithMergedApk(final Context context,
                                              final ArtistRunConfig config) {
        Log.d(TAG, "replaceAppApkWithMergedApk()");
        progressUpdateVerbose(-1, "Replacing original APk with merged APK");
        final String packageName = config.app_package_name;
        final String apkPath = config.app_apk_file_path;
        final String mergedApkPath = config.app_apk_merged_signed_file_path;

        final String baseApkUid = AndroidUtils.getFileOwnerUid(apkPath);
        final String baseApkGid = AndroidUtils.getFileGroupId(apkPath);
        final String baseApkPerms = AndroidUtils.getFilePermissions(apkPath);

        Log.d(TAG, "APK: " + apkPath);
        Log.d(TAG, String.format("> UID: %s GID: %s (Permissions: %s)",
                baseApkUid, baseApkGid, baseApkPerms));

        boolean success = false;
        final String cmd_backup_base_apk = "rm " + apkPath;
        success = ProcessExecutor.execute(cmd_backup_base_apk, true, ProcessExecutor.processName(config.app_name, "rm_backup"));
        if (!success) {
            return "";
        }
        final String cmd_copy_merged_apk = "cp " + mergedApkPath + " " + apkPath;
        success = ProcessExecutor.execute(cmd_copy_merged_apk, true, ProcessExecutor.processName(config.app_name, "cp_backup_merged"));

        if (!success) {
            return "";
        }
        AndroidUtils.setFileUid(apkPath, baseApkUid);
        AndroidUtils.setFileGid(apkPath, baseApkGid);
        AndroidUtils.setFilePermissions(apkPath, baseApkPerms);

        return packageName;
    }

    private String setupMergedAppApk(final ArtistRunConfig config) {
        Log.d(TAG, "setupMergedAppApk()");
        String packageName = config.app_package_name;
        final String apkPathOriginal = config.app_apk_file_path;
        final String apkPathAlternative = config.app_apk_file_path_alternative;
        final String mergedApkPath = config.app_apk_merged_signed_file_path;
        Log.d(TAG, "setupMergedAppApk() APK: " + apkPathAlternative);

        final String baseApkUid = AndroidUtils.getFileOwnerUid(apkPathOriginal);
        final String baseApkGid = AndroidUtils.getFileGroupId(apkPathOriginal);
        final String baseApkPerms = AndroidUtils.getFilePermissions(apkPathOriginal);

        Log.d(TAG, String.format("> UID: %s GID: %s (Permissions: %s)",
                baseApkUid, baseApkGid, baseApkPerms));

//        final String cmd_delete_base_apk = "rm " + apkPathOriginal;
//        final boolean successs = ProcessExecutor.execute(cmd_delete_base_apk, true, ProcessExecutor.processName(config.app_name, "rm_backup"));
//        if (!successs ) {
//            return "";
//        }

        final String cmd_copy_merged_apk = "cp " + mergedApkPath + " " + apkPathAlternative;
        final boolean success = ProcessExecutor.execute(cmd_copy_merged_apk, true, ProcessExecutor.processName(config.app_name, "cp_backup_merged"));
        if (!success) {
            Log.d(TAG, "setupMergedAppApk() Failed");
            packageName =  "";
        }
        AndroidUtils.setFileUid(apkPathAlternative, baseApkUid);
        AndroidUtils.setFileGid(apkPathAlternative, baseApkGid);
        AndroidUtils.setFilePermissions(apkPathAlternative, baseApkPerms);

        return packageName;
    }

    public String getAppEntrance(final Context context, final String packageName) {
        Log.d(TAG, "getAppEntrance packageName: " + packageName);
        String launcherActivity = null;
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> packageInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        String applicationname = null;
        try {
            applicationname = context.getPackageManager().getApplicationInfo(packageName, 0).className;
            Log.i(TAG,  "ClassName: " + applicationname);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(applicationname != null) {
            launcherActivity = applicationname + ".onCreate";
            Log.i(TAG, "AppEntrance : " + launcherActivity);
            return launcherActivity;
        }
        for (ResolveInfo packageInfo : packageInfos) {
            String packagename = packageInfo.activityInfo.packageName;
            if(packageName.equals(packagename)) {
                launcherActivity = packageInfo.activityInfo.name + ".onCreate";
                Log.i(TAG, packagename + " : " + launcherActivity);
            }
        }
        return launcherActivity;
    }

    private void backupMergedApk(final ArtistRunConfig config) {
        if (!config.BACKUP_APK_MERGED) {
            Log.v(TAG, "Skip: backupMergedApk()");
            return;
        }
        Log.v(TAG, "backupMergedApk()");

        final File sdcard = Environment.getExternalStorageDirectory();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();

        final String mergedApkBackupPath = sdcard.getAbsolutePath() + File.separator
                + config.app_package_name + "_merged_signed_" + dateFormat.format(date) + ".apk";

        progressUpdateVerbose(-1, "Backing up Merged APK: " + mergedApkBackupPath);

        final String cmd_backup_merged_apk = "cp " + config.app_apk_merged_signed_file_path + " " + mergedApkBackupPath;

        boolean success = ProcessExecutor.execute(cmd_backup_merged_apk, true, ProcessExecutor.processName(config.app_name, "cp_backup_merged"));

        if (success) {
            Log.d(TAG, "backupMergedApk() Success: " + mergedApkBackupPath);
        } else {
            Log.e(TAG, "backupMergedApk() Failed:  " + mergedApkBackupPath);
        }
    }

    private void backupOriginalApk(final ArtistRunConfig config) {
        if (!config.BACKUP_APK_ORIGINAL) {
            Log.v(TAG, "Skip: backupOriginalApk()");
            return;
        }
        Log.v(TAG, "backupOriginalApk()");
        final File sdcard = Environment.getExternalStorageDirectory();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();

        final String originalApkBackupPath = sdcard.getAbsolutePath() + File.separator
                + config.app_package_name + "_original_" + dateFormat.format(date) + ".apk";
        final String cmd_backup_merged_apk = "cp " + config.app_apk_file_path + " " + originalApkBackupPath;

        progressUpdateVerbose(-1, "Backing up Merged APK: " + originalApkBackupPath);

        boolean success = ProcessExecutor.execute(cmd_backup_merged_apk, true, ProcessExecutor.processName(config.app_name, "cp_backup_merged"));

        if (success) {
            Log.d(TAG, "backupOriginalApk() Success: " + originalApkBackupPath);
        } else {
            Log.e(TAG, "backupOriginalApk() Failed:  " + originalApkBackupPath);
        }
    }

    public void setupCodeLib(final Context context) {
        if (config.codeLibName.startsWith(ArtistUtils.CODELIB_ASSET)) {
            Log.d(TAG, "setupCodeLib() " + config.codeLibName);
            final String assetName = config.codeLibName.replaceFirst(ArtistUtils.CODELIB_ASSET, "");
            AndroidUtils.copyAsset(context, "codelib" + File.separator + assetName, config.codeLib.getAbsolutePath());
            if (!config.codeLib.exists()) {
                Log.e(TAG, " setupCodeLib: " + config.codeLib + " FAILED");
            } else {
                Log.d(TAG, " setupCodeLib: " + config.codeLib + " READY");
            }
        }
    }
}
