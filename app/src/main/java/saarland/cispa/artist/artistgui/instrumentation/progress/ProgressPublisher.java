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

package saarland.cispa.artist.artistgui.instrumentation.progress;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

public class ProgressPublisher implements ProgressListener {

    /**
     * Instrumentation status broadcast constants
     */
    public static final String ACTION_STAGE_UPDATE =
            "saarland.cispa.artist.artistgui.action.STAGE_UPDATE";
    public static final String ACTION_DETAILED_UPDATE =
            "saarland.cispa.artist.artistgui.action.DETAILED_UPDATE";

    public static final String EXTRA_PACKAGE_NAME =
            "saarland.cispa.artist.artistgui.extra.PACKAGE_NAME";
    public static final String EXTRA_PROGRESS =
            "saarland.cispa.artist.artistgui.extra.PROGRESS";
    public static final String EXTRA_STATUS_MESSAGE =
            "saarland.cispa.artist.artistgui.extra.INSTRUMENTATION_STATUS_MESSAGE";

    /**
     * Instrumentation result broadcast constants
     */
    public static final String ACTION_INSTRUMENTATION_RESULT =
            "saarland.cispa.artist.artistgui.action.INSTRUMENTATION_RESULT";
    public static final String EXTRA_INSTRUMENTATION_RESULT =
            "saarland.cispa.artist.artistgui.extra.INSTRUMENTATION_RESULT";

    private LocalBroadcastManager mBroadcastManager;

    public ProgressPublisher(LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
    }

    @Override
    public void prepareReporter() {
    }

    @Override
    public void reportProgressStage(@NonNull String packageName, int progress,
                                    @NonNull String stage) {
        Intent intent = buildIntent(ACTION_STAGE_UPDATE, packageName, stage);
        intent.putExtra(EXTRA_PROGRESS, progress);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void reportProgressDetails(@NonNull String packageName, @NonNull String message) {
        Intent intent = buildIntent(ACTION_DETAILED_UPDATE, packageName, message);
        mBroadcastManager.sendBroadcast(intent);
    }

    private Intent buildIntent(@NonNull String action, @NonNull String packageName,
                               @NonNull String message) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(EXTRA_STATUS_MESSAGE, message);
        return intent;
    }

    @Override
    public void onSuccess(@NonNull String packageName) {
        reportResult(packageName, true);
    }

    @Override
    public void onFailure(@NonNull String packageName) {
        reportResult(packageName, false);
    }

    private void reportResult(@NonNull String packageName, boolean isSuccess) {
        Intent intent = new Intent(ACTION_INSTRUMENTATION_RESULT);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(EXTRA_INSTRUMENTATION_RESULT, isSuccess);
        mBroadcastManager.sendBroadcast(intent);
    }
}
