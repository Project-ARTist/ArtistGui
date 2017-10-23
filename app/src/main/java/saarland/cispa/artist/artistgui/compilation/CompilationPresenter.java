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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import saarland.cispa.artist.artistgui.MainActivity;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;
import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;
import saarland.cispa.artist.artistgui.settings.db.operations.AddInstrumentedPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;
import saarland.cispa.artist.artistgui.utils.AndroidUtils;
import trikita.log.Log;

public class CompilationPresenter implements CompilationContract.Presenter {

    public static final String TAG = "CompileActivity";

    private final CompilationContract.View mView;
    private final Activity mActivity;
    private final SettingsManager mSettingsManager;

    private ArtistAppConfig mConfig = null;

    public CompilationPresenter(Activity activity, CompilationContract.View view,
                                SettingsManager settingsManager) {
        mConfig = new ArtistAppConfig();
        mActivity = activity;
        mView = view;
        mSettingsManager = settingsManager;

        mView.setPresenter(this);
    }

    @Override
    public void executeIntentTasks(Intent intent) {
        if (intent != null && intent.hasExtra(MainActivity.EXTRA_PACKAGE)) {
            final String packageName = intent.getStringExtra(MainActivity.EXTRA_PACKAGE);
            if (packageName != null) {
                Log.d(TAG, "CompilationTask() Execute: " + packageName);
                queueInstrumentation(packageName);
            }
        }
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

    @Override
    public void maybeStartRecompiledApp(String applicationName) {
        Log.d(TAG, "maybeStartRecompiledApp() ? " + applicationName);
        final boolean launchActivity = mSettingsManager.shouldLaunchActivityAfterCompilation();
        if (launchActivity) {
            Log.d(TAG, "Starting compiled app: " + applicationName);
            final Intent launchIntent = mActivity.getPackageManager()
                    .getLaunchIntentForPackage(applicationName);
            mActivity.startActivity(launchIntent);
        }
    }

    @Override
    public void queueInstrumentation(String packageName) {
        Intent intent = new Intent(mActivity, InstrumentationService.class);
        intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, packageName);
        mActivity.startService(intent);
        mView.showInstrumentationProgress();
    }

    @Override
    public void handleInstrumentationResult(Context context, boolean isSuccess,
                                            String packageName) {
        mView.showInstrumentationResult(isSuccess, packageName);

        if (isSuccess) {
            new AddInstrumentedPackageToDbAsyncTask(context).execute(packageName);
            maybeStartRecompiledApp(packageName);
        }
    }
}
