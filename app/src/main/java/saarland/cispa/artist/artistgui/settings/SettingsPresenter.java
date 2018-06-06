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

package saarland.cispa.artist.artistgui.settings;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.settings.config.ArtistAppConfig;
import saarland.cispa.artist.artistgui.utils.AndroidUtils;
import saarland.cispa.artist.artistgui.utils.ArtistUtils;
import saarland.cispa.utils.LogA;
import saarland.cispa.artist.artistgui.utils.UriUtils;

class SettingsPresenter implements SettingsContract.Presenter {

    static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 44556;

    private static final String TAG = "SettingsPresenter";
    private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";

    private Context mContext;
    private SettingsContract.View mView;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener mBindValueToSummaryListener =
            (preference, value) -> {
                bindValueToSummary(preference, value);
                return true;
            };

    private Preference.OnPreferenceChangeListener mLoggingPrefListener =
            (preference, value) -> {
                bindValueToSummary(preference, value);
                LogA.setUserLogLevel(value.toString());
                return true;
            };

    SettingsPresenter(Context context, SettingsContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void bindPrefValueToSummary(boolean isNewInstance, Preference preference) {
        preference.setOnPreferenceChangeListener(mBindValueToSummaryListener);
        if (isNewInstance) {
            triggerInitialOnPreferenceChange(preference, mBindValueToSummaryListener);
        }
    }

    @Override
    public void setupLoggingListener(boolean isNewInstance, Preference preference) {
        preference.setOnPreferenceChangeListener(mLoggingPrefListener);
        if (isNewInstance) {
            triggerInitialOnPreferenceChange(preference, mLoggingPrefListener);
        }
    }

    private void bindValueToSummary(Preference preference, Object value) {
        ListPreference listPreference = (ListPreference) preference;
        final String stringValue = value.toString();
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        preference.setSummary(index >= 0 ? listPreference.getEntries()[index]
                : null);
    }

    private void triggerInitialOnPreferenceChange(Preference preference,
                                                  Preference.OnPreferenceChangeListener listener) {
        listener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void setupCodeLibSelection(ListPreference codeLibSelection) {
        final String[] assetCodeLibs = listAssetCodeLibs();
        final String[] importedCodeLibs = listImportedCodeLibs();

        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (final String codeLib : assetCodeLibs) {
            entries.add(codeLib + " (Asset)");
            values.add(ArtistUtils.CODELIB_ASSET + codeLib);
        }
        for (final String codeLib : importedCodeLibs) {
            entries.add(codeLib + " (Imported)");
            values.add(ArtistUtils.CODELIB_IMPORTED + codeLib);
        }
        codeLibSelection.setEntries(entries.toArray(new CharSequence[0]));
        codeLibSelection.setEntryValues(values.toArray(new CharSequence[0]));
    }

    @Override
    public void setupCodeLibImport(final Activity activity, Preference codeLibPref) {
        codeLibPref.setOnPreferenceClickListener((Preference preference) ->
        {
            LogA.d(TAG, "performFileSearch()");

            final int permissionCheck = activity
                    .checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                LogA.d(TAG, "Requesting Permission: " + Manifest.permission.READ_EXTERNAL_STORAGE);
                activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_REQUEST_CODE);
            } else {
                mView.showFileChooser(APK_MIME_TYPE);
            }

            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mView.showFileChooser(APK_MIME_TYPE);
        } else {
            Toast.makeText(mContext,
                    mContext.getString(R.string.external_storage_permission_denied),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void processChosenCodeLib(Intent resultData) {
        if (resultData != null) {
            final Uri uri = resultData.getData();
            final String importedCodeLibName =
                    UriUtils.getFilenameFromUri(uri);

            final String toPath = AndroidUtils.getFilesDirLocation(
                    mContext,
                    ArtistAppConfig.APP_FOLDER_CODELIBS + File.separator + importedCodeLibName);

            final String pathToCodeLib =
                    AndroidUtils.copyUriToFilesystem(mContext, uri, toPath);
            if (!pathToCodeLib.isEmpty()) {
                LogA.i(TAG, "CodeLib copied: " + pathToCodeLib);
            }
        }
    }

    private String[] listAssetCodeLibs() {
        final AssetManager assets = mContext.getAssets();
        final List<String> cleanedAssetCodeLibs = new ArrayList<>();

        String[] assetCodeLibs;
        try {
            assetCodeLibs = assets.list(ArtistAppConfig.ASSET_FOLDER_CODELIBS);
            for (final String assetLib : assetCodeLibs) {
                if (assetLib.endsWith(".apk")
                        || assetLib.endsWith(".jar")
                        || assetLib.endsWith(".zip")
                        || assetLib.endsWith(".dex")) {
                    cleanedAssetCodeLibs.add(assetLib);
                }
            }

        } catch (final IOException e) {
            LogA.e(TAG, "Could not open assetfolder: ", e);

        }
        return cleanedAssetCodeLibs.toArray(new String[0]);
    }

    private String[] listImportedCodeLibs() {
        final String codeLibFolder = AndroidUtils.getFilesDirLocation(mContext,
                ArtistAppConfig.APP_FOLDER_CODELIBS);
        String[] importedCodeLibs = new File(codeLibFolder).list();
        if (importedCodeLibs == null) {
            importedCodeLibs = new String[0];
        }
        return importedCodeLibs;
    }
}
