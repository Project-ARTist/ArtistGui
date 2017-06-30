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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;

import saarland.cispa.artist.ArtistImpl;
import saarland.cispa.artist.CompilationResultReceiver;
import saarland.cispa.artist.artistgui.gui.CompileNotification;
import saarland.cispa.artist.gui.artist.ArtistGuiProgress;
import saarland.cispa.artist.log.Logg;
import trikita.log.Log;

public class CompileDialogActivity
        extends Activity
        implements CompilationResultReceiver.Receiver, ArtistGuiProgress {

    private static final String TAG = "CompileDialogActivity";

    public static final int COMPILE_DIALOG_ID = 555;

    public CompilationResultReceiver mReceiver = null;

    private int activityResult = RESULT_CANCELED;

    private String APP_NAME = null;

    CompilationService compileService;
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
            CompilationService.CompilationServiceBinder binder =
                    (CompilationService.CompilationServiceBinder) service;
            compileService = binder.getService();
            boundToService = true;

            if (APP_NAME != null) {
                setDefaultTitle();
                startCompilation();
            } else {
                reconnectGuiToService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected()");
            boundToService = false;
        }
    };

    private void startCompilation() {
        compileService.compileInstalledApp(mReceiver, APP_NAME);
        compileService.registerArtistGuiProgress(CompileDialogActivity.this);
    }

    private void closeActivity() {
        Log.i(TAG, "CompileDialogActivity closing, nothing is compiling");
        CompileDialogActivity.this.finish();
        CompileNotification notification =
                new CompileNotification(getApplicationContext());
        notification.kill("");
    }

    private void reconnectGuiToService() {
        Log.d(TAG, "reconnectGuiToService()");
        final String appName = compileService.reconnectGuiToService(this, mReceiver);
        final String lastStatusMessage = compileService.getLastStatusMessage();
        setTitle(appName);
        updateProgress(-1, lastStatusMessage);
        if (appName == null || appName.isEmpty()) {
            Log.d(TAG, "reconnectGuiToService() => closeActivity()");
            closeActivity();
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String appPackageName = "";
        if (resultData != null) {
            appPackageName = resultData.getString(ArtistImpl.INTENT_EXTRA_APP_NAME);
        }
        Log.d(TAG, String.format(Locale.getDefault(),
                "CompileDialogActivity.onReceiveResult() resultcode[%d] APP[%s]",
                resultCode, appPackageName));

        if (appPackageName != null && !appPackageName.isEmpty()) {
            compileService.compilationCleanup(appPackageName);
        } else {
            compileService.compilationCleanup(APP_NAME);
        }
        switch (resultCode) {
            case ArtistImpl.COMPILATION_CANCELED:
            case ArtistImpl.COMPILATION_ERROR:
                activityResult = RESULT_CANCELED;
                setActivityResult();
                finish();
                break;
            case ArtistImpl.COMPILATION_SUCCESS:
                activityResult = RESULT_OK;
                setActivityResult();
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void updateProgress(int progress, String message) {
        Log.d(TAG, "updateProgress() " + progress + ": " + message);
        runOnUiThread(() -> {
            setCompilationProgress(progress);
            if (progress >= 0 ) {
                this.compileStatus.setText(progress + ": " + message);
            } else {
                this.compileStatus.setText(message);
            }
            this.compileStatusExtended.append(message + "\n");
        });
    }

    @Override
    public void updateProgressVerbose(int progress, String message) {
        Log.d(TAG, "updateProgressVerbose() " + progress + ": " + message);
        runOnUiThread(() -> {
            setCompilationProgress(progress);
            this.progressBar.setProgress(progress);
            this.compileStatusExtended.append("> " + message + "\n");
        });
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
    public void kill(final String message) {
        Log.d(TAG, "kill() " + message);
        runOnUiThread(() -> {
            this.compileStatus.setText(message);
            this.compileStatusExtended.setText(message);
        });
    }

    @Override
    public void doneSuccess(final String message) {
        Log.d(TAG, "doneSuccess() " + message);
        runOnUiThread(() -> {
            setCompilationProgress(100);
            this.compileStatus.setText(message);
            this.compileStatusExtended.setText(message);
        });
    }

    @Override
    public void doneFailed(final String message) {
        Log.d(TAG, "doneFailed() " + message);
        runOnUiThread(() -> {
            this.compileStatus.setText(message);
            this.compileStatusExtended.setText(message);
        });
    }

    @Override
    public void done() {
        Log.d(TAG, "done()");
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate() savedInstanceState: " + savedInstanceState.toString());
        }

        this.mReceiver = new CompilationResultReceiver(new Handler());
        this.mReceiver.setReceiver(this);

        final Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            this.APP_NAME = bundle.getString(CompilationService.EXTRA_APP_NAME);
            Log.d(TAG, "onCreate() Bundle APP_NAME: " + this.APP_NAME);
        }
        setContentView(R.layout.activity_dialog_compiler);

        this.compileDialogLayout = (RelativeLayout) findViewById(R.id.compile_dialog_layout);
        this.progressBar = (ProgressBar) findViewById(R.id.compile_progress);
        this.progressBar.setIndeterminate(false);

        setupButtons();
        setupTextViews();
        setDefaultTitle();
    }

    private void setDefaultTitle() {
        if (APP_NAME != null && !Objects.equals(APP_NAME, "")) {
            setTitle(APP_NAME);
        } else {
            setTitle("Artist Compilation");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent(Intent intent)");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState(savedInstanceState)");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        Log.d(TAG, "onRestoreInstanceState(savedInstanceState, PersistableBundle persistentState)");
    }

    private void setupButtons() {
        final Button buttonVerbose = (Button)findViewById(R.id.button_compile_verbose);
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

        final Button buttonCancel = (Button)findViewById(R.id.button_compile_cancel);
        buttonCancel.setOnClickListener(onClickListener -> {
            Log.d(TAG, "Button: Cancel()");
            if (boundToService
                    && compileService != null
                    && APP_NAME != null) {
                compileService.cancelCompilation(APP_NAME);
            }
            Log.d(TAG, "Button: Cancel() DONE_ finish()");
        });
    }

    private void setupTextViews() {
        this.compileStatus = (TextView)findViewById(R.id.compile_status);
        this.compileStatusExtended = (TextView)findViewById(R.id.compile_status_extended);
        this.compileStatusExtended.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        Intent intent = new Intent(this, CompilationService.class);
        CompilationService.startService(getApplicationContext(), null);
        bindService(intent, compileServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        Logg.setUserLogLevel(getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if (boundToService) {
            unbindService(compileServiceConnection);
        }
    }

    private void setActivityResult() {
        final Intent resultData = new Intent();
        resultData.putExtra(ArtistImpl.INTENT_EXTRA_APP_NAME, APP_NAME);
        if (getParent() == null) {
            setResult(activityResult, resultData);
        } else {
            getParent().setResult(activityResult, resultData);
        }
        Log.d(TAG,"setActivityResult() : " + activityResult);
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

    public static void compile(final Activity activity, final String apkPackageName) {
        final Intent intent = new Intent(activity, CompileDialogActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putString(CompilationService.EXTRA_APP_NAME, apkPackageName);
        intent.putExtras(bundle); //Put your id to your next Intent
        Log.i(TAG, String.format("LaunchActivity: %s for Artist App: %s",
                CompileDialogActivity.class.toString(), apkPackageName));
        activity.startActivityForResult(intent, COMPILE_DIALOG_ID);
    }
}
