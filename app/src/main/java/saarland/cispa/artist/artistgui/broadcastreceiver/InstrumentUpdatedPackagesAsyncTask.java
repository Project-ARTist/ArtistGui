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

package saarland.cispa.artist.artistgui.broadcastreceiver;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import saarland.cispa.artist.artistgui.Application;
import saarland.cispa.artist.artistgui.database.Package;
import saarland.cispa.artist.artistgui.database.PackageDao;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;

class InstrumentUpdatedPackagesAsyncTask extends AsyncTask<String, Void, Void> {

    private Application appContext;

    InstrumentUpdatedPackagesAsyncTask(Application appContext) {
        this.appContext = appContext;
    }

    @Override
    protected Void doInBackground(String... packages) {
        String packageName = packages[0];
        Package app = getPackage(packageName);
        if (app != null && app.keepInstrumented) {
            startService(packageName);
        }
        return null;
    }

    private Package getPackage(@NonNull String packageName) {
        PackageDao dao = appContext.getDatabase().packageDao();
        return dao.get(packageName);
    }

    private void startService(@NonNull String packageName) {
        Intent serviceIntent = new Intent(appContext, InstrumentationService.class);
        serviceIntent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, packageName);
        appContext.startService(serviceIntent);
    }
}
