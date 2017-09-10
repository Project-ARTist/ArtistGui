package saarland.cispa.artist.artistgui.settings.db.operations;

import android.content.Context;
import android.os.AsyncTask;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.settings.db.DatabaseManager;
import saarland.cispa.artist.artistgui.settings.db.InstrumentedPackagesManager;

public class PersistPackageToDbAsyncTask extends AsyncTask<Package, Void, Void> {

    private Context mContext;

    public PersistPackageToDbAsyncTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Package... params) {
        InstrumentedPackagesManager manager = new DatabaseManager(mContext);
        for (Package p : params) {
            manager.persistPackage(p);
        }
        manager.onDestroy();
        return null;
    }
}
