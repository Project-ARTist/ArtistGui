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
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import saarland.cispa.artist.artistgui.Application;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.database.Package;
import saarland.cispa.artist.artistgui.database.operations.PersistPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher;
import saarland.cispa.artist.artistgui.modules.loader.ModuleListLoader;
import saarland.cispa.artist.artistgui.progress.ProgressDialogFragment;

public class AppDetailsDialog extends DialogFragment implements AppDetailsDialogContract.View,
        LoaderManager.LoaderCallbacks<List<Module>> {

    public static final String TAG = "AppDetailsDialog";
    public static final String PACKAGE_KEY = "package";

    private static final int LOADER_ID = 459323415;

    private AppDetailsDialogContract.Presenter mPresenter;
    private View mRootView;

    private TextView mLastInstrumentationView;
    private Switch mKeepInstrumentedSwitch;
    private Button mInstrumentationButton;
    private Button mRemoveInstrumentationButton;

    private RecyclerView mModuleSelectionView;
    private ModuleSelectionAdapter mAdapter;

    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null &&
                    ProgressPublisher.ACTION_INSTRUMENTATION_RESULT.equals(intent.getAction())) {
                boolean isSuccess = intent
                        .getBooleanExtra(ProgressPublisher.EXTRA_INSTRUMENTATION_RESULT, false);
                mPresenter.handleInstrumentationResult(isSuccess);
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_app_details, container, false);
        mLastInstrumentationView = mRootView.findViewById(R.id.last_instrumentation);
        mKeepInstrumentedSwitch = mRootView.findViewById(R.id.keep_instrumented_switch);

        // Module selection recycler view setup
        mModuleSelectionView = mRootView.findViewById(R.id.module_selection_list);
        mModuleSelectionView.setHasFixedSize(true);
        final Context context = getContext();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        mModuleSelectionView.setLayoutManager(mLayoutManager);
        mAdapter = new ModuleSelectionAdapter();
        mModuleSelectionView.setAdapter(mAdapter);

        mInstrumentationButton = mRootView.findViewById(R.id.instrument_button);
        mInstrumentationButton.setOnClickListener(v -> {
            List<Module> selectedModules = mAdapter.getSelectedModules();
            mPresenter.startInstrumentation(selectedModules);
        });

        mRemoveInstrumentationButton = mRootView.findViewById(R.id.remove_instrumentation_button);
        mRemoveInstrumentationButton.setOnClickListener(v -> mPresenter.removeInstrumentation());

        // restore after configuration change
        Package selectedPackage = null;
        if (savedInstanceState != null) {
            selectedPackage = savedInstanceState.getParcelable(PACKAGE_KEY);
        } else {
            // new instance
            Bundle bundle = getArguments();
            if (bundle != null) {
                selectedPackage = bundle.getParcelable(PACKAGE_KEY);
            }
        }

        if (selectedPackage != null) {
            mPresenter.setSelectedPackage(selectedPackage);
            initializeAppAndPackageName(selectedPackage);
            mPresenter.loadAppIcon();
            mPresenter.determineInstrumentationStatusAndUpdateViews();
        }

        return mRootView;
    }

    private void initializeAppAndPackageName(Package selectedPackage) {
        TextView appNameView = mRootView.findViewById(R.id.app_name);
        TextView packageNameView = mRootView.findViewById(R.id.package_name);

        appNameView.setText(selectedPackage.appName);
        packageNameView.setText(selectedPackage.packageName);
    }

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenter.saveInstanceState(outState);
    }

    @Override
    public Loader<List<Module>> onCreateLoader(int id, Bundle args) {
        return new ModuleListLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<Module>> loader, List<Module> data) {
        // Set the new data in the adapter.
        mAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Module>> loader) {
        mAdapter.setData(null);
    }

    @Override
    public void setPresenter(AppDetailsDialogContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void setAppIcon(Drawable appIcon) {
        ImageView appIconView = mRootView.findViewById(R.id.app_icon);
        appIconView.setImageDrawable(appIcon);
    }

    @Override
    public void setLastInstrumentationText(@Nullable String dateAndTime) {
        String textToShow;
        if (dateAndTime != null) {
            textToShow = String.format(getString(R.string.last_instrumentation), dateAndTime);
        } else {
            textToShow = getString(R.string.never_instrumented);
        }
        mLastInstrumentationView.setText(textToShow);
    }

    @Override
    public void updateKeepInstrumentedViews(boolean enable, Package app) {
        mKeepInstrumentedSwitch.setChecked(app.keepInstrumented);
        Application appContext = (Application) getContext().getApplicationContext();
        mKeepInstrumentedSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            app.keepInstrumented = isChecked;
            new PersistPackageToDbAsyncTask(appContext.getDatabase())
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, app);
        });
        mKeepInstrumentedSwitch.setEnabled(enable);

        TextView summary = mRootView
                .findViewById(R.id.keep_instrumented_switch_info_text);
        summary.setOnClickListener((l) -> mKeepInstrumentedSwitch.toggle());
    }

    @Override
    public void updateInstrumentationButton(boolean isInstrumented) {
        mInstrumentationButton.setText(isInstrumented ? getString(R.string.reinstrument_app) :
                getString(R.string.instrument_app));
    }

    @Override
    public void updateRemoveInstrumentationButton(boolean isInstrumented) {
        mRemoveInstrumentationButton.setVisibility(isInstrumented ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDeviceNotCompatible() {
        mInstrumentationButton.setEnabled(false);
        mInstrumentationButton.setText(getString(R.string.instrumentation_not_possible));
    }

    @Override
    public void showInstrumentationProgress() {
        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
        dialogFragment.setCancelable(false);
        dialogFragment.show(getFragmentManager(), ProgressDialogFragment.TAG);
    }
}
