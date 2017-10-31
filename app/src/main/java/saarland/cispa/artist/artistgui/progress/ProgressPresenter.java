package saarland.cispa.artist.artistgui.progress;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;

import static saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher.ACTION_DETAILED_UPDATE;
import static saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher.ACTION_INSTRUMENTATION_RESULT;
import static saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher.ACTION_STAGE_UPDATE;
import static saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher.EXTRA_PACKAGE_NAME;
import static saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher.EXTRA_PROGRESS;
import static saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher.EXTRA_STATUS_MESSAGE;

public class ProgressPresenter implements ProgressContract.Presenter {

    private Context mContext;
    private ProgressContract.View mView;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            InstrumentationService.InstrumentationServiceBinder binder =
                    (InstrumentationService.InstrumentationServiceBinder) service;
            InstrumentationService instrumentationService = binder.getService();
            instrumentationService.cancel();
            mContext.unbindService(this);
            mView.onFinishedInstrumentation();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);

                switch (action) {
                    case ACTION_STAGE_UPDATE:
                        int progress = intent.getIntExtra(EXTRA_PROGRESS, 0);
                        String message = intent.getStringExtra(EXTRA_STATUS_MESSAGE);
                        mView.onProgressStageChanged(progress, packageName, message);
                        break;
                    case ACTION_DETAILED_UPDATE:
                        message = intent.getStringExtra(EXTRA_STATUS_MESSAGE);
                        mView.onProgressDetailChanged(packageName, message);
                        break;
                    case ACTION_INSTRUMENTATION_RESULT:
                        mView.onFinishedInstrumentation();
                        break;
                }
            }
        }
    };

    ProgressPresenter(Context context, ProgressContract.View view) {
        mContext = context;
        mView = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STAGE_UPDATE);
        intentFilter.addAction(ACTION_DETAILED_UPDATE);
        intentFilter.addAction(ACTION_INSTRUMENTATION_RESULT);
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(mResultReceiver, intentFilter);
    }

    @Override
    public void cancelInstrumentation() {
        Intent intent = new Intent(mContext, InstrumentationService.class);
        mContext.bindService(intent, mConnection, 0);
    }

    @Override
    public void destroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mResultReceiver);
    }
}
