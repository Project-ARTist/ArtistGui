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
import android.content.Intent;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import saarland.cispa.artist.android.LogA;
import saarland.cispa.artist.artistgui.compilation.CompilationContract;
import saarland.cispa.artist.artistgui.compilation.CompilationPresenter;
import saarland.cispa.artist.artistgui.compilation.CompileFragment;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;

class MainActivityPresenter implements MainActivityContract.Presenter {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INFO_FRAGMENT, COMPILATION_FRAGMENT})
    @interface selectableFragment {
    }

    static final int INFO_FRAGMENT = 0;
    static final int COMPILATION_FRAGMENT = 1;

    private static final int[] supportedSdks = {Build.VERSION_CODES.M, Build.VERSION_CODES.N,
            Build.VERSION_CODES.N_MR1};

    private Context mAppContext;
    private Activity mActivity;
    private MainActivityContract.View mView;
    private SettingsManager mSettingsManager;

    private int mSelectedFragmentId;
    private InfoFragment mInfoFragment;

    private CompileFragment mCompileFragment;
    private CompilationContract.Presenter mCompilationPresenter;

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
    public void processIntent(Intent intent) {
        if (intent.hasExtra(MainActivity.EXTRA_PACKAGE)) {
            selectFragment(COMPILATION_FRAGMENT);
            mCompilationPresenter.executeIntentTasks(intent);
        } else {
            selectFragment(INFO_FRAGMENT);
        }
    }

    @Override
    public void processCompilationResult(int resultCode, Intent data) {
        mCompilationPresenter.onCompilationFinished(resultCode, data);
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
                if (mCompileFragment == null) {
                    mCompileFragment = new CompileFragment();
                    mCompilationPresenter = new CompilationPresenter(mActivity,
                            mCompileFragment, mSettingsManager);
                }
                selectedFragment = mCompileFragment;
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
                mCompileFragment = (CompileFragment) selectedFragment;
                mCompilationPresenter = new CompilationPresenter(mActivity,
                        mCompileFragment, mSettingsManager);
                mCompileFragment.setPresenter(mCompilationPresenter);
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
