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

package saarland.cispa.artist.artistgui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import trikita.log.Log;

public class SettingsActivity extends Activity {

    private static final String TAG = "SettingsActivity";

    private SettingsPresenter mPresenter;
    private SettingsFragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Material_Settings);

        mFragment = new SettingsFragment();
        mPresenter = new SettingsPresenter(this, mFragment);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == SettingsPresenter.READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            mPresenter.onRequestPermissionsResult(grantResults);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent resultData) {
        Log.d(TAG, "SettingsActivity.onActivityResult()");

        if (requestCode == SettingsPresenter.READ_EXTERNAL_STORAGE_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            mPresenter.processChosenCodeLib(resultData);
        }
    }
}
