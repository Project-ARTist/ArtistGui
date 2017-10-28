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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.Package;

public class AppListView extends RecyclerView
        implements ReadInstalledAppsAsyncTask.OnReadInstalledPackages {

    public interface OnPackageSelectedListener {
        void onPackageSelected(Package selectedPackage);
    }

    private Context mContext;
    private IntentFilter mIntentFilter;
    private List<OnPackageSelectedListener> mListeners;

    private BroadcastReceiver mPackageInstalledOrRemoved;

    public AppListView(Context context) {
        this(context, null);
    }

    public AppListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mListeners = new ArrayList<>();
        mContext = context;

        setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        setLayoutManager(mLayoutManager);

        PackageManager packageManager = context.getPackageManager();
        new ReadInstalledAppsAsyncTask(context, this).execute(packageManager);
    }

    private boolean shouldFilterApps(final Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean("pref_key_apps_filter_google", false);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (mPackageInstalledOrRemoved == null) {
            mPackageInstalledOrRemoved = new PackageModifiedReceiver((AppListAdapter) adapter);
        }
        registerPackageModifiedBroadcastReceiver();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPackageInstalledOrRemoved != null) {
            registerPackageModifiedBroadcastReceiver();
        }
    }

    private void registerPackageModifiedBroadcastReceiver() {
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            mIntentFilter.addDataScheme("package");
        }
        mContext.registerReceiver(mPackageInstalledOrRemoved, mIntentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPackageInstalledOrRemoved != null) {
            mContext.unregisterReceiver(mPackageInstalledOrRemoved);
        }
    }

    @Override
    public void onReadInstalledPackages(List<Package> packages) {
        AppListAdapter adapter = new AppListAdapter(mContext, packages, mListeners);
        setAdapter(adapter);
    }

    public void addOnPackageSelectedListener(OnPackageSelectedListener listener) {
        mListeners.add(listener);
    }

    public void removeOnPackageSelectedListener(OnPackageSelectedListener listener) {
        mListeners.remove(listener);
    }
}
