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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import saarland.cispa.artist.artistgui.MainActivity;
import saarland.cispa.artist.artistgui.compilation.notification.CompileNotificationManager;
import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;
import saarland.cispa.artist.artistgui.settings.db.AddInstrumentedPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;
import saarland.cispa.artist.artistgui.utils.AndroidUtils;
import trikita.log.Log;

import static android.app.Activity.RESULT_OK;

public class CompilationPresenter implements CompilationContract.Presenter {

    public static final String TAG = "CompileActivity";

    private final CompilationContract.View mView;
    private final Activity mActivity;
    private final SettingsManager mSettingsManager;

    private ArtistAppConfig mConfig = null;
    private CompilationService compileService;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mCompileServiceConnection;

    public CompilationPresenter(Activity activity, CompilationContract.View view,
                                SettingsManager settingsManager) {
        mConfig = new ArtistAppConfig();
        mActivity = activity;
        mView = view;
        mSettingsManager = settingsManager;

        mView.setPresenter(this);

        mCompileServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d(TAG, "onServiceConnected()");
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                CompilationService.CompilationServiceBinder binder
                        = (CompilationService.CompilationServiceBinder) service;
                compileService = binder.getService();
                if (!compileService.isCompiling()) {
                    CompileNotificationManager.cancelNotification(activity.getApplicationContext());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.d(TAG, "onServiceDisconnected()");
                compileService = null;
            }
        };
    }

    @Override
    public void executeIntentTasks(Intent intent) {
        if (intent != null && intent.hasExtra(MainActivity.EXTRA_PACKAGE)) {
            final String packageName = intent.getStringExtra(MainActivity.EXTRA_PACKAGE);
            if (packageName != null) {
                Log.d(TAG, "CompilationTask() Execute: " + packageName);
                queueCompilation(packageName);
            }
        }
    }

    @Override
    public void start() {
        connectToCompilationService();
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
    public void connectToCompilationService() {
        Log.d(TAG, "connectToCompilationService()");
        CompilationService.startService(mActivity.getApplicationContext(), null);
        Intent intent = new Intent(mActivity, CompilationService.class);
        mActivity.bindService(intent, mCompileServiceConnection, Context.BIND_AUTO_CREATE);
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

    public ServiceConnection getCompileServiceConnection() {
        return mCompileServiceConnection;
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
    public void queueCompilation(String packageName) {
        Log.d(TAG, "compileInstalledApp(): " + packageName);
        CompileDialogActivity.compile(mActivity, packageName);
    }

    @Override
    public void onCompilationFinished(int resultCode, Intent data) {
        String applicationName = "";
        if (data != null) {
            applicationName += data.getStringExtra(ArtistImpl.INTENT_EXTRA_APP_NAME);
        }

        boolean success = resultCode == RESULT_OK;
        mView.showCompilationResult(success, applicationName);

        if (success) {
            new AddInstrumentedPackageToDbAsyncTask(mActivity).execute(applicationName);
            maybeStartRecompiledApp(applicationName);
        }
    }
}
