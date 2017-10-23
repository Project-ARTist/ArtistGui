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

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;

import saarland.cispa.artist.artistgui.R;

import static android.app.Notification.EXTRA_PROGRESS;
import static android.app.Notification.EXTRA_PROGRESS_INDETERMINATE;
import static android.app.Notification.EXTRA_PROGRESS_MAX;
import static android.app.Notification.EXTRA_SUB_TEXT;

public class NotificationManager implements ProgressListener {

    public final static int INSTRUMENTATION_NOTIFICATION_ID = Integer.MAX_VALUE - 23;
    private final static int PROGRESS_MAX = 100;

    private Context mContext;
    private android.app.NotificationManager mNotificationManager;
    private Notification mNotification;

    public NotificationManager(Context serviceContext) {
        this.mContext = serviceContext;
        mNotificationManager = (android.app.NotificationManager) serviceContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void prepareReporter() {
        mNotification = getNotification(mContext);
        mNotificationManager.notify(INSTRUMENTATION_NOTIFICATION_ID, mNotification);
    }

    public static Notification getNotification(Context mContext) {
        Notification.Builder notificationBuilder = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_icon_compiler)
                .setContentTitle(mContext.getString(R.string.notification_instrumentation_title))
                .setProgress(PROGRESS_MAX, 0, false)
                .setOngoing(true)
                .setAutoCancel(false);

        return notificationBuilder.build();
    }

    @Override
    public void reportProgressStage(@NonNull String packageName, int progress, @NonNull String stage) {
        mNotification.extras.putInt(EXTRA_PROGRESS, progress);
        mNotification.extras.putInt(EXTRA_PROGRESS_MAX, PROGRESS_MAX);
        mNotification.extras.putBoolean(EXTRA_PROGRESS_INDETERMINATE, true);

        mNotification.extras.putCharSequence(EXTRA_SUB_TEXT, stage);
        mNotificationManager.notify(INSTRUMENTATION_NOTIFICATION_ID, mNotification);
    }

    @Override
    public void reportProgressDetails(@NonNull String packageName, @NonNull String message) {
    }

    @Override
    public void onSuccess(@NonNull String packageName) {
        dismissNotification();
    }

    @Override
    public void onFailure(@NonNull String packageName) {
        dismissNotification();
    }

    private void dismissNotification() {
        mNotificationManager.cancel(INSTRUMENTATION_NOTIFICATION_ID);
    }
}
