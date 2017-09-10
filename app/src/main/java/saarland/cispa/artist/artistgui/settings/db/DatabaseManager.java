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

package saarland.cispa.artist.artistgui.settings.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.Package;

import static saarland.cispa.artist.artistgui.settings.db.DbContract.PackageEntry;

public class DatabaseManager implements InstrumentedPackagesManager {

    private static String[] sInstrumentedAppsProjection = {
            PackageEntry.COLUMN_NAME_PACKAGE_NAME,
            PackageEntry.COLUMN_NAME_TIMESTAMP,
            PackageEntry.COLUMN_NAME_KEEP_INSTRUMENTED
    };

    private SQLiteDbHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public DatabaseManager(Context context) {
        mDatabaseHelper = new SQLiteDbHelper(context);
    }

    private void getWritableDatabase() {
        if (mDatabase == null || mDatabase.isReadOnly()) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
    }

    @Override
    public void addInstrumentedPackage(String packageName) {
        getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PackageEntry.COLUMN_NAME_PACKAGE_NAME, packageName);
        values.put(PackageEntry.COLUMN_NAME_TIMESTAMP, System.currentTimeMillis());
        values.put(PackageEntry.COLUMN_NAME_KEEP_INSTRUMENTED, 0);

        if (!isEntryInPresent(packageName)) {
            mDatabase.insert(PackageEntry.TABLE_NAME, null, values);
        } else {
            String selection = PackageEntry.COLUMN_NAME_PACKAGE_NAME + " LIKE ?";
            String[] selectionArgs = {packageName};

            mDatabase.update(
                    PackageEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
        }
    }

    @Override
    public void persistPackage(Package app) {
        getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PackageEntry.COLUMN_NAME_PACKAGE_NAME, app.getPackageName());
        values.put(PackageEntry.COLUMN_NAME_TIMESTAMP, app.getLastInstrumentationTimestamp());
        values.put(PackageEntry.COLUMN_NAME_KEEP_INSTRUMENTED, app.isKeepInstrumented());

        if (!isEntryInPresent(app.getPackageName())) {
            mDatabase.insert(PackageEntry.TABLE_NAME, null, values);
        } else {
            String selection = PackageEntry.COLUMN_NAME_PACKAGE_NAME + " LIKE ?";
            String[] selectionArgs = {app.getPackageName()};

            mDatabase.update(
                    PackageEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
        }
    }

    private boolean isEntryInPresent(String packageName) {
        String selection = PackageEntry.COLUMN_NAME_PACKAGE_NAME + " = ?";
        String[] selectionArgs = {packageName};

        Cursor cursor = mDatabase.query(
                PackageEntry.TABLE_NAME,                     // The table to query
                sInstrumentedAppsProjection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        boolean isPresent = cursor.getCount() == 1;
        cursor.close();
        return isPresent;
    }

    @Override
    public void removeUninstrumentedPackage(String packageName) {
        getWritableDatabase();
        String selection = PackageEntry.COLUMN_NAME_PACKAGE_NAME + " LIKE ?";
        String[] selectionArgs = {packageName};
        mDatabase.delete(PackageEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public List<Package> getAllInstrumentedApps() {
        if (mDatabase == null) {
            mDatabase = mDatabaseHelper.getReadableDatabase();
        }

        Cursor cursor = mDatabase.query(
                PackageEntry.TABLE_NAME,                     // The table to query
                sInstrumentedAppsProjection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        List<Package> packageList = extractCursorInformation(cursor);
        cursor.close();
        return packageList;
    }

    private List<Package> extractCursorInformation(Cursor cursor) {
        List<Package> packageList = new ArrayList<>();
        String packageName;
        long lastInstrumentationTimestamp;
        int keepInstrumented;
        while (cursor.moveToNext()) {
            packageName = cursor.getString(
                    cursor.getColumnIndexOrThrow(PackageEntry.COLUMN_NAME_PACKAGE_NAME));
            lastInstrumentationTimestamp = cursor.getLong(
                    cursor.getColumnIndexOrThrow(PackageEntry.COLUMN_NAME_TIMESTAMP));
            keepInstrumented = cursor.getInt(
                    cursor.getColumnIndexOrThrow(PackageEntry.COLUMN_NAME_KEEP_INSTRUMENTED));
            packageList.add(new Package(packageName, lastInstrumentationTimestamp,
                    keepInstrumented == 1));
        }
        return packageList;
    }

    @Override
    public void onDestroy() {
        mDatabaseHelper.close();
    }
}
