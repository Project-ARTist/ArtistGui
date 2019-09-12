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

package saarland.cispa.artist.artistgui.instrumentation.config;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

import saarland.cispa.artist.artistgui.utils.AndroidUtils;

public class ArtistRunConfig {

    public static class InputFiles {
        private ArrayList<String> paths = new ArrayList<>();
        private ArrayList<String> mergedpaths = new ArrayList<>();
        private ArrayList<String> signedpaths = new ArrayList<>();


        public InputFiles(String[] paths, final Context context) {
            addFiles(paths, context);
        }


        public InputFiles() {
        }


        public void addFile(String filePath, final Context context) {
            int i = paths.size();
            paths.add(filePath);
            mergedpaths.add(AndroidUtils.getFilesDirLocation(context, ArtistRunConfig.BASE_APK_MERGED+"_"+i+".apk"));
            signedpaths.add(AndroidUtils.getFilesDirLocation(context, ArtistRunConfig.BASE_APK_SIGNED+"_"+i+".apk"));
        }


        public void addFiles(String[] paths, final Context context) {
            for (String s :paths){
                addFile(s, context);
            }
        }


        public String getFilePath(int i) {
            return paths.get(i);
        }


        public String getMergedPath(int i) {
            return mergedpaths.get(i);
        }


        public String getSignedPath(int i) {
            return signedpaths.get(i);
        }


        public String[] getPaths() {
            return (String[]) paths.toArray();
        }


        public String[] getMergedPaths() {
            return (String[]) mergedpaths.toArray();
        }


        public String[] getSignedPaths() {
            return (String[]) signedpaths.toArray();
        }


        public int size(){
            return paths.size();
        }


        @Override
        public String toString() {
            return "InputFiles{" +
                    "paths=" + paths +
                    ", mergedpaths=" + mergedpaths +
                    ", signedpaths=" + signedpaths +
                    '}';
        }


    }

    public final static String KEYSTORE_NAME = "artist.bks";

    public static final String BASE_APK_ALTERNATIVE = "base2.apk";
    public static final String BASE_APK_MERGED = "base_merged";
    public static final String BASE_APK_SIGNED = "base_merged-signed";

    public final static String OAT_FILE = "base.odex";

    /**
     * Package Name of the application
     */

    public String app_folder_path = "";

    public String app_apk_file_path_alternative = "";

    public String app_oat_folder_path = "";
    public String app_oat_file_path = "";
    public String app_package_name = "";

    public String app_oat_architecture = "";

    /**
     * Android Version / API Level
     * => Used the select the appropriate dex2oat compiler version
     */
    public String api_level = "";

    public int COMPILER_THREADS = -1;

    public String asset_path_artist_root = "";
    public String asset_path_dex2oat = "";
    public String asset_path_dex2oat_libs = "";
    public String asset_path_keystore = "artist" + File.separator + KEYSTORE_NAME;

    public String artist_exec_path = "";
    public String artist_exec_path_libs_dir = "";
    public String artist_exec_path_dex2oat = "";

    public File keystore = null;

    public String nativeLibraryDir = "";

    public String nativeLibraryRootDir = "";
    public String secondaryNativeLibraryDir = "";
    public boolean nativeLibraryRootRequiresIsa = false;
    public String primaryCpuAbi = "";
    public String secondaryCpuAbi = "";

    public ArtistRunStats stats = new ArtistRunStats();

    public String oatOwner = "";
    public String oatGroup = "";
    public String oatPermissions = "";

    public InputFiles input_files = new InputFiles();

    @Override
    public String toString() {
        return "ArtistRunConfig {" + "\n" +
                "  api_level=                        '" + api_level + '\'' + "\n" +
                ", app_folder_path=                  '" + app_folder_path + '\'' + "\n" +
                ", input_files=                      '" + input_files + '\'' + "\n" +
                ", app_apk_file_path_alternative=    '" + app_apk_file_path_alternative + '\'' + "\n" +
                ", app_oat_folder_path=              '" + app_oat_folder_path + '\'' + "\n" +
                ", app_oat_file_path=                '" + app_oat_file_path + '\'' + "\n" +
                ", app_package_name=                 '" + app_package_name + '\'' + "\n" +
                ", app_oat_architecture=             '" + app_oat_architecture + '\'' + "\n" +
                ", COMPILER_THREADS=                 '" + COMPILER_THREADS + "'\n" +
                ", asset_path_artist_root=           '" + asset_path_artist_root + '\'' + "\n" +
                ", asset_path_dex2oat=               '" + asset_path_dex2oat + '\'' + "\n" +
                ", asset_path_dex2oat_libs=          '" + asset_path_dex2oat_libs + '\'' + "\n" +
                ", asset_path_keystore=              '" + asset_path_keystore + '\'' + "\n" +
                ", artist_exec_path=                 '" + artist_exec_path + '\'' + "\n" +
                ", artist_exec_path_libs_dir=        '" + artist_exec_path_libs_dir + '\'' + "\n" +
                ", artist_exec_path_dex2oat=         '" + artist_exec_path_dex2oat + '\'' + "\n" +
                ", keystore=                         '" + keystore + "'\n" +
                ", nativeLibraryDir=                 '" + nativeLibraryDir + '\'' + "\n" +
                ", nativeLibraryRootDir=             '" + nativeLibraryRootDir + '\'' + "\n" +
                ", secondaryNativeLibraryDir=        '" + secondaryNativeLibraryDir + '\'' + "\n" +
                ", nativeLibraryRootRequiresIsa=     '" + nativeLibraryRootRequiresIsa + "'\n" +
                ", primaryCpuAbi=                    '" + primaryCpuAbi + '\'' + "\n" +
                ", secondaryCpuAbi=                  '" + secondaryCpuAbi + '\'' + "\n" +
                ", stats=                            '" + stats + "\n" +
                ", oatOwner=                         '" + oatOwner + '\'' + "\n" +
                ", oatGroup=                         '" + oatGroup + '\'' + "\n" +
                ", oatPermissions=                   '" + oatPermissions + '\'' + "\n" +
                '}';
    }
}
