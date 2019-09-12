/*
 * The ARTist Project (https://artist.cispa.saarland)
 * <p>
 * Copyright (C) 2017 CISPA (https://cispa.saarland), Saarland University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author "Oliver Schranz <oliver.schranz@cispa.saarland>"
 * @author "Sebastian Weisgerber <weisgerber@cispa.saarland>"
 */
package saarland.cispa.artist.artistgui.settings.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import saarland.cispa.artist.artistgui.database.Package;
import saarland.cispa.artist.artistgui.instrumentation.config.ArtistRunConfig;
import saarland.cispa.artist.artistgui.system.FrameworkPackages;
import saarland.cispa.artist.artistgui.utils.AndroidUtils;
import trikita.log.Log;

import static saarland.cispa.artist.artistgui.system.FrameworkPackages.getFiles;

/**
 * @author Sebastian Weisgerber (weisgerber@cispa.saarland)
 * @author Oliver Schranz (oliver.schranz@cispa.saarland)
 */

public class ArtistConfigFactory {

    private static final String TAG = "ArtistConfigFactory";

    private final static String PATH_ASSET_ARTIST_ROOT = "artist";
    private final static String PATH_ASSET_ARTIST_BIN_PREFIX = PATH_ASSET_ARTIST_ROOT + File.separator + "android-";



    public static ArtistRunConfig buildArtistRunConfig(final Context context, final String appPackageName) {
        ArtistRunConfig artistConfig = new ArtistRunConfig();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        artistConfig.api_level = String.valueOf(Build.VERSION.SDK_INT);

        // Asset Paths don't like a trailing '/'
        artistConfig.asset_path_artist_root = PATH_ASSET_ARTIST_BIN_PREFIX + artistConfig.api_level;
        artistConfig.asset_path_dex2oat_libs = artistConfig.asset_path_artist_root + File.separator + "lib";
        artistConfig.asset_path_dex2oat = artistConfig.asset_path_artist_root + File.separator + "dex2oat";
        // Asset Paths don't like a trailing '/'

        artistConfig.artist_exec_path = "artist" + File.separator;
        artistConfig.artist_exec_path_dex2oat = artistConfig.artist_exec_path + "dex2oat";
        artistConfig.artist_exec_path_libs_dir = artistConfig.artist_exec_path
                + "lib" + File.separator;

        final String compiler_threads = sharedPref.getString(ArtistAppConfig.KEY_PREF_COMPILER_THREADS, "-1");
        artistConfig.COMPILER_THREADS = Integer.parseInt(compiler_threads);

        final PackageInfo packageInfo = getPackageInfo(context, appPackageName);

        artistConfig.app_package_name = appPackageName;
        artistConfig.input_files = new ArtistRunConfig.InputFiles(getFiles(packageInfo == null?appPackageName:packageInfo.applicationInfo.publicSourceDir), context);

        artistConfig.app_folder_path = new File(artistConfig.input_files.getFilePath(0)).getParentFile().getAbsolutePath();
        artistConfig.app_apk_file_path_alternative = artistConfig.app_folder_path + File.separator + ArtistRunConfig.BASE_APK_ALTERNATIVE;

        if (packageInfo != null) {
            // For normal applications
            artistConfig.app_oat_folder_path = artistConfig.app_folder_path + File.separator + "oat" + File.separator;
            artistConfig.app_oat_architecture = AndroidUtils.probeArchitetureFolderName(artistConfig.app_oat_folder_path);
            artistConfig.app_oat_file_path = artistConfig.app_oat_folder_path
                    + artistConfig.app_oat_architecture
                    + File.separator + ArtistRunConfig.OAT_FILE;
        }
        else {
            // For framework jar files
            artistConfig.app_oat_folder_path = "/data/dalvik-cache/";
            artistConfig.app_oat_architecture = AndroidUtils.probeArchitetureFolderName(artistConfig.app_oat_folder_path);
            artistConfig.app_oat_file_path = FrameworkPackages.getOatFile(appPackageName, artistConfig);
        }

        artistConfig.keystore = new File(AndroidUtils.getFilesDirLocation(context, ArtistRunConfig.KEYSTORE_NAME));

        try {
            Field fieldNativeLibraryRootDir = ApplicationInfo.class.getDeclaredField("nativeLibraryRootDir");
            artistConfig.nativeLibraryRootDir = (String) fieldNativeLibraryRootDir.get(context.getApplicationInfo());
            artistConfig.nativeLibraryDir = artistConfig.nativeLibraryRootDir
                    + File.separator + "arm" + File.separator;
            Field fieldSecondaryNativeLibraryDir = ApplicationInfo.class.getDeclaredField("secondaryNativeLibraryDir");
            artistConfig.secondaryNativeLibraryDir = (String) fieldSecondaryNativeLibraryDir.get(context.getApplicationInfo());

            Field fieldNativeLibraryRootRequiresIsa = ApplicationInfo.class.getDeclaredField("nativeLibraryRootRequiresIsa");
            artistConfig.nativeLibraryRootRequiresIsa = (Boolean) fieldNativeLibraryRootRequiresIsa.get(context.getApplicationInfo());

            Field fieldPrimaryCpuAbi = ApplicationInfo.class.getDeclaredField("primaryCpuAbi");
            artistConfig.primaryCpuAbi = (String) fieldPrimaryCpuAbi.get(context.getApplicationInfo());

            Field fieldSecondaryCpuAbi = ApplicationInfo.class.getDeclaredField("secondaryCpuAbi");
            artistConfig.secondaryCpuAbi = (String) fieldSecondaryCpuAbi.get(context.getApplicationInfo());
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Getting ApplicationInfo FAILED", e);
        }
        Log.i(TAG, artistConfig.toString());
        return artistConfig;
    }

    @Nullable
    private static PackageInfo getPackageInfo(final Context context, final String app_name) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(app_name, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not find packge: " + app_name);
        }
        return packageInfo;
    }
}
