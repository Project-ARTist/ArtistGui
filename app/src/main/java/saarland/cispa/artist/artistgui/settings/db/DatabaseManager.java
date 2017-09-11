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

public class DatabaseManager implements InstrumentedPackagesManager {

    private static String[] sInstrumentedAppsProjection = {
            DbContract.PackageEntry.COLUMN_NAME_PACKAGE_NAME,
            DbContract.PackageEntry.COLUMN_NAME_TIMESTAMP
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

    public void addInstrumentedPackage(String packageName) {
        getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.PackageEntry.COLUMN_NAME_PACKAGE_NAME, packageName);
        values.put(DbContract.PackageEntry.COLUMN_NAME_TIMESTAMP, System.currentTimeMillis());

        if (!isEntryInPresent(packageName)) {
            mDatabase.insert(DbContract.PackageEntry.TABLE_NAME, null, values);
        } else {
            String selection = DbContract.PackageEntry.COLUMN_NAME_PACKAGE_NAME + " LIKE ?";
            String[] selectionArgs = {packageName};

            mDatabase.update(
                    DbContract.PackageEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
        }
    }

    private boolean isEntryInPresent(String packageName) {
        String selection = DbContract.PackageEntry.COLUMN_NAME_PACKAGE_NAME + " = ?";
        String[] selectionArgs = {packageName};

        Cursor cursor = mDatabase.query(
                DbContract.PackageEntry.TABLE_NAME,                     // The table to query
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

    public void removeUninstrumentedPackage(String packageName) {
        getWritableDatabase();
        String selection = DbContract.PackageEntry.COLUMN_NAME_PACKAGE_NAME + " LIKE ?";
        String[] selectionArgs = {packageName};
        mDatabase.delete(DbContract.PackageEntry.TABLE_NAME, selection, selectionArgs);
    }

    public List<Package> getAllInstrumentedApps() {
        if (mDatabase == null) {
            mDatabase = mDatabaseHelper.getReadableDatabase();
        }

        Cursor cursor = mDatabase.query(
                DbContract.PackageEntry.TABLE_NAME,                     // The table to query
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
        while (cursor.moveToNext()) {
            packageName = cursor.getString(
                    cursor.getColumnIndexOrThrow(DbContract.PackageEntry.COLUMN_NAME_PACKAGE_NAME));
            lastInstrumentationTimestamp = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DbContract.PackageEntry.COLUMN_NAME_TIMESTAMP));
            packageList.add(new Package(packageName, lastInstrumentationTimestamp));
        }
        return packageList;
    }

    public void onDestroy() {
        mDatabaseHelper.close();
    }
}
