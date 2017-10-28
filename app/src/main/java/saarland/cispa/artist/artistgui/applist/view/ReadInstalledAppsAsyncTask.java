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

package saarland.cispa.artist.artistgui.applist.view;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.settings.db.DatabaseManager;

class ReadInstalledAppsAsyncTask extends
        AsyncTask<PackageManager, Void, List<Package>> {

    interface OnReadInstalledPackages {
        void onReadInstalledPackages(List<Package> packages);
    }

    private Context mContext;
    private OnReadInstalledPackages mResultCallback;

    private List<Package> mInstrumentedApps;

    ReadInstalledAppsAsyncTask(Context context, OnReadInstalledPackages callback) {
        super();
        mContext = context;
        mResultCallback = callback;
    }

    @Override
    protected List<Package> doInBackground(PackageManager... params) {
        PackageManager packageManager = params[0];
        List<PackageInfo> packageInfoList = packageManager
                .getInstalledPackages(PackageManager.GET_ACTIVITIES);

        List<Package> packageList = new ArrayList<>();
        ApplicationInfo applicationInfo;
        String appName;
        int appIconId;
        for (PackageInfo packageInfo : packageInfoList) {
//            if (this.filterGapps) {
//                if (packageInfo.packageName.startsWith("com.android.")) {
//                    continue;
//                }
//                if (packageInfo.packageName.startsWith("com.google.")) {
//                    continue;
//                }
//            }
            if (!isCancelled()) {
                applicationInfo = packageInfo.applicationInfo;
                appName = packageManager.getApplicationLabel(applicationInfo).toString();

                Package mPackage = getInstrumentedApp(packageInfo.packageName);
                // Not instrumented?
                if (mPackage == null) {
                    mPackage = new Package(packageInfo.packageName);
                }

                mPackage.setAppName(appName);

                appIconId = applicationInfo.icon == 0 ?
                        android.R.mipmap.sym_def_app_icon : applicationInfo.icon;
                mPackage.setAppIconId(appIconId);

                packageList.add(mPackage);
            }
        }
        Collections.sort(packageList, Package.sComparator);
        return packageList;
    }

    /**
     * Fetches app entry from db if given package has been instrumented.
     *
     * @return instrumented package or null
     */
    private Package getInstrumentedApp(String packageName) {
        if (mInstrumentedApps == null) {
            DatabaseManager databaseManager = new DatabaseManager(mContext);
            mInstrumentedApps = databaseManager.getAllInstrumentedApps();
        }

        for (Package p : mInstrumentedApps) {
            if (p.getPackageName().equals(packageName)) {
                return p;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Package> packages) {
        super.onPostExecute(packages);
        if (!isCancelled() && mResultCallback != null) {
            mResultCallback.onReadInstalledPackages(packages);
        }
    }
}
