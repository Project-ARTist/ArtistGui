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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import saarland.cispa.artist.artistgui.applist.AppListFragment;
import saarland.cispa.artist.artistgui.modules.ModuleFragment;
import saarland.cispa.artist.artistgui.modules.ModulePresenter;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;

public class MainActivityPresenter implements MainActivityContract.Presenter {

    static final int INFO_FRAGMENT = 0;
    static final int INSTRUMENTATION_FRAGMENT = 1;
    static final int MODULES_FRAGMENT = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INFO_FRAGMENT, INSTRUMENTATION_FRAGMENT, MODULES_FRAGMENT})
    @interface selectableFragment {
    }

    private static final String SELECTED_FRAGMENT_STATE_KEY = "selected_fragment";
    private static final int[] supportedSdks = {
            Build.VERSION_CODES.M,
            Build.VERSION_CODES.N,
            Build.VERSION_CODES.N_MR1,
            Build.VERSION_CODES.O
    };

    private static final String ASSETS_ARTIST_PATH = "artist" + File.separator +
            "android-" + Build.VERSION.SDK_INT;
    private static final String COMPILER_BINARY = "dex2oat";
    private static final String LIBS_DIR = "lib";

    private final MainActivityContract.View mView;
    private final SettingsManager mSettingsManager;
    private final Context mContext;

    private int mSelectedFragmentId;
    private InfoFragment mInfoFragment;
    private AppListFragment mAppListFragment;
    private ModuleFragment mModuleFragment;

    MainActivityPresenter(MainActivityContract.View view,
                          SettingsManager settingsManager, Context context) {
        mView = view;
        mSettingsManager = settingsManager;
        mContext = context;
    }

    @Override
    public void start() {
    }

    @Override
    public void checkCompatibility() {
        checkAndroidVersionCompatibility();
        if (!dex2oatBundledWithApp(mContext)) {
            mView.showMissingDex2OatFilesDialog();
        }
    }

    @VisibleForTesting
    void checkAndroidVersionCompatibility() {
        if (!supportedByArtist()) {
            mView.showIncompatibleAndroidVersionDialog();
        }
    }

    public static boolean isDeviceCompatible(Context context) {
        return supportedByArtist() && dex2oatBundledWithApp(context);
    }

    private static boolean supportedByArtist() {
        final int currentSdk = Build.VERSION.SDK_INT;
        for (int sdk : supportedSdks) {
            if (sdk == currentSdk) {
                return true;
            }
        }
        return false;
    }

    private static boolean dex2oatBundledWithApp(Context context) {
        boolean result = false;
        try {
            final AssetManager assetManager = context.getAssets();
            String[] files = assetManager.list(ASSETS_ARTIST_PATH);
            int foundFiles = 0;
            for (String file : files) {
                switch (file) {
                    case COMPILER_BINARY:
                    case LIBS_DIR:
                        foundFiles++;
                }
            }

            if (foundFiles == 2) {
                // Do actual files exist in lib/
                files = assetManager.list(ASSETS_ARTIST_PATH + File.separator + LIBS_DIR);
                if (files.length > 0) {
                    result = true;
                }
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    @Override
    public void openDex2OatHelpPage() {
        Uri url = Uri.parse("https://artist.cispa.saarland/build-setup/");
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        final PackageManager packageManager = mContext.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            mContext.startActivity(intent);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.no_browser_installed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void selectFragment(@selectableFragment int id) {
        Fragment selectedFragment = null;
        switch (id) {
            case INFO_FRAGMENT:
                if (mInfoFragment == null) {
                    mInfoFragment = new InfoFragment();
                }
                selectedFragment = mInfoFragment;
                break;
            case INSTRUMENTATION_FRAGMENT:
                if (mAppListFragment == null) {
                    mAppListFragment = new AppListFragment();
                }
                selectedFragment = mAppListFragment;
                break;
            case MODULES_FRAGMENT:
                if (mModuleFragment == null) {
                    mModuleFragment = new ModuleFragment();
                    new ModulePresenter(mContext, mModuleFragment);
                }
                selectedFragment = mModuleFragment;
                break;
        }
        mSelectedFragmentId = id;
        mView.showSelectedFragment(selectedFragment);
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_FRAGMENT_STATE_KEY, mSelectedFragmentId);
    }

    @Override
    public void restoreSavedInstanceState(Bundle savedInstanceState,
                                          FragmentManager fragmentManager) {
        int selectedFragmentId = savedInstanceState.getInt(SELECTED_FRAGMENT_STATE_KEY);
        Fragment selectedFragment = fragmentManager.findFragmentById(R.id.content_frame);

        switch (selectedFragmentId) {
            case INFO_FRAGMENT:
                mInfoFragment = (InfoFragment) selectedFragment;
                break;
            case INSTRUMENTATION_FRAGMENT:
                mAppListFragment = (AppListFragment) selectedFragment;
                break;
        }
        mSelectedFragmentId = selectedFragmentId;
    }
}
