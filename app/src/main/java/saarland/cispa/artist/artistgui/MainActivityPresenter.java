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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import saarland.cispa.artist.artistgui.applist.AppListContract;
import saarland.cispa.artist.artistgui.applist.AppListFragment;
import saarland.cispa.artist.artistgui.applist.AppListPresenter;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;
import saarland.cispa.artist.artistgui.utils.LogA;

class MainActivityPresenter implements MainActivityContract.Presenter {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INFO_FRAGMENT, COMPILATION_FRAGMENT})
    @interface selectableFragment {
    }

    static final int INFO_FRAGMENT = 0;
    static final int COMPILATION_FRAGMENT = 1;

    private static final int[] supportedSdks = {
            Build.VERSION_CODES.M,
            Build.VERSION_CODES.N,
            Build.VERSION_CODES.N_MR1,
            Build.VERSION_CODES.O
    };

    private Context mAppContext;
    private Activity mActivity;
    private MainActivityContract.View mView;
    private SettingsManager mSettingsManager;

    private int mSelectedFragmentId;
    private InfoFragment mInfoFragment;

    private AppListFragment mAppListFragment;
    private AppListContract.Presenter mCompilationPresenter;

    MainActivityPresenter(Context context, Activity activity, MainActivityContract.View view,
                          SettingsManager settingsManager) {
        mAppContext = context;
        mActivity = activity;
        mView = view;
        mSettingsManager = settingsManager;
    }

    @Override
    public void start() {
        LogA.setUserLogLevel(mAppContext);
    }

    @Override
    public void checkCompatibility() {
        if (!supportedByArtist()) {
            mView.onIncompatibleAndroidVersion();
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
            case COMPILATION_FRAGMENT:
                if (mAppListFragment == null) {
                    mAppListFragment = new AppListFragment();
                    mCompilationPresenter = new AppListPresenter(mActivity,
                            mAppListFragment, mSettingsManager);
                }
                selectedFragment = mAppListFragment;
                break;
        }
        mSelectedFragmentId = id;
        mView.onFragmentSelected(selectedFragment);
    }

    @Override
    public void onRestoreSavedInstance(int selectedFragmentId, Fragment selectedFragment) {
        switch (selectedFragmentId) {
            case INFO_FRAGMENT:
                mInfoFragment = (InfoFragment) selectedFragment;
                break;
            case COMPILATION_FRAGMENT:
                mAppListFragment = (AppListFragment) selectedFragment;
                mCompilationPresenter = new AppListPresenter(mActivity,
                        mAppListFragment, mSettingsManager);
                mAppListFragment.setPresenter(mCompilationPresenter);
                break;
        }
        mSelectedFragmentId = selectedFragmentId;
    }

    @Override
    public int getSelectedFragmentId() {
        return mSelectedFragmentId;
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
}
