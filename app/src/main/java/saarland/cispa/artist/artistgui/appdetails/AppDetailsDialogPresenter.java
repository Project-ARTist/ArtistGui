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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import saarland.cispa.artist.artistgui.MainActivityPresenter;
import saarland.cispa.artist.artistgui.Application;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.database.AppDatabase;
import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.database.Package;
import saarland.cispa.artist.artistgui.database.operations.AddInstrumentedPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.database.operations.PersistPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.database.operations.RemovePackagesFromDbAsyncTask;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;
import saarland.cispa.artist.artistgui.instrumentation.RemoveInstrumentationAsyncTask;
import saarland.cispa.artist.artistgui.instrumentation.config.ArtistRunConfig;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher;
import saarland.cispa.artist.artistgui.settings.config.ArtistConfigFactory;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;
import trikita.log.Log;

public class AppDetailsDialogPresenter implements AppDetailsDialogContract.Presenter {

    public static final String TAG = "AppDetailsDialogPresenter";

    private final AppDetailsDialogContract.View mView;
    private final Context mContext;
    private final SettingsManager mSettingsManager;
    private final AppDatabase mDatabase;

    private Package mSelectedPackage;

    public AppDetailsDialogPresenter(AppDetailsDialogContract.View view, Context context,
                                     SettingsManager settingsManager) {
        mView = view;
        mContext = context;
        mSettingsManager = settingsManager;
        mDatabase = ((Application) context.getApplicationContext()).getDatabase();
        view.setPresenter(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void loadAppIcon() {
        try {
            Context mdpiContext = mContext.createPackageContext(mSelectedPackage.packageName,
                    Context.CONTEXT_IGNORE_SECURITY);
            Drawable appIcon = mdpiContext.getResources()
                    .getDrawableForDensity(mSelectedPackage.appIconId,
                            DisplayMetrics.DENSITY_XHIGH, null);
            mView.setAppIcon(appIcon);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void determineInstrumentationStatusAndUpdateViews() {
        String dateAndTime = null;

        boolean isDex2OatPresent = MainActivityPresenter.isDeviceCompatible(mContext);
        if (!isDex2OatPresent) {
            mView.onDeviceNotCompatible();
        } else {
            long timestamp = mSelectedPackage.lastInstrumentationTimestamp;
            boolean isInstrumented = timestamp != 0;

            if (isInstrumented) {
                dateAndTime = convertTimestampToDateAndTime(timestamp);
                mView.updateKeepInstrumentedViews(true, mSelectedPackage);
            }

            mView.updateInstrumentationButton(isInstrumented);
            mView.updateRemoveInstrumentationButton(isInstrumented);
        }

        mView.setLastInstrumentationText(dateAndTime);
    }

    private String convertTimestampToDateAndTime(long timestamp) {
        Date date = new Date(timestamp);
        return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(date);
    }

    @Override
    public void startInstrumentation(List<Module> modules) {
        int selectedModulesCount = modules.size();
        if (selectedModulesCount == 0) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.no_module_selected),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mView.showInstrumentationProgress();
        String[] modulePackageNames = new String[selectedModulesCount];
        for (int i = 0; i < selectedModulesCount; i++) {
            modulePackageNames[i] = modules.get(i).packageName;
        }

        Intent intent = new Intent(mContext, InstrumentationService.class);
        intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME,
                mSelectedPackage.packageName);
        intent.putExtra(InstrumentationService.INTENT_KEY_MODULE_PACKAGE_NAMES,
                modulePackageNames);
        mContext.startService(intent);
    }

    @Override
    public void removeInstrumentation() {
        startRemovalTasks();
        mSelectedPackage.reset();

        mView.updateKeepInstrumentedViews(false, mSelectedPackage);
        mView.setLastInstrumentationText(null);
        mView.updateRemoveInstrumentationButton(false);
        mView.updateInstrumentationButton(false);

        Intent intent = new Intent(ProgressPublisher.ACTION_INSTRUMENTATION_REMOVED);
        intent.putExtra(ProgressPublisher.EXTRA_PACKAGE_NAME, mSelectedPackage.packageName);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void startRemovalTasks() {
        String packageName = mSelectedPackage.packageName;
        ArtistRunConfig runConfig = ArtistConfigFactory
                .buildArtistRunConfig(mContext, packageName);
        new RemoveInstrumentationAsyncTask()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, runConfig.app_oat_file_path);
        new RemovePackagesFromDbAsyncTask(mDatabase)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, packageName);
    }

    @Override
    public void handleInstrumentationResult(boolean isSuccess) {
        if (isSuccess) {
            boolean wasInstrumented = mSelectedPackage.lastInstrumentationTimestamp != 0;

            mSelectedPackage.lastInstrumentationTimestamp = System.currentTimeMillis();
            String dateAndTime = convertTimestampToDateAndTime(mSelectedPackage
                    .lastInstrumentationTimestamp);
            mView.setLastInstrumentationText(dateAndTime);

            mView.updateKeepInstrumentedViews(true, mSelectedPackage);
            mView.updateRemoveInstrumentationButton(true);

            if (!wasInstrumented) {
                new AddInstrumentedPackageToDbAsyncTask(mDatabase).execute(mSelectedPackage);
            } else {
                new PersistPackageToDbAsyncTask(mDatabase).execute(mSelectedPackage);
            }
            startInstrumentedAppIfWished();
        }
    }

    private void startInstrumentedAppIfWished() {
        final boolean launchActivity = mSettingsManager.shouldLaunchActivityAfterCompilation();
        if (launchActivity) {
            String packageName = mSelectedPackage.packageName;
            Log.d(TAG, "Starting compiled app: " + packageName);
            final Intent launchIntent = mContext.getPackageManager()
                    .getLaunchIntentForPackage(packageName);
            mContext.startActivity(launchIntent);
        }
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putParcelable(AppDetailsDialog.PACKAGE_KEY, mSelectedPackage);
    }

    @Override
    public void setSelectedPackage(Package selectedPackage) {
        this.mSelectedPackage = selectedPackage;
    }
}
