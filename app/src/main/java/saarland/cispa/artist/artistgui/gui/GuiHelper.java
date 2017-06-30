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
package saarland.cispa.artist.artistgui.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.settings.ArtistAppConfig;
import saarland.cispa.artist.log.Logg;
import saarland.cispa.artist.utils.AndroidUtils;
import trikita.log.Log;

public class GuiHelper {

    private static final String TAG = Logg.TAG;

    public static final int READ_REQUEST_CODE = 42;

    /**
     * Fires an intent to spin up the "file chooser" UI and select a file.
     * @param activity
     */
    public static void showFileChooser(final Activity activity, final String typeFilter) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only showNotification results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to showNotification only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType(typeFilter);
        // intent.setType("application/vnd.android.package-archive");
        // intent.setType("*/*");
        activity.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public static void showFileChooserApks(final Activity activity) {
        showFileChooser(activity, "application/vnd.android.package-archive");
    }

    public static String[] listImportedCodeLibs(final Context context) {
        final String codeLibFolder = AndroidUtils.getFilesDirLocation(context,
                ArtistAppConfig.APP_FOLDER_CODELIBS);
        String[] importedCodeLibs = new File(codeLibFolder).list();
        if (importedCodeLibs == null) {
            importedCodeLibs = new String[0];
        }
        return importedCodeLibs;
    }

    public static String[] listAssetCodeLibs(final Context context) {
        final AssetManager assets = context.getAssets();
        final List<String> cleanedAssetCodeLibs = new ArrayList<>();

        String[] assetCodeLibs;
        try {
            assetCodeLibs = assets.list(ArtistAppConfig.ASSET_FOLDER_CODELIBS);
            for (final String assetLib : assetCodeLibs) {
                if (assetLib.endsWith(".apk")
                        || assetLib.endsWith(".jar")
                        || assetLib.endsWith(".zip")
                        || assetLib.endsWith(".dex")) {
                    cleanedAssetCodeLibs.add(assetLib);
                }
            }

        } catch (final IOException e) {
            Log.e(TAG, "Could not open assetfolder: ", e);

        }
        return cleanedAssetCodeLibs.toArray(new String[0]);
    }
}
