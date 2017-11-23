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

package saarland.cispa.artist.artistgui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;

public class PackageUpdatedReceiver extends BroadcastReceiver {

    private static final String PACKAGE_NAME_PREFIX = "package:";

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getDataString();
        if (packageName != null) {
            packageName = packageName.replace(PACKAGE_NAME_PREFIX, "");

            Intent serviceIntent = new Intent(context, InstrumentationService.class);
            serviceIntent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, packageName);
            context.startService(serviceIntent);
        }
    }
}
