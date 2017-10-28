package saarland.cispa.artist.artistgui.appdetails;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.text.DateFormat;
import java.util.Date;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;
import saarland.cispa.artist.artistgui.instrumentation.progress.ProgressPublisher;
import trikita.log.Log;

public class AppDetailsDialogPresenter implements AppDetailsDialogContract.Presenter {

    public static final String TAG = "AppDetailsDialogPresenter";

    private AppDetailsDialogContract.View mView;
    private Activity mActivity;
    private Package mSelectedPackage;

    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ProgressPublisher.ACTION_INSTRUMENTATION_RESULT.equals(action)) {
                String packageName = intent.getStringExtra(ProgressPublisher.EXTRA_PACKAGE_NAME);
                if (packageName.equals(mSelectedPackage.getPackageName())) {

                }
            }
        }
    };

    AppDetailsDialogPresenter(AppDetailsDialogContract.View view, Activity activity) {
        this.mView = view;
        this.mActivity = activity;
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
            mView.onAppIconLoaded(appIcon);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void determineInstrumentationStatusAndUpdateViews() {
        long timestamp = mSelectedPackage.getLastInstrumentationTimestamp();
        boolean isInstrumented = timestamp != 0;

        String lastInstrumentationText;
        if (isInstrumented) {
            Date date = new Date(timestamp);
            String formattedData = DateFormat
                    .getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(date);

            String prefix = mActivity.getString(R.string.last_instrumentation);
            lastInstrumentationText = String.format(prefix, formattedData);

            mView.activateKeepInstrumentedViews(mSelectedPackage);
        } else {
            lastInstrumentationText = mActivity.getString(R.string.never_instrumented);
        }

        mView.updateLastInstrumentationTextView(lastInstrumentationText);
        mView.updateInstrumentationButton(isInstrumented, mSelectedPackage.getPackageName());
    }

    @Override
    public void instrumentApp() {
        String packageName = mSelectedPackage.getPackageName();
        Log.d(TAG, "compileInstalledApp(): " + packageName);
        Intent intent = new Intent(mActivity, InstrumentationService.class);
        intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, packageName);
        mActivity.startService(intent);
        mView.showInstrumentationProgress();
    }

    @Override
    public void setSelectedPackage(Package selectedPackage) {
        this.mSelectedPackage = selectedPackage;
    }
}
