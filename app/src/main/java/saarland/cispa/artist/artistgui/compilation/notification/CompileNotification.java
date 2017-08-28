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

import android.content.Context;

import saarland.cispa.artist.android.ArtistGuiProgress;
import saarland.cispa.artist.log.LogG;
import trikita.log.Log;

public class CompileNotification implements ArtistGuiProgress {

    private static final String TAG = LogG.TAG;
    
    private Context appContext;

    public CompileNotification(final Context context) {
        this.appContext = context.getApplicationContext();
        CompileNotificationManager.showNotification(this.appContext);
    }

    @Override
    public void updateProgress(final int progress, final String message) {
        Log.v(TAG, "CompileNotification.updateProgress()");
        CompileNotificationManager.updateNotification(this.appContext, progress, message);
    }

    @Override
    public void updateProgressVerbose(int progress, String message) {
        Log.v(TAG, "CompileNotification.updateProgressVerbose()");
    }

    @Override
    public void kill(final String message) {
        Log.v(TAG, "CompileNotification.kill()");
        CompileNotificationManager.cancelNotification(this.appContext);
    }

    @Override
    public void doneSuccess(final String message) {
        Log.v(TAG, "CompileNotification.doneSuccess()");
         CompileNotificationManager.finishNotification(this.appContext, message);
    }

    @Override
    public void doneFailed(final String message) {
        Log.v(TAG, "CompileNotification.doneFailed()");
        CompileNotificationManager.failNotification(this.appContext, message);
    }

    @Override
    public void done() {
        Log.v(TAG, "CompileNotification.done()");
        CompileNotificationManager.closeDelayed(this.appContext);
    }
}
