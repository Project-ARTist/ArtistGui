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

package saarland.cispa.artist.artistgui.applist.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.settings.db.DatabaseManager;

public class AppListLoader extends AsyncTaskLoader<List<Package>> {

    private static final int PACKAGE_MANAGER_NO_FILTER_FLAGS = 0;

    private final PackageManager mPackageManager;
    private AppListChangedReceiver mPackageObserver;

    private List<Package> mDbAppList;
    private List<Package> mCachedResult;

    public AppListLoader(Context context) {
        super(context);
        // getContext() gets the application context
        mPackageManager = getContext().getPackageManager();
    }

    @Override
    protected void onStartLoading() {
        if (mCachedResult != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mCachedResult);
        }

        if (mPackageObserver == null) {
            mPackageObserver = new AppListChangedReceiver(this);
        }

        if (takeContentChanged() || mCachedResult == null) {
            forceLoad();
        }
    }

    @Override
    public List<Package> loadInBackground() {
        List<PackageInfo> packageInfoList = mPackageManager
                .getInstalledPackages(PACKAGE_MANAGER_NO_FILTER_FLAGS);
        return buildSortedPackageList(packageInfoList);
    }

    @Override
    public void deliverResult(List<Package> apps) {
        mCachedResult = apps;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        mCachedResult = null;

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }


    private List<Package> buildSortedPackageList(List<PackageInfo> packageInfos) {
        List<Package> packageList = new ArrayList<>();

        ApplicationInfo applicationInfo;
        Package mPackage;
        String appName;
        int appIconId;

        for (PackageInfo packageInfo : packageInfos) {
            // Collect meta data
            applicationInfo = packageInfo.applicationInfo;
            appName = mPackageManager.getApplicationLabel(applicationInfo).toString();
            appIconId = applicationInfo.icon == 0 ?
                    android.R.mipmap.sym_def_app_icon : applicationInfo.icon;

            // Set collected metadata
            mPackage = getPackage(packageInfo.packageName);
            mPackage.setAppName(appName);
            mPackage.setAppIconId(appIconId);

            packageList.add(mPackage);
        }
        Collections.sort(packageList, Package.sComparator);
        return packageList;
    }

    /**
     * Fetches package details if present in db or return package without additional data.
     *
     * @return package
     */
    private Package getPackage(String packageName) {
        if (mDbAppList == null) {
            DatabaseManager databaseManager = new DatabaseManager(getContext());
            mDbAppList = databaseManager.getAllInstrumentedApps();
        }

        for (Package p : mDbAppList) {
            if (p.getPackageName().equals(packageName)) {
                return p;
            }
        }

        // If not in db return package without additional data
        return new Package(packageName);
    }
}
