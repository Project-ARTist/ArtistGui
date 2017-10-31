package saarland.cispa.artist.artistgui.appdetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.text.DateFormat;
import java.util.Date;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;
import saarland.cispa.artist.artistgui.settings.db.operations.AddInstrumentedPackageToDbAsyncTask;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;
import trikita.log.Log;

public class AppDetailsDialogPresenter implements AppDetailsDialogContract.Presenter {

    public static final String TAG = "AppDetailsDialogPresenter";


    private final AppDetailsDialogContract.View mView;
    private final Activity mActivity;
    private final SettingsManager mSettingsManager;

    private Package mSelectedPackage;

    AppDetailsDialogPresenter(AppDetailsDialogContract.View view, Activity activity,
                              SettingsManager settingsManager) {
        mView = view;
        mActivity = activity;
        mSettingsManager = settingsManager;
        view.setPresenter(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void loadAppIcon() {
        try {
            Context mdpiContext = mActivity.createPackageContext(mSelectedPackage.getPackageName(),
                    Context.CONTEXT_IGNORE_SECURITY);
            Drawable appIcon = mdpiContext.getResources()
                    .getDrawableForDensity(mSelectedPackage.getAppIconId(),
                            DisplayMetrics.DENSITY_XHIGH, null);
            mView.setAppIcon(appIcon);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void determineInstrumentationStatusAndUpdateViews() {
        long timestamp = mSelectedPackage.getLastInstrumentationTimestamp();
        boolean isInstrumented = timestamp != 0;

        String dateAndTime = null;
        if (isInstrumented) {
            dateAndTime = convertTimestampToDateAndTime(timestamp);
            mView.activateKeepInstrumentedViews(mSelectedPackage);
        }

        mView.setLastInstrumentationText(dateAndTime);
        mView.updateInstrumentationButton(isInstrumented, mSelectedPackage.getPackageName());
    }

    private String convertTimestampToDateAndTime(long timestamp) {
        Date date = new Date(timestamp);
        return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(date);
    }

    @Override
    public void handleInstrumentationResult(boolean isSuccess) {
        if (isSuccess) {
            mSelectedPackage.updateLastInstrumentationTimestamp();

            long timestamp = mSelectedPackage.getLastInstrumentationTimestamp();
            String dateAndTime = convertTimestampToDateAndTime(timestamp);
            mView.setLastInstrumentationText(dateAndTime);

            mView.activateKeepInstrumentedViews(mSelectedPackage);

            new AddInstrumentedPackageToDbAsyncTask(mActivity).execute(mSelectedPackage);
            startInstrumentedAppIfWished();
        }
    }

    @Override
    public void startInstrumentation() {
        Intent intent = new Intent(mActivity, InstrumentationService.class);
        intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME,
                mSelectedPackage.getPackageName());
        mActivity.startService(intent);
    }

    private void startInstrumentedAppIfWished() {
        final boolean launchActivity = mSettingsManager.shouldLaunchActivityAfterCompilation();
        if (launchActivity) {
            String packageName = mSelectedPackage.getPackageName();
            Log.d(TAG, "Starting compiled app: " + packageName);
            final Intent launchIntent = mActivity.getPackageManager()
                    .getLaunchIntentForPackage(packageName);
            mActivity.startActivity(launchIntent);
        }
    }

    @Override
    public void setSelectedPackage(Package selectedPackage) {
        this.mSelectedPackage = selectedPackage;
    }
}
