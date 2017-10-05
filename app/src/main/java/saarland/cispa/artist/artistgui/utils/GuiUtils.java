/**
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
package saarland.cispa.artist.artistgui.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

public class GuiUtils {

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
