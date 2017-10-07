/**
 * The ARTist Project (https://artist.cispa.saarland)
 * <p>
 * Copyright (C) 2017 CISPA (https://cispa.saarland), Saarland University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author "Oliver Schranz <oliver.schranz@cispa.saarland>"
 * @author "Sebastian Weisgerber <weisgerber@cispa.saarland>"
 */
package saarland.cispa.artist.artistgui.compilation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher;
import saarland.cispa.artist.artistgui.utils.LogA;
import trikita.log.Log;

public class CompileDialogActivity extends Activity {

    private static final String TAG = "CompileDialogActivity";

    private String packageName;

    InstrumentationService compileService;
    boolean boundToService = false;

    TextView compileStatus = null;
    TextView compileStatusExtended = null;
    ProgressBar progressBar = null;

    RelativeLayout compileDialogLayout = null;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection compileServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected()");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            InstrumentationService.InstrumentationServiceBinder binder =
                    (InstrumentationService.InstrumentationServiceBinder) service;
            compileService = binder.getService();
            boundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected()");
        }
    };

    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                if (action.equals(ProgressPublisher.ACTION_INSTRUMENTATION_STATUS_UPDATE)) {
                    int progress = intent
                            .getIntExtra(ProgressPublisher.EXTRA_INSTRUMENTATION_STATUS_PROGRESS, -1);
                    String message = intent
                            .getStringExtra(ProgressPublisher.EXTRA_INSTRUMENTATION_STATUS_MESSAGE);

                    if (progress != -1) {
                        updateProgress(progress, message);
                    } else {
                        updateProgressVerbose(progress, message);
                    }
                } else if (action.equals(ProgressPublisher.ACTION_INSTRUMENTATION_RESULT)) {
                    finish();
                }
            }
        }
    };


    private void updateProgress(int progress, String message) {
        Log.d(TAG, "reportProgressStage() " + progress + ": " + message);
        setCompilationProgress(progress);
        if (progress >= 0) {
            this.compileStatus.setText(progress + ": " + message);
        } else {
            this.compileStatus.setText(message);
        }
        this.compileStatusExtended.append(message + "\n");
    }

    private void updateProgressVerbose(int progress, String message) {
        Log.d(TAG, "updateProgressVerbose() " + progress + ": " + message);
        setCompilationProgress(progress);
        this.progressBar.setProgress(progress);
        this.compileStatusExtended.append("> " + message + "\n");
    }

    private void setCompilationProgress(int progress) {
        if (progress < 0) {
            return;
        }
        if (progress <= this.progressBar.getMax()) {
            this.progressBar.setProgress(progress);
        } else {
            this.progressBar.setProgress(this.progressBar.getMax());
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate() savedInstanceState: " + savedInstanceState.toString());
        }

        setContentView(R.layout.dialog_compilation);
        this.compileDialogLayout = (RelativeLayout) findViewById(R.id.compile_dialog_layout);
        this.progressBar = (ProgressBar) findViewById(R.id.compile_progress);
        this.progressBar.setIndeterminate(false);

        setupButtons();
        setupTextViews();
        setDefaultTitle();

        setupBroadcastReceiver();
    }

    private void setDefaultTitle() {
        Intent intent = getIntent();
        if (intent != null) {
            packageName = intent.getStringExtra(InstrumentationService.INTENT_KEY_APP_NAME);
        }
        if (packageName != null && !packageName.isEmpty()) {
            setTitle(packageName);
        } else {
            setTitle("Artist Compilation");
        }
    }

    private void setupBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ProgressPublisher.ACTION_INSTRUMENTATION_STATUS_UPDATE);
        intentFilter.addAction(ProgressPublisher.ACTION_INSTRUMENTATION_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mResultReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart()");
        if (!boundToService) {
            Intent intent = new Intent(this, InstrumentationService.class);
            bindService(intent, compileServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop()");
        if (boundToService) {
            unbindService(compileServiceConnection);
            boundToService = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent(Intent intent)");
        setIntent(intent);
        setDefaultTitle();
    }

    private void setupButtons() {
        final Button buttonVerbose = (Button) findViewById(R.id.button_compile_verbose);
        buttonVerbose.setOnClickListener(onClickListener -> {
            if (this.compileStatusExtended.isShown()) {
                this.compileStatusExtended.setVisibility(View.GONE);
                this.compileDialogLayout.setVisibility(View.GONE);
                this.compileDialogLayout.setVisibility(View.VISIBLE);
            } else {
                this.compileStatusExtended.setVisibility(View.VISIBLE);
                this.compileDialogLayout.setVisibility(View.GONE);
                this.compileDialogLayout.setVisibility(View.VISIBLE);
            }

            if (boundToService) {
                Log.d(TAG, "onClick() Service Connected");
            } else {
                Log.d(TAG, "onClick() NOT BOUND");
            }
        });

        final Button buttonCancel = (Button) findViewById(R.id.button_compile_cancel);
        buttonCancel.setOnClickListener(onClickListener -> {
            Log.d(TAG, "Button: Cancel()");
            if (boundToService
                    && compileService != null
                    && packageName != null) {
                compileService.cancel();
            }
            Log.d(TAG, "Button: Cancel() DONE_ finish()");
            finish();
        });
    }

    private void setupTextViews() {
        this.compileStatus = (TextView) findViewById(R.id.compile_status);
        this.compileStatusExtended = (TextView) findViewById(R.id.compile_status_extended);
        this.compileStatusExtended.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        LogA.setUserLogLevel(getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState(Bundle outState)");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d(TAG, "onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, String.format(Locale.getDefault(), "MotionEven: [%s] [Action: %d] [Masked: %d]",
                event.toString(),
                event.getAction(),
                event.getActionMasked()));
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            // finish();
            Log.d(TAG, "Outside Touch: ACTION_OUTSIDE");
            return true;
        }
        // Delegate everything else to Activity.
        return true;
    }

    public static void compile(final Context context, final String packageName) {
        startCompilationService(context, packageName);
        final Intent intent = new Intent(context, CompileDialogActivity.class);
        intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, packageName);
        context.startActivity(intent);
        Log.i(TAG, String.format("LaunchActivity: %s for Artist App: %s",
                CompileDialogActivity.class.toString(), packageName));
    }

    private static void startCompilationService(final Context context, final String packageName) {
        final Intent intent = new Intent(context, InstrumentationService.class);
        intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, packageName);
        context.startService(intent);
    }
}
