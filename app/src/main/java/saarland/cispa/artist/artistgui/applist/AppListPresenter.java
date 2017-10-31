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

package saarland.cispa.artist.artistgui.applist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;
import saarland.cispa.artist.artistgui.settings.db.operations.AddInstrumentedPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;
import saarland.cispa.artist.artistgui.utils.AndroidUtils;
import trikita.log.Log;

public class AppListPresenter implements AppListContract.Presenter {

    public static final String TAG = "CompileActivity";

    private final AppListContract.View mView;
    private final Activity mActivity;
    private final SettingsManager mSettingsManager;

    private ArtistAppConfig mConfig = null;

    public AppListPresenter(Activity activity, AppListContract.View view,
                            SettingsManager settingsManager) {
        mConfig = new ArtistAppConfig();
        mActivity = activity;
        mView = view;
        mSettingsManager = settingsManager;

        mView.setPresenter(this);
    }

    @Override
    public void start() {
        createArtistFolders();
    }

    @Override
    public void checkIfCodeLibIsChosen() {
        final String userCodeLib = mSettingsManager.getSelectedCodeLib();

        final boolean codeLibChosen = userCodeLib != null && !userCodeLib.equals("-1");
        final boolean shouldMerge = mSettingsManager.shouldInjectCodeLib();
        // warn the user IF no code lib is chosen AND code lib should be merged
        if (!codeLibChosen && shouldMerge) {
            mView.showNoCodeLibChosenMessage();
        }
    }

    @Override
    public void createArtistFolders() {
        if (mConfig.apkBackupFolderLocation.isEmpty()) {
            mConfig.apkBackupFolderLocation =
                    AndroidUtils.createFoldersInFilesDir(mActivity.getApplicationContext(),
                            ArtistAppConfig.APP_FOLDER_APK_BACKUP);
        }
        if (mConfig.codeLibFolder.isEmpty()) {
            mConfig.codeLibFolder = AndroidUtils.createFoldersInFilesDir(mActivity
                    .getApplicationContext(), ArtistAppConfig.APP_FOLDER_CODELIBS);
        }
    }
}
