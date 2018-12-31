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

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import saarland.cispa.artist.artistgui.base.BasePresenter;
import saarland.cispa.artist.artistgui.base.BaseView;

interface MainActivityContract {
    interface View extends BaseView<MainActivityContract.Presenter> {
        void showIncompatibleAndroidVersionDialog();

        void showMissingDex2OatFilesDialog();

        void showSelectedFragment(Fragment fragment);
    }

    interface Presenter extends BasePresenter {
        void checkCompatibility();

        void openDex2OatHelpPage();

        void selectFragment(@MainActivityPresenter.selectableFragment int id);

        void saveInstanceState(Bundle outState);

        void restoreSavedInstanceState(Bundle savedInstanceState, FragmentManager fragmentManager);
    }
}
