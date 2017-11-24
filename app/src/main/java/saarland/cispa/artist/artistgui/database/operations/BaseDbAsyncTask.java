package saarland.cispa.artist.artistgui.database.operations;

import android.os.AsyncTask;

import saarland.cispa.artist.artistgui.database.AppDatabase;

abstract class BaseDbAsyncTask<Params> extends AsyncTask<Params, Void, Void> {
    AppDatabase mDatabase;

    BaseDbAsyncTask(AppDatabase database) {
        this.mDatabase = database;
    }
}
