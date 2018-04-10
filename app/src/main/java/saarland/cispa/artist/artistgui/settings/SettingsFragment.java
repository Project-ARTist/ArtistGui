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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;

public class SettingsFragment extends PreferenceFragment implements SettingsContract.View {

    private static final String LOG_LEVEL_STATE_KEY = "log_level";
    private static final String COMPILER_THREADS_STATE_KEY = "compiler_threads";

    private SettingsContract.Presenter mPresenter;

    private Preference mLogLevelPref;
    private Preference mCompilerThreadsPref;

    @Override
    public void setPresenter(SettingsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mLogLevelPref = findPreference(ArtistAppConfig.KEY_PREF_GENERAL_LOGLEVEL);
        mCompilerThreadsPref = findPreference(ArtistAppConfig.KEY_PREF_COMPILER_THREADS);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean isNewInstance = savedInstanceState == null;
        mPresenter.setupLoggingListener(isNewInstance, mLogLevelPref);
        mPresenter.bindPrefValueToSummary(isNewInstance, mCompilerThreadsPref);

        if (!isNewInstance) {
            restoreSavedInstance(savedInstanceState);
        }
    }

    private void restoreSavedInstance(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        String logLevel = savedInstanceState.getString(LOG_LEVEL_STATE_KEY);
        mLogLevelPref.setSummary(logLevel);

        String compilerThreads = savedInstanceState.getString(COMPILER_THREADS_STATE_KEY);
        mCompilerThreadsPref.setSummary(compilerThreads);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOG_LEVEL_STATE_KEY, mLogLevelPref.getSummary().toString());
        outState.putString(COMPILER_THREADS_STATE_KEY, mCompilerThreadsPref.getSummary()
                .toString());
    }
}
