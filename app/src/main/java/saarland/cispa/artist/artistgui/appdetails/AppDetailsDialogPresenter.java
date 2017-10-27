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
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;
import trikita.log.Log;

public class AppDetailsDialogPresenter implements AppDetailsDialogContract.Presenter {

    public static final String TAG = "AppDetailsDialogPresenter";

    private AppDetailsDialogContract.View mView;
    private Activity mActivity;

    public AppDetailsDialogPresenter(AppDetailsDialogContract.View view, Activity activity) {
        this.mView = view;
        this.mActivity = activity;
        view.setPresenter(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void loadAppIcon(Package app) {
        try {
            Context mdpiContext = mActivity.createPackageContext(app.getPackageName(),
                    Context.CONTEXT_IGNORE_SECURITY);
            Drawable appIcon = mdpiContext.getResources().getDrawableForDensity(app.getAppIconId(),
                    DisplayMetrics.DENSITY_XHIGH, null);
            mView.onAppIconLoaded(appIcon);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void determineInstrumentationStatusAndUpdateViews(Package app) {
        long timestamp = app.getLastInstrumentationTimestamp();
        boolean isInstrumented = timestamp != 0;

        String lastInstrumentationText;
        if (isInstrumented) {
            Date date = new Date(timestamp);
            String formattedData = DateFormat
                    .getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(date);

            String prefix = mActivity.getString(R.string.last_instrumentation);
            lastInstrumentationText = String.format(prefix, formattedData);

            mView.activateKeepInstrumentedViews(app);
        } else {
            lastInstrumentationText = mActivity.getString(R.string.never_instrumented);
        }

        mView.updateLastInstrumentationTextView(lastInstrumentationText);
        mView.updateInstrumentationButton(isInstrumented, app.getPackageName());
    }

    @Override
    public void instrumentApp(String packageName) {
        Log.d(TAG, "compileInstalledApp(): " + packageName);
        Intent intent = new Intent(mActivity, InstrumentationService.class);
        intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, packageName);
        mActivity.startService(intent);
        mView.showInstrumentationProgress();
    }
}
