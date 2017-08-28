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
package saarland.cispa.artist.artistgui.utils;

import android.net.Uri;

import java.io.File;

import saarland.cispa.artist.log.LogG;
import trikita.log.Log;

public class UriUtils {

    private static final String TAG = LogG.TAG;

    public static String getFilenameFromUri(final Uri uri) {
        Log.v(TAG, "Uri: " + uri.toString());
        Log.v(TAG, "Uri Path: " + uri.getPath());
        Log.v(TAG, "Uri LastPathSegment: " + uri.getLastPathSegment());

        final File fileFromUri = new File(uri.getPath());
        Log.v(TAG, "Uri File Name: " + fileFromUri.getName());
        String filename = fileFromUri.getName();
        filename = cleanUriFilename(filename);
        return filename;
    }

    private static String cleanUriFilename(String filename) {
        if (filename.startsWith("primary:")) {
            filename = filename.replaceFirst("primary:", "");
        }
        return filename;
    }

}
