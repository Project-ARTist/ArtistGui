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

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;

public class SettingsFragment extends PreferenceFragment implements SettingsContract.View {

    private SettingsContract.Presenter mPresenter;

    @Override
    public void setPresenter(SettingsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference logLevelPref = findPreference(ArtistAppConfig.KEY_PREF_GENERAL_LOGLEVEL);
        mPresenter.setupLoggingListener(logLevelPref);

        Preference compilerPref = findPreference(ArtistAppConfig.KEY_PREF_COMPILER_THREADS);
        mPresenter.bindPrefValueToSummary(compilerPref);

        ListPreference codeLibSelectionPref = (ListPreference) findPreference(ArtistAppConfig
                .PREF_KEY_CODELIB_SELECTION);
        mPresenter.bindPrefValueToSummary(codeLibSelectionPref);
        mPresenter.setupCodeLibSelection(codeLibSelectionPref);

        Preference codeLibImportPref = findPreference(ArtistAppConfig.PREF_KEY_CODELIB_IMPORT);
        mPresenter.setupCodeLibImport(getActivity(), codeLibImportPref);

    }

    @Override
    public void showFileChooser(String typeFilter) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(typeFilter);
        getActivity().startActivityForResult(intent, SettingsPresenter
                .READ_EXTERNAL_STORAGE_REQUEST_CODE);
    }
}
