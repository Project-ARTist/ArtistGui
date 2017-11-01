package saarland.cispa.artist.artistgui.broadcastreceiver;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.List;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.instrumentation.InstrumentationService;
import saarland.cispa.artist.artistgui.settings.db.DatabaseManager;
import saarland.cispa.artist.artistgui.settings.db.InstrumentedPackagesManager;

public class ReinstrumentAppAsyncTask extends AsyncTask<String, Void, Void> {

    private Context mContext;

    public ReinstrumentAppAsyncTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        InstrumentedPackagesManager manager = new DatabaseManager(mContext);
        for (String p : params) {
            List<Package> instrumentedPackages = manager.getAllInstrumentedApps();
            for (Package app : instrumentedPackages) {
                if (app.getPackageName().equals(p) && app.isKeepInstrumented()) {
                    Intent intent = new Intent(mContext, InstrumentationService.class);
                    intent.putExtra(InstrumentationService.INTENT_KEY_APP_NAME, p);
                    mContext.startService(intent);
                }
            }
        }
        manager.onDestroy();
        return null;
    }
}
