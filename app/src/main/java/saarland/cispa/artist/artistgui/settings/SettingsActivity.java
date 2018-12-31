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
import android.app.FragmentManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Material_Settings);

        FragmentManager fragmentManager = getFragmentManager();
        SettingsFragment mFragment;

        if (savedInstanceState != null) {
            mFragment = (SettingsFragment) fragmentManager.findFragmentById(android.R.id.content);
            // Presenter binds itself to view
            new SettingsPresenter(mFragment);
        } else {
            mFragment = new SettingsFragment();
            // Presenter binds itself to view
            new SettingsPresenter(mFragment);
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, mFragment)
                    .commit();
        }
    }
}
