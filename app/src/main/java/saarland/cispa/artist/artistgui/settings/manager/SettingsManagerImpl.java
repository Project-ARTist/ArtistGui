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

package saarland.cispa.artist.artistgui.settings.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;

public class SettingsManagerImpl implements SettingsManager {

    private final SharedPreferences mSharedPreferences;

    public SettingsManagerImpl(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getSelectedCodeLib() {
        return mSharedPreferences.getString(ArtistAppConfig.PREF_KEY_CODELIB_SELECTION, null);
    }

    @Override
    public boolean shouldInjectCodeLib() {
        return mSharedPreferences
                .getBoolean(ArtistAppConfig.KEY_PREF_COMPILER_INJECT_CODELIB, true);
    }

    @Override
    public boolean shouldLaunchActivityAfterCompilation() {
        return mSharedPreferences
                .getBoolean(ArtistAppConfig.KEY_PREF_COMPILER_LAUNCH_ACTIVITY, false);
    }
}
