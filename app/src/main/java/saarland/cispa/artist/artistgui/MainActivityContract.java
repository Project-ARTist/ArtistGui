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

import android.content.Intent;
import android.support.v4.app.Fragment;

import saarland.cispa.artist.artistgui.base.BasePresenter;
import saarland.cispa.artist.artistgui.base.BaseView;

interface MainActivityContract {
    interface View extends BaseView<MainActivityContract.Presenter> {
        void onIncompatibleAndroidVersion();

        void onFragmentSelected(Fragment fragment);
    }

    interface Presenter extends BasePresenter {
        void checkCompatibility();

        void processIntent(Intent intent);

        void selectFragment(@MainActivityPresenter.selectableFragment int id);

        void onRestoreSavedInstance(int selectedFragmentId, Fragment selectedFragment);

        int getSelectedFragmentId();
    }
}
