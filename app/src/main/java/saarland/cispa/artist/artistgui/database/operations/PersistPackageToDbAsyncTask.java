package saarland.cispa.artist.artistgui.database.operations;

import android.support.annotation.NonNull;

import saarland.cispa.artist.artistgui.database.AppDatabase;
import saarland.cispa.artist.artistgui.database.Package;

public class PersistPackageToDbAsyncTask extends BaseDbAsyncTask<Package> {

    public PersistPackageToDbAsyncTask(AppDatabase database) {
        super(database);
    }

    @Override
    protected Void doInBackground(@NonNull Package... params) {
        mDatabase.packageDao().update(params);
        return null;
    }
}
