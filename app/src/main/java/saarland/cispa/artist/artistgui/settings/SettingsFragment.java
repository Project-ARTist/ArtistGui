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

    private static final String LOG_LEVEL_STATE_KEY = "log_level";
    private static final String COMPILER_THREADS_STATE_KEY = "compiler_threads";
    private static final String CHOSEN_CODELIB_STATE_KEY = "chosen_codelib";

    private static final String SELECTABLE_CODELIB_ENTRIES_STATE_KEY = "selectable_codelib_entries";
    private static final String SELECTABLE_CODELIB_ENTRY_VALUES_STATE_KEY = "selectable_codelib_values";

    private SettingsContract.Presenter mPresenter;

    private Preference mLogLevelPref;
    private Preference mCompilerThreadsPref;
    private Preference mCodeLibImportPref;
    private ListPreference mCodeLibSelectionPref;

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
        mCodeLibImportPref = findPreference(ArtistAppConfig.PREF_KEY_CODELIB_IMPORT);
        mCodeLibSelectionPref = (ListPreference) findPreference(ArtistAppConfig
                .PREF_KEY_CODELIB_SELECTION);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean isNewInstance = savedInstanceState == null;
        mPresenter.setupLoggingListener(isNewInstance, mLogLevelPref);
        mPresenter.bindPrefValueToSummary(isNewInstance, mCompilerThreadsPref);

        mPresenter.setupCodeLibImport(getActivity(), mCodeLibImportPref);

        if (!isNewInstance) {
            restoreSavedInstance(savedInstanceState);
        } else {
            mPresenter.setupCodeLibSelection(mCodeLibSelectionPref);
        }

        mPresenter.bindPrefValueToSummary(isNewInstance, mCodeLibSelectionPref);
    }

    private void restoreSavedInstance(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        String logLevel = savedInstanceState.getString(LOG_LEVEL_STATE_KEY);
        mLogLevelPref.setSummary(logLevel);

        String compilerThreads = savedInstanceState.getString(COMPILER_THREADS_STATE_KEY);
        mCompilerThreadsPref.setSummary(compilerThreads);

        // CodeLibSelection pref has no default value
        String chosenCodeLib = savedInstanceState.getString(CHOSEN_CODELIB_STATE_KEY, "");
        mCodeLibSelectionPref.setSummary(chosenCodeLib);

        CharSequence[] codeLibSelectionEntries = savedInstanceState
                .getCharSequenceArray(SELECTABLE_CODELIB_ENTRIES_STATE_KEY);
        mCodeLibSelectionPref.setEntries(codeLibSelectionEntries);

        CharSequence[] codeLibEntryValues = savedInstanceState
                .getCharSequenceArray(SELECTABLE_CODELIB_ENTRY_VALUES_STATE_KEY);
        mCodeLibSelectionPref.setEntryValues(codeLibEntryValues);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOG_LEVEL_STATE_KEY, mLogLevelPref.getSummary().toString());
        outState.putString(COMPILER_THREADS_STATE_KEY, mCompilerThreadsPref.getSummary()
                .toString());
        outState.putString(CHOSEN_CODELIB_STATE_KEY, mCodeLibSelectionPref.getSummary()
                .toString());

        outState.putCharSequenceArray(SELECTABLE_CODELIB_ENTRIES_STATE_KEY,
                mCodeLibSelectionPref.getEntries());
        outState.putCharSequenceArray(SELECTABLE_CODELIB_ENTRY_VALUES_STATE_KEY,
                mCodeLibSelectionPref.getEntryValues());
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
