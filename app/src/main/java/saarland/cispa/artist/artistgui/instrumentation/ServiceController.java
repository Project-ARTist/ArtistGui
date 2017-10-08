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

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import saarland.cispa.artist.artistgui.instrumentation.config.ArtistRunConfig;
import saarland.cispa.artist.artistgui.instrumentation.progress.NotificationManager;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressListener;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher;
import saarland.cispa.artist.artistgui.settings.config.ArtistConfigFactory;
import saarland.cispa.artist.artistgui.utils.ProcessExecutor;
import trikita.log.Log;

class ServiceController implements IServiceController {

    public static final String TAG = "ServiceController";

    private Context mAppContext;
    private ExecutorService mThreadPool;

    private final Queue<InstrumentationTask> mInstrumentationQueue = new LinkedList<>();
    private List<ProgressListener> mProgressListener;


    ServiceController(Context appContext) {
        mAppContext = appContext;
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(appContext);

        mProgressListener = new ArrayList<>();
        mProgressListener.add(new ProgressPublisher(broadcastManager));
        mProgressListener.add(new NotificationManager(appContext));
    }

    @Override
    public void moveToForeground(Service service) {
        Notification notification = NotificationManager.getNotification(mAppContext);
        service.startForeground(NotificationManager.INSTRUMENTATION_NOTIFICATION_ID, notification);
    }

    @Override
    public void instrument(String packageName) {
        Log.d(TAG, String.format("instrument(%s)", packageName));
        InstrumentationTask task = createInstrumentationTask(packageName);
        if (!mInstrumentationQueue.contains(task)) {
            createOrRestartThreadPool();
            mThreadPool.submit(task);
            mInstrumentationQueue.add(task);
        }
    }

    private InstrumentationTask createInstrumentationTask(String packageName) {
        final ArtistRunConfig runConfig =
                ArtistConfigFactory.buildArtistRunConfig(mAppContext, packageName);
        return new InstrumentationTask(mAppContext, runConfig, mProgressListener);
    }

    private void createOrRestartThreadPool() {
        if (mThreadPool == null || mThreadPool.isTerminated() || mThreadPool.isShutdown()) {
            mThreadPool = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void cancel() {
        Log.d(TAG, "cancel()");
        mInstrumentationQueue.clear();
        if (mThreadPool != null) {
            mThreadPool.shutdownNow();
        }
        ProcessExecutor.killAllExecutorProcesses();
    }

    @Override
    public boolean isInstrumenting() {
        return mInstrumentationQueue.peek() != null;
    }

    @Override
    public void processResult(String packageName) {
        InstrumentationTask task = mInstrumentationQueue.peek();
        if (packageName.equals(task.getPackageName())) {
            mInstrumentationQueue.poll();
        }
    }
}
