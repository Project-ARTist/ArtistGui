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

package saarland.cispa.artist.artistgui.packagelist.view;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import saarland.cispa.artist.artistgui.Package;

class ReadInstalledPackagesAsyncTask extends
        AsyncTask<PackageManager, Void, List<Package>> {

    interface OnReadInstalledPackages {
        void onReadInstalledPackages(List<Package> packages);
    }

    private OnReadInstalledPackages mResultCallback;

    ReadInstalledPackagesAsyncTask(OnReadInstalledPackages callback) {
        super();
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
            if (!isCancelled()) {
                applicationInfo = packageInfo.applicationInfo;
                appName = packageManager.getApplicationLabel(applicationInfo).toString();
                appIconId = applicationInfo.icon == 0 ?
                        android.R.mipmap.sym_def_app_icon : applicationInfo.icon;

                packageList.add(new Package(appName, packageInfo.packageName, appIconId));
            }
        }

        packageList.sort(Package.sComparator);
        return packageList;
    }

    @Override
    protected void onPostExecute(List<Package> packages) {
        super.onPostExecute(packages);
        if (mResultCallback != null) {
            mResultCallback.onReadInstalledPackages(packages);
        }
    }
}
