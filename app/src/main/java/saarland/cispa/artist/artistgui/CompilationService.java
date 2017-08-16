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
package saarland.cispa.artist.artistgui;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import saarland.cispa.artist.ArtistCompilationTask;
import saarland.cispa.artist.ArtistImpl;
import saarland.cispa.artist.CompilationResultReceiver;
import saarland.cispa.artist.artistgui.compilation.notification.CompileNotification;
import saarland.cispa.artist.artistgui.compilation.notification.CompileNotificationManager;
import saarland.cispa.artist.artistgui.settings.config.ArtistConfigFactory;
import saarland.cispa.artist.gui.artist.ArtistGuiProgress;
import saarland.cispa.artist.log.Logg;
import saarland.cispa.artist.settings.ArtistRunConfig;
import saarland.cispa.artist.utils.ProcessExecutor;
import trikita.log.Log;

public class CompilationService extends Service {

    private final static String TAG = "ArtistService";

    public static final String EXTRA_APP_NAME = "app_name";

    private final static int THREAD_COUNT = 1;

    private final int mStartMode = START_STICKY;       // indicates how to behave if the service is killed
    private final IBinder mBinder = new CompilationServiceBinder();
    private final boolean ALLOW_REBIND = true; // indicates whether onRebind should be used

    private ExecutorService pool;

    private Map<String, Future<Boolean>> compilationResults = new ConcurrentHashMap<>();
    private final Queue<ArtistCompilationTask> compilationQueue = new ConcurrentLinkedQueue<>();

    /**
     * Class used for the client Binder.
     *
     * Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class CompilationServiceBinder extends Binder {
        public CompilationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CompilationService.this;
        }
    }

    public CompilationService() {
        Log.i(TAG, "CompilationService()");
        pool = newPool();
    }

    private ExecutorService newPool() {
        return Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        Logg.setUserLogLevel(getApplicationContext());
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Logg.setUserLogLevel(getApplicationContext());
        Log.d(TAG, "onStartCommand()");
        if (intent != null) {
            final String appName = intent.getStringExtra(EXTRA_APP_NAME);
            if (appName != null) {
                Log.d(TAG, "onStartCommand() Extra: " + appName);
                Toast.makeText(this, "Artist: CompilationService Started", Toast.LENGTH_SHORT).show();
            }
        }
        return mStartMode;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        Logg.setUserLogLevel(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logg.setUserLogLevel(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // The service is no longer used and is being destroyed
        Toast.makeText(this, TAG + ": CompilationService Destroyed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory()");
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind()");
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved()");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory()");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        return ALLOW_REBIND;
    }

    public static void startService(final Context context, @Nullable final String appName) {
        Log.d(TAG, "startService() " + appName);
        Intent intent = new Intent(context, CompilationService.class);

        if (appName != null && !appName.isEmpty() ) {
            intent.putExtra(EXTRA_APP_NAME, appName);
        }
        context.startService(intent);
        Log.d(TAG, "startService() " + appName + " DONE");
    }

    public boolean compileInstalledApp(final CompilationResultReceiver listener,
                                       final String appPackageName) {
        return compileInstalledApp(listener, appPackageName, null);
    }

    public boolean compileInstalledApp(final CompilationResultReceiver listener,
                                       final String appPackageName,
                                       final ArtistGuiProgress guiCallback) {
        Log.d(TAG, String.format("compileInstalledApp(%s)", appPackageName));

        synchronized (compilationQueue) {

            moveServiceToForeGround();

            for (ArtistCompilationTask task : compilationQueue) {
                if(task.getAppName().equals(appPackageName)) {
                    Log.d(TAG,
                            String.format("compileInstalledApp() Skipping: %s => Already in queue",
                                    appPackageName));
                    return false;
                }
            }
            final ArtistCompilationTask compileTask =
                    createCompilationTask(listener, appPackageName, guiCallback);
            restartExecutorServiceIfNecessary();
            final Future<Boolean> result = this.pool.submit(compileTask);
            compilationQueue.add(compileTask);
            compilationResults.put(appPackageName, result);
        }
        Log.d(TAG, String.format("compileInstalledApp(%s) DONE", appPackageName));
        return true;
    }

    private void moveServiceToForeGround() {
        final Notification serviceNotification =
                CompileNotificationManager.getServiceNotification(getApplicationContext());
        startForeground(CompileNotificationManager.COMPILATION_NOTIFICATION_ID, serviceNotification);
    }

    @NonNull
    private ArtistCompilationTask createCompilationTask(CompilationResultReceiver listener,
                                                        String appPackageName,
                                                        ArtistGuiProgress guiCallback) {
        final ArtistRunConfig artistConfig =
                ArtistConfigFactory.buildArtistRunConfig(getApplicationContext(), appPackageName);

        ArtistCompilationTask compileTask = new ArtistCompilationTask(artistConfig,
                getApplicationContext());
        compileTask.addArtistGuiProgressCallback(new CompileNotification(this));
        if (guiCallback != null) {
            compileTask.addArtistGuiProgressCallback(guiCallback);
        }
        compileTask.addResultCallback(listener);
        return compileTask;
    }

    private void restartExecutorServiceIfNecessary() {
        if (pool.isTerminated() || pool.isShutdown()) {
            pool = newPool();
        }
    }

    public void cancelCompilation(final String appPackageName) {
        Log.d(TAG, String.format("cancelCompilation(%s) : ", appPackageName));
        final boolean KILL_IT_WITH_FIRE = true;
        synchronized (compilationQueue) {
            ArtistCompilationTask compilationTask = compilationQueue.poll();
            Future<Boolean> compilationResult =
                    compilationResults.remove(compilationTask.getAppName());

            boolean killSuccess = false;
            if (compilationResult != null) {
                killSuccess = compilationResult.cancel(KILL_IT_WITH_FIRE);
                Log.d(TAG, String.format("cancelCompilation(%s) KillSuccess: %b",
                        appPackageName, killSuccess));
                Log.d(TAG, "cancelCompilation() Killing Pool");
                pool.shutdownNow();
                Log.w(TAG, "cancelCompilation() Killing Pool: Check if this is really necessary");
            } else {
                Log.d(TAG, String.format("cancelCompilation(%s) Failed: Future not found",
                        appPackageName));
            }
            ProcessExecutor.killAllExecutorProcesses();

            CompilationResultReceiver resultcallback = compilationTask.getResultCallback();
            if (resultcallback != null) {
                resultcallback.send(ArtistImpl.COMPILATION_CANCELED, null);
            }
            Log.d(TAG, String.format("cancelCompilation(%s) DONE (success: %b)",
                appPackageName, killSuccess));
        }
    }

    public String reconnectGuiToService(final ArtistGuiProgress guiCallback,
                                         final CompilationResultReceiver resultReceiver) {
        Log.d(TAG, "reconnectGuiToService()");
        synchronized (compilationQueue) {
            if (isCompiling()) {
                this.registerResultListener(resultReceiver);
                this.registerArtistGuiProgress(guiCallback);
                final ArtistCompilationTask currentTask = compilationQueue.peek();
                if (currentTask != null) {
                    return currentTask.getAppName();
                }
                Log.d(TAG, "reconnectGuiToService() NO Task Found");
            }
            Log.d(TAG, "reconnectGuiToService() NOT Compiling");
            return "";
        }
    }

    public boolean registerArtistGuiProgress(final ArtistGuiProgress guiCallback) {
        synchronized (compilationQueue) {
            ArtistCompilationTask currenTask = compilationQueue.peek();
            if (currenTask != null) {
                currenTask.addArtistGuiProgressCallback(guiCallback);
                return true;
            }
            return false;
        }
    }

    public void registerResultListener(final CompilationResultReceiver mReceiver) {
        synchronized (compilationQueue) {
            final ArtistCompilationTask compileTask = compilationQueue.peek();
            if (compileTask != null) {
                compileTask.addResultCallback(mReceiver);
            }
        }

    }

    public boolean isCompiling() {
        synchronized (compilationQueue) {
            return compilationQueue.peek() != null;
        }
    }

    public String getLastStatusMessage() {
        String lastStatusMessage;
        try {
            final ArtistCompilationTask compileTask = compilationQueue.peek();
            lastStatusMessage = compileTask.getLastStatusMessage();
        } catch (final NullPointerException e) {
            lastStatusMessage= "";
        }
        return lastStatusMessage;
    }

    public void compilationCleanup(final String appPackageName) {
        Log.v(TAG, "compilationCleanup() " + appPackageName);
        synchronized (compilationQueue) {
            final ArtistCompilationTask task = compilationQueue.peek();
            if (task != null
                    && task.getAppName().equals(appPackageName)) {
                Log.v(TAG, "compilationCleanup() removed DONE task from queue" + appPackageName);
                compilationQueue.poll();
            }
            if (compilationQueue.size() == 0) {
                Log.v(TAG, "compilationCleanup() stopForeground()");
                stopForeground(true);
            }
        }
    }
}
