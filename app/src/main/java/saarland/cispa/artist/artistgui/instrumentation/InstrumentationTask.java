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

package saarland.cispa.artist.artistgui.instrumentation;

import android.content.Context;

import java.util.List;

import saarland.cispa.artist.artistgui.instrumentation.config.ArtistRunConfig;
import saarland.cispa.artist.artistgui.instrumentation.exceptions.InstrumentationException;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressListener;
import saarland.cispa.artist.artistgui.instrumentation.stages.InstrumentationStages;
import saarland.cispa.artist.artistgui.instrumentation.stages.InstrumentationStagesImpl;
import saarland.cispa.artist.artistgui.instrumentation.exceptions.ArtistInterruptedException;
import trikita.log.Log;

class InstrumentationTask implements Runnable {

    private static final String TAG = "InstrumentationTask";

    private ArtistRunConfig mRunConfig;
    private List<ProgressListener> mProgressListeners;
    private InstrumentationStages mInstrumenationStages;

    InstrumentationTask(Context context, ArtistRunConfig runConfig,
                        List<ProgressListener> listeners) {
        this.mRunConfig = runConfig;
        mProgressListeners = listeners;
        mInstrumenationStages = new InstrumentationStagesImpl(context, mRunConfig,
                mProgressListeners);
    }

    @Override
    public void run() {
        Log.i(TAG, "Run() compiling and starting " + mRunConfig.app_package_name);
        Log.i(TAG, "> apkPath:     " + mRunConfig.app_apk_file_path);
        Log.i(TAG, "> codeLibName: " + mRunConfig.codeLibName);
        Log.i(TAG, "> Keystore:    " + mRunConfig.keystore);

        try {
            ArtistThread.checkThreadCancellation();
            prepareReporter();

            reportProgress(10, "Preparing build environment");
            String pathDex2oat = mInstrumenationStages.prepareEnvironment();

            ArtistThread.checkThreadCancellation();
            mInstrumenationStages.probePermissionAndDeleteOatFile();

            ArtistThread.checkThreadCancellation();
            reportProgress(40, "Merging CodeLib");
            mInstrumenationStages.mergeCodeLib();
            ArtistThread.checkThreadCancellation();
            mInstrumenationStages.backupMergedApk();

            ArtistThread.checkThreadCancellation();
            reportProgress(50, "Compiling: " + mRunConfig.app_package_name);
            mInstrumenationStages.runDex2OatCompilation(pathDex2oat);

            ArtistThread.checkThreadCancellation();
            reportProgress(90, "Compilation done, setting file permissions");
            mInstrumenationStages.setOatFilePermissions();
        } catch (InstrumentationException | ArtistInterruptedException e) {
            reportResult(false);
            return;
        }
        reportResult(true);
    }

    private void prepareReporter() {
        for (ProgressListener listener : mProgressListeners) {
            listener.prepareReporter();
        }
    }

    private void reportProgress(int progress, String message) {
        for (ProgressListener listener : mProgressListeners) {
            listener.reportProgressStage(mRunConfig.app_package_name, progress, message);
        }
    }

    private void reportResult(boolean isSuccess) {
        for (ProgressListener listener : mProgressListeners) {
            if (isSuccess) listener.onSuccess(mRunConfig.app_package_name);
            else listener.onFailure(mRunConfig.app_package_name);
        }
    }

    public String getPackageName() {
        return mRunConfig.app_package_name;
    }
}
