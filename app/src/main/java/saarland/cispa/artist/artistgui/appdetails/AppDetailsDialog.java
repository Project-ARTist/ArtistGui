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

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.progress.ProgressDialogFragment;
import saarland.cispa.artist.artistgui.settings.db.operations.PersistPackageToDbAsyncTask;

public class AppDetailsDialog extends DialogFragment implements AppDetailsDialogContract.View {

    public static final String TAG = "AppDetailsDialog";
    public static final String PACKAGE_KEY = "package";

    private AppDetailsDialogContract.Presenter mPresenter;
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_app_details, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            Package selectedPackage = bundle.getParcelable(PACKAGE_KEY);
            setUpTextViews(selectedPackage);
            mPresenter.loadAppIcon(selectedPackage);
            mPresenter.determineInstrumentationStatusAndUpdateViews(selectedPackage);
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
        instrumentButton.setOnClickListener(v -> mPresenter.instrumentApp(packageName));
    }

    @Override
    public void showInstrumentationProgress() {
        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        dialogFragment.setCancelable(false);
        dialogFragment.show(getFragmentManager(), ProgressDialogFragment.TAG);
    }
}
