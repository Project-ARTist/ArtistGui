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

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import saarland.cispa.utils.LogA;

class SettingsPresenter implements SettingsContract.Presenter {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener mBindValueToSummaryListener =
            (preference, value) -> {
                bindValueToSummary(preference, value);
                return true;
            };

    private Preference.OnPreferenceChangeListener mLoggingPrefListener =
            (preference, value) -> {
                bindValueToSummary(preference, value);
                LogA.setUserLogLevel(value.toString());
                return true;
            };

    SettingsPresenter(SettingsContract.View view) {
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void bindPrefValueToSummary(boolean isNewInstance, Preference preference) {
        preference.setOnPreferenceChangeListener(mBindValueToSummaryListener);
        if (isNewInstance) {
            triggerInitialOnPreferenceChange(preference, mBindValueToSummaryListener);
        }
    }

    @Override
    public void setupLoggingListener(boolean isNewInstance, Preference preference) {
        preference.setOnPreferenceChangeListener(mLoggingPrefListener);
        if (isNewInstance) {
            triggerInitialOnPreferenceChange(preference, mLoggingPrefListener);
        }
    }

    private void bindValueToSummary(Preference preference, Object value) {
        ListPreference listPreference = (ListPreference) preference;
        final String stringValue = value.toString();
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        preference.setSummary(index >= 0 ? listPreference.getEntries()[index]
                : null);
    }

    private void triggerInitialOnPreferenceChange(Preference preference,
                                                  Preference.OnPreferenceChangeListener listener) {
        listener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
