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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher;
import saarland.cispa.artist.artistgui.utils.LogA;
import trikita.log.Log;

public class InstrumentationService extends Service {

    private final static String TAG = "CompilationService";

    public static final String INTENT_KEY_APP_NAME = "app_name";
    private static final boolean ALLOW_REBIND = true;

    private final IBinder mBinder;
    private boolean mClientsBoundToService;
    private IServiceController mServiceController;

    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null &&
                    ProgressPublisher.ACTION_INSTRUMENTATION_RESULT.equals(intent.getAction())) {
                String packageName = intent.getStringExtra(ProgressPublisher.EXTRA_PACKAGE_NAME);
                mServiceController.processResult(packageName);
                stopIfNothingToDo();
            }
        }
    };

    /**
     * Class used for the client Binder.
     * <p>
     * Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class InstrumentationServiceBinder extends Binder {
        public InstrumentationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return InstrumentationService.this;
        }
    }

    public InstrumentationService() {
        mBinder = new InstrumentationService.InstrumentationServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "CompilationService()");
        LogA.setUserLogLevel(getApplicationContext());
        mServiceController = new ServiceController(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ProgressPublisher.ACTION_INSTRUMENTATION_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mResultReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        if (intent != null) {
            final String packageName = intent.getStringExtra(INTENT_KEY_APP_NAME);
            if (packageName != null && !packageName.isEmpty()) {
                Log.d(TAG, "onStartCommand() Extra: " + packageName);
                instrumentApp(packageName);
            }
        }
        return START_STICKY;
    }

    private void instrumentApp(String packageName) {
        mServiceController.moveToForeground(this);
        mServiceController.instrument(packageName);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mClientsBoundToService = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind()");
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        mClientsBoundToService = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        mClientsBoundToService = false;
        stopIfNothingToDo();
        return ALLOW_REBIND;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mResultReceiver);
    }

    public void cancel() {
        mServiceController.cancel();
        stopIfNothingToDo();
    }

    private void stopIfNothingToDo() {
        if (!mClientsBoundToService && !mServiceController.isInstrumenting()) {
            stopSelf();
        }
    }
}
