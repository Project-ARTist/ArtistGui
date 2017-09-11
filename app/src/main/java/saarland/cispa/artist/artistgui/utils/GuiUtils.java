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

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import trikita.log.Log;

public class GuiUtils {

    private static final String TAG = "GuiUtils";

    public static void displayToast(final Activity activity, final String toastMessage) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                Toast toast = Toast.makeText(activity.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
                toast.show();
            });
        }
    }

    public static void displaySnackForever(final View view, final String snackMessage) {
        if (view != null) {
            Snackbar.make(view, snackMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show();
        }
    }

    public static void displaySnackLong(final View view, final String snackMessage) {
        if (view != null) {
            Snackbar.make(view, snackMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
