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
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;

import saarland.cispa.artist.artistgui.compilation.CompilationContract;
import saarland.cispa.artist.artistgui.compilation.CompilationPresenter;
import saarland.cispa.artist.artistgui.compilation.CompileFragment;
import saarland.cispa.artist.log.Logg;

class MainActivityPresenter implements MainActivityContract.Presenter {

    private static final int[] supportedSdks = {Build.VERSION_CODES.M, Build.VERSION_CODES.N,
            Build.VERSION_CODES.N_MR1};

    private Context mAppContext;
    private Activity mActivity;
    private MainActivityContract.View mView;

    private InfoFragment mInfoFragment;

    private CompileFragment mCompileFragment;
    private CompilationContract.Presenter mCompilationPresenter;

    MainActivityPresenter(Context context, MainActivityContract.View view) {
        mAppContext = context;
        mActivity = (Activity) view;
        mView = view;
    }

    @Override
    public void start() {
        Logg.setUserLogLevel(mAppContext);
    }

    @Override
    public void checkCompatibility() {
        if (!supportedByArtist()) {
            mView.onIncompatibleAndroidVersion();
        }
    }

    @Override
    public void processIntent(Intent intent) {
        selectFragment(R.id.nav_compiler);
        mCompilationPresenter.executeIntentTasks(intent);
    }

    @Override
    public void processCompilationResult(int resultCode, Intent data) {
        mCompilationPresenter.onCompilationFinished(resultCode, data);
    }

    @Override
    public void selectFragment(@IdRes int id) {
        Fragment selectedFragment = null;
        switch (id) {
            case R.id.nav_home:
                if (mInfoFragment == null) {
                    mInfoFragment = new InfoFragment();
                }
                selectedFragment = mInfoFragment;
                break;
            case R.id.nav_compiler:
                if (mCompileFragment == null) {
                    mCompileFragment = new CompileFragment();
                    mCompilationPresenter = new CompilationPresenter(mActivity, mCompileFragment);
                }
                selectedFragment = mCompileFragment;
                break;
        }
        mView.onFragmentSelected(selectedFragment);
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
