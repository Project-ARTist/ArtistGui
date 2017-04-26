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

public class ArtistAppConfig {

    public static final String ASSET_FOLDER_CODELIBS = "codelib";
    public static final String ASSET_FOLDER_ARTIST = "artist";
    public static final String ASSET_FOLDER_CONFIG = "config";

    public static final String APP_FOLDER_APK_BACKUP = "backup";
    public static final String APP_FOLDER_CODELIBS = "codelibs";

    public static final String KEY_PREF_COMPILER_THREADS = "pref_key_compiler_threads";
    public static final String KEY_PREF_COMPILER_ABORT_MULTIDEX = "pref_key_compiler_abort_multidex";
    public static final String KEY_PREF_COMPILER_INJECT_CODELIB = "pref_key_compiler_inject_codelib";
    public static final String KEY_PREF_COMPILER_CODELIB_APPCOMPART = "pref_key_compiler_special_app_compart_settings";

    public static final String KEY_PREF_COMPILER_LAUNCH_ACTIVITY = "pref_key_compiler_launch_activity";

    public static final String KEY_PREF_BACKUP_APK_MERGED = "pref_key_compiler_backup_merged_apk";
    public static final String KEY_PREF_BACKUP_APK_ORIGINAL = "pref_key_compiler_backup_original_apk";

    public static final String PREF_KEY_CODELIB_SELECTION = "pref_key_codelib_selection";
    public static final String PREF_KEY_CODELIB_IMPORT = "pref_key_codelib_import";

    public static final String KEY_PREF_GENERAL_LOGLEVEL = "pref_general_loglevel";


    public String apkBackupFolderLocation = "";
    public String codeLibFolder = "";
}
