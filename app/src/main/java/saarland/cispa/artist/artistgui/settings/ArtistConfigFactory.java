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
package saarland.cispa.artist.artistgui.settings;

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

import saarland.cispa.artist.Artist;
import saarland.cispa.artist.log.Logg;
import saarland.cispa.artist.settings.ArtistRunConfig;
import saarland.cispa.artist.utils.AndroidUtils;
import saarland.cispa.artist.utils.ArtistUtils;
import trikita.log.Log;

/**
 * @author Sebastian Weisgerber (weisgerber@cispa.saarland)
 * @author Oliver Schranz (oliver.schranz@cispa.saarland)
 */

public class ArtistConfigFactory {

    private static final String TAG = Logg.TAG;

    public final static String PATH_ASSET_ARTIST_ROOT = "artist";
    public final static String PATH_ASSET_ARTIST_BIN_PREFIX = PATH_ASSET_ARTIST_ROOT + File.separator + "android-";

    public static ArtistRunConfig buildArtistRunConfig(final Context context, final String appPackageName) {
        ArtistRunConfig artistConfig = new ArtistRunConfig();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        artistConfig.app_name = appPackageName;
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
        artistConfig.MULTIDEX_ABORT = sharedPref.getBoolean(ArtistAppConfig.KEY_PREF_COMPILER_ABORT_MULTIDEX, false);
        artistConfig.INJECT_CODELIB = sharedPref.getBoolean(ArtistAppConfig.KEY_PREF_COMPILER_INJECT_CODELIB, false);
        artistConfig.BACKUP_APK_MERGED = sharedPref.getBoolean(ArtistAppConfig.KEY_PREF_BACKUP_APK_MERGED, false);
        artistConfig.BACKUP_APK_ORIGINAL = sharedPref.getBoolean(ArtistAppConfig.KEY_PREF_BACKUP_APK_ORIGINAL, false);
        artistConfig.CODELIB_SETTINGS_APPCOMPART = sharedPref.getBoolean(ArtistAppConfig.KEY_PREF_COMPILER_CODELIB_APPCOMPART, false);

        final PackageInfo packageInfo = getPackageInfo(context, artistConfig.app_name);

        artistConfig.app_package_name = packageInfo.packageName;
        artistConfig.app_apk_file_path = packageInfo.applicationInfo.publicSourceDir;
        artistConfig.app_apk_merged_file_path = AndroidUtils.getFilesDirLocation(context, ArtistRunConfig.BASE_APK_MERGED);
        artistConfig.app_apk_merged_signed_file_path = AndroidUtils.getFilesDirLocation(context, ArtistRunConfig.BASE_APK_SIGNED);

        artistConfig.app_folder_path = new File(artistConfig.app_apk_file_path).getParentFile().getAbsolutePath();
        artistConfig.app_apk_file_path_alternative = artistConfig.app_folder_path + File.separator + ArtistRunConfig.BASE_APK_ALTERNATIVE;

        artistConfig.app_oat_folder_path = artistConfig.app_folder_path + File.separator + "oat" + File.separator;
        artistConfig.app_oat_architecture = AndroidUtils.probeArchitetureFolderName(artistConfig.app_oat_folder_path );
        artistConfig.app_oat_file_path = artistConfig.app_oat_folder_path
                + artistConfig.app_oat_architecture
                + File.separator + ArtistRunConfig.OAT_FILE;

        final String userCodeLib = sharedPref.getString(ArtistAppConfig.PREF_KEY_CODELIB_SELECTION, "");
        artistConfig.codeLibName = userCodeLib;
        artistConfig.codeLib = getPathToCodeLib(context, userCodeLib);

        artistConfig.keystore = new File(AndroidUtils.getFilesDirLocation(context, ArtistRunConfig.KEYSTORE_NAME));

        artistConfig.REPLACE_BASE_APK = sharedPref.getBoolean("pref_key_compiler_replace_base_apk", false);

        try {
            Field fieldNativeLibraryRootDir = ApplicationInfo.class.getDeclaredField("nativeLibraryRootDir");
            artistConfig.nativeLibraryRootDir = (String)fieldNativeLibraryRootDir.get(context.getApplicationInfo());
            artistConfig.nativeLibraryDir = artistConfig.nativeLibraryRootDir
                                            + File.separator + "arm" + File.separator;
            Field fieldSecondaryNativeLibraryDir = ApplicationInfo.class.getDeclaredField("secondaryNativeLibraryDir");
            artistConfig.secondaryNativeLibraryDir = (String)fieldSecondaryNativeLibraryDir.get(context.getApplicationInfo());

            Field fieldNativeLibraryRootRequiresIsa = ApplicationInfo.class.getDeclaredField("nativeLibraryRootRequiresIsa");
            artistConfig.nativeLibraryRootRequiresIsa = (Boolean)fieldNativeLibraryRootRequiresIsa.get(context.getApplicationInfo());

            Field fieldPrimaryCpuAbi = ApplicationInfo.class.getDeclaredField("primaryCpuAbi");
            artistConfig.primaryCpuAbi = (String)fieldPrimaryCpuAbi.get(context.getApplicationInfo());

            Field fieldSecondaryCpuAbi = ApplicationInfo.class.getDeclaredField("secondaryCpuAbi");
            artistConfig.secondaryCpuAbi = (String)fieldSecondaryCpuAbi.get(context.getApplicationInfo());
        } catch (final NoSuchFieldException|IllegalAccessException e) {
            Log.e(TAG, "Getting ApplicationInfo FAILED", e);
        }
        Log.i(TAG, artistConfig.toString());
        return artistConfig;
    }

    @Nullable
    private static File getPathToCodeLib(final Context context, final String userCodeLib) {
        File codeLib = null;
        if (userCodeLib != null) {
            String codeLibPath = null;
            if (userCodeLib.startsWith(ArtistUtils.CODELIB_IMPORTED)) {
                String codeLibName = userCodeLib.replaceFirst(ArtistUtils.CODELIB_IMPORTED, "");
                codeLibPath = AndroidUtils.getFilesDirLocation(context, ArtistAppConfig.APP_FOLDER_CODELIBS + File.separator + codeLibName);
            } else if (userCodeLib.startsWith(ArtistUtils.CODELIB_ASSET)) {
                String codeLibName = userCodeLib.replaceFirst(ArtistUtils.CODELIB_ASSET, "");
                codeLibPath = AndroidUtils.getFilesDirLocation(context, Artist.CODE_LIB_ASSET);
            }

            if (codeLibPath != null) {
                codeLib = new File(codeLibPath);
            }
        }
        return codeLib;
    }

    @Nullable
    public static PackageInfo getPackageInfo(final Context context, final String app_name) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(app_name, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not find packge: " + app_name);
        }
        return packageInfo;
    }
}
