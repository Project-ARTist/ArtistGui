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

package saarland.cispa.artist.artistgui.compilation;

import android.content.Intent;
import android.content.ServiceConnection;

import saarland.cispa.artist.artistgui.base.BasePresenter;
import saarland.cispa.artist.artistgui.base.BaseView;

public interface CompilationContract {

    interface View extends BaseView<Presenter> {
        void showNoCodeLibChosenMessage();

        void showCompilationResult(boolean isSuccess, String packageName);
    }

    interface Presenter extends BasePresenter {
        void checkIfCodeLibIsChosen();

        void connectToCompilationService();

        void createArtistFolders();

        ServiceConnection getCompileServiceConnection();

        boolean writeResultFile(String packageName, boolean success);

        void maybeStartRecompiledApp(final String applicationName);

        void queueCompilation(String packageName);

        void executeIntentTasks(Intent intent);

        void onCompilationFinished(int resultCode, Intent data);
    }
}
