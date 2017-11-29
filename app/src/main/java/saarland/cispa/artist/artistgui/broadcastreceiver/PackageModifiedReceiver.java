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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import saarland.cispa.artist.artistgui.Application;
import saarland.cispa.artist.artistgui.database.AppDatabase;
import saarland.cispa.artist.artistgui.database.PackageDao;
import saarland.cispa.artist.artistgui.database.operations.RemovePackagesFromDbAsyncTask;

public class PackageModifiedReceiver extends BroadcastReceiver {

    private static final String PACKAGE_NAME_PREFIX = "package:";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final String packageName = extractPackageName(intent);
        if (action != null && packageName != null) {
            final Application appContext = (Application) context.getApplicationContext();
            switch (action) {
                case Intent.ACTION_PACKAGE_REPLACED:
                    new InstrumentUpdatedPackagesAsyncTask(appContext)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, packageName);
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    AppDatabase database = appContext.getDatabase();
                    new RemovePackagesFromDbAsyncTask(database)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, packageName);
                    break;
            }

        }
    }

    private String extractPackageName(Intent intent) {
        final String packageName = intent.getDataString();
        if (packageName != null) {
            return packageName.replace(PACKAGE_NAME_PREFIX, "");
        } else {
            return null;
        }
    }
}
