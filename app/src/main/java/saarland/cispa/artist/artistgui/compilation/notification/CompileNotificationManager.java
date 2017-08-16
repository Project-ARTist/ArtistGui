/**
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
 * @author "Oliver Schranz <oliver.schranz@cispa.saarland>"
 * @author "Sebastian Weisgerber <weisgerber@cispa.saarland>"
 *
 */
package saarland.cispa.artist.artistgui.compilation.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import saarland.cispa.artist.artistgui.compilation.CompileDialogActivity;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.log.Logg;
import trikita.log.Log;

public class CompileNotificationManager {

    public final static int COMPILATION_NOTIFICATION_ID = Integer.MAX_VALUE-23;

    private final static int PROGRESS_MAX = 100;

    public static final String TAG = Logg.TAG;


    private static Notification.Builder buildDefault(final Context context) {
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_icon_compiler)
                .setContentTitle(context.getString(R.string.notification_compilation_title))
                .setContentText(context.getString(R.string.notification_compilation_text))
                .setProgress(PROGRESS_MAX, 0, false)
                .setOngoing(true)
                .setAutoCancel(false);

        Intent compileDialogActivity = new Intent(context, CompileDialogActivity.class);
        PendingIntent compileDialogActivityPending = PendingIntent.getActivity(context, 0, compileDialogActivity, 0);
        notificationBuilder.setContentIntent(compileDialogActivityPending);
        return notificationBuilder;
    }

    private static void build(Context context, Notification.Builder notification) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(COMPILATION_NOTIFICATION_ID, notification.build());
    }

    public static Notification getServiceNotification(final Context context) {
        return buildDefault(context).build();
    }

    static void showNotification(final Context context) {
        showNotification(context, context.getString(R.string.notification_compilation_text));
    }

    private static void showNotification(final Context context, final String message) {
        Notification.Builder notification = buildDefault(context);
        notification.setSubText(message);
        build(context, notification);
    }

    static void finishNotification(final Context context, final String message) {
        Notification.Builder notification = buildDefault(context);
        notification.setOngoing(false);
        notification.setAutoCancel(true);
        notification.setProgress(PROGRESS_MAX, PROGRESS_MAX, false);
        notification.setSubText(message);
        scheduleNotificationCancelation(context);
    }

    public static void cancelNotification(final Context context) {
        Log.d(TAG, "cancelNotification");
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(COMPILATION_NOTIFICATION_ID);
    }

    static void updateNotification(final Context context,
                                   final int progressValue,
                                   final String statusText) {
        Notification.Builder notification = buildDefault(context);
        notification.setProgress(PROGRESS_MAX, progressValue, true);
        if (statusText != null && !statusText.isEmpty()) {
            notification.setSubText(statusText);
        }
        build(context, notification);
    }

    static void failNotification(final Context context, final String message) {
        finishNotification(context, "FAILED: " + message);
        scheduleNotificationCancelation(context);
    }

    static void closeDelayed(final Context context) {
        scheduleNotificationCancelation(context);
    }

    private static void scheduleNotificationCancelation(final Context context) {
        Log.v(TAG, "scheduleNotificationCancelation");
        new Thread(() -> {
            try {
                final int SLEEP_TIME = 3000;
                Log.v(TAG, "Sleeping for: " + SLEEP_TIME);
                Thread.sleep(SLEEP_TIME);
                cancelNotification(context);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep failed", e);
            }
        }).run();
    }
}
