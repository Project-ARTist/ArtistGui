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

package saarland.cispa.artist.artistgui.appdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher;
import saarland.cispa.artist.artistgui.progress.ProgressDialogFragment;
import saarland.cispa.artist.artistgui.settings.db.operations.PersistPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManagerImpl;
import saarland.cispa.artist.artistgui.utils.GuiUtils;

public class AppDetailsDialog extends DialogFragment implements AppDetailsDialogContract.View {

    public static final String TAG = "AppDetailsDialog";
    public static final String PACKAGE_KEY = "package";

    private AppDetailsDialogContract.Presenter mPresenter;
    private View mRootView;

    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null &&
                    ProgressPublisher.ACTION_INSTRUMENTATION_RESULT.equals(intent.getAction())) {
                // TODO package name check
                boolean isSuccess = intent
                        .getBooleanExtra(ProgressPublisher.EXTRA_INSTRUMENTATION_RESULT, false);
                mPresenter.handleInstrumentationResult(isSuccess);
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(ProgressPublisher
                .ACTION_INSTRUMENTATION_RESULT);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mResultReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mResultReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new AppDetailsDialogPresenter(this, getActivity(),
                new SettingsManagerImpl(getContext()));

        Bundle bundle = getArguments();
        if (bundle != null) {
            Package selectedPackage = bundle.getParcelable(PACKAGE_KEY);
            mPresenter.setSelectedPackage(selectedPackage);
            mPresenter.loadAppIcon();
            mPresenter.determineInstrumentationStatusAndUpdateViews();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_app_details, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            Package selectedPackage = bundle.getParcelable(PACKAGE_KEY);
            setUpTextViews(selectedPackage);
        }
        return mRootView;
    }

    @Override
    public void setPresenter(AppDetailsDialogContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void setUpTextViews(Package app) {
        TextView appNameView = mRootView.findViewById(R.id.app_name);
        TextView packageNameView = mRootView.findViewById(R.id.package_name);

        appNameView.setText(app.getAppName());
        packageNameView.setText(app.getPackageName());
    }

    @Override
    public void onAppIconLoaded(Drawable appIcon) {
        ImageView appIconView = mRootView.findViewById(R.id.app_icon);
        appIconView.setImageDrawable(appIcon);
    }

    @Override
    public void updateLastInstrumentationTextView(String lastInstrumentedString) {
        TextView lastInstrumentationView = mRootView.findViewById(R.id.last_instrumentation);
        lastInstrumentationView.setText(lastInstrumentedString);
    }

    @Override
    public void activateKeepInstrumentedViews(Package app) {
        Switch keepInstrumentedSwitch = mRootView
                .findViewById(R.id.keep_instrumented_switch);
        keepInstrumentedSwitch.setChecked(app.isKeepInstrumented());
        keepInstrumentedSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            app.setKeepInstrumented(isChecked);
            new PersistPackageToDbAsyncTask(getContext())
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, app);
        });
        keepInstrumentedSwitch.setEnabled(true);

        TextView summary = mRootView
                .findViewById(R.id.keep_instrumented_switch_info_text);
        summary.setOnClickListener((l) -> keepInstrumentedSwitch.toggle());
    }

    @Override
    public void updateInstrumentationButton(boolean instrumented, String packageName) {
        Button instrumentButton = mRootView.findViewById(R.id.instrument_button);
        instrumentButton.setText(instrumented ? getString(R.string.reinstrument_app) :
                getString(R.string.instrument_app));
        instrumentButton.setOnClickListener(v -> mPresenter.instrumentApp());
    }

    @Override
    public void showInstrumentationProgress() {
        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        dialogFragment.setCancelable(false);
        dialogFragment.show(getFragmentManager(), ProgressDialogFragment.TAG);
    }

    @Override
    public void showInstrumentationResult(boolean isSuccess, String packageName) {
        int stringResourceId = isSuccess ? R.string.snack_compilation_success :
                R.string.snack_compilation_failed;
        String userMessage = getResources().getString(stringResourceId) + packageName;
        GuiUtils.displaySnackLong(mRootView, userMessage);
    }
}
