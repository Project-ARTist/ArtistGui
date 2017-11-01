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

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import saarland.cispa.artist.artistgui.applist.AppListContract;
import saarland.cispa.artist.artistgui.applist.AppListFragment;
import saarland.cispa.artist.artistgui.applist.AppListPresenter;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;

class MainActivityPresenter implements MainActivityContract.Presenter {

    static final int INFO_FRAGMENT = 0;
    static final int INSTRUMENTATION_FRAGMENT = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INFO_FRAGMENT, INSTRUMENTATION_FRAGMENT})
    @interface selectableFragment {
    }

    private static final String SELECTED_FRAGMENT_STATE_KEY = "selected_fragment";
    private static final int[] supportedSdks = {
            Build.VERSION_CODES.M,
            Build.VERSION_CODES.N,
            Build.VERSION_CODES.N_MR1,
            Build.VERSION_CODES.O
    };

    private MainActivityContract.View mView;
    private SettingsManager mSettingsManager;

    private int mSelectedFragmentId;
    private InfoFragment mInfoFragment;
    private AppListFragment mAppListFragment;
    private AppListContract.Presenter mAppListPresenter;

    MainActivityPresenter(MainActivityContract.View view,
                          SettingsManager settingsManager) {
        mView = view;
        mSettingsManager = settingsManager;
    }

    @Override
    public void start() {
    }

    @Override
    public void checkCompatibility() {
        if (!supportedByArtist()) {
            mView.showIncompatibleVersionDialog();
        }
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
                    mAppListPresenter = new AppListPresenter(mAppListFragment, mSettingsManager);
                }
                selectedFragment = mAppListFragment;
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
                mAppListPresenter = new AppListPresenter(mAppListFragment, mSettingsManager);
                mAppListFragment.setPresenter(mAppListPresenter);
                break;
        }
        mSelectedFragmentId = selectedFragmentId;
    }
}
