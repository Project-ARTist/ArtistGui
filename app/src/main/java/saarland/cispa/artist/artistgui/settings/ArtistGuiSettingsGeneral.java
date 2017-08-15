/**
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
 * @author "Oliver Schranz <oliver.schranz@cispa.saarland>"
 * @author "Sebastian Weisgerber <weisgerber@cispa.saarland>"
 *
 */
package saarland.cispa.artist.artistgui.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.MainActivity;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.gui.GuiHelper;
import saarland.cispa.artist.artistgui.utils.UriUtils;
import saarland.cispa.artist.log.Logg;
import saarland.cispa.artist.utils.AndroidUtils;
import saarland.cispa.artist.utils.ArtistUtils;
import trikita.log.Log;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ArtistGuiSettingsGeneral extends AppCompatPreferenceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ArtistGuiSettings";

    @Override
    public void onActivityResult(final int requestCode,
                                 final int resultCode,
                                 final Intent resultData) {
        Log.d(TAG, "ArtistGuiSettingsGeneral.onActivityResult()");

        if (requestCode == GuiHelper.READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                final Uri uri = resultData.getData();
                final String importedCodeLibName =
                        UriUtils.getFilenameFromUri(uri);

                final String toPath = AndroidUtils.getFilesDirLocation(
                        getApplicationContext(),
                        ArtistAppConfig.APP_FOLDER_CODELIBS + File.separator + importedCodeLibName);

                final String pathToCodeLib =
                        AndroidUtils.copyUriToFilesystem(getApplicationContext(), uri, toPath);
                if (!pathToCodeLib.isEmpty()) {
                    Log.i(TAG, "Codelib Copied: " + pathToCodeLib);
                }
            }
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            (preference, value) -> {

                final String stringValue = value.toString();

                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                }

                try {
                    switch (preference.getKey()) {
                        case Logg.PREF_KEY_LOGLEVEL:
                            Logg.setUserLogLevel(stringValue);
                            break;
                    }
                } catch (final NullPointerException e) {
                    Log.w(TAG, "Preference without Key " + preference.getKey()
                            + " changed to value: " + stringValue);
                }

                return true;
            };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        createArtistFolders();
    }

    private void createArtistFolders() {
        Log.i(TAG, "createArtistFolders()");
        AndroidUtils.createFoldersInFilesDir(getApplicationContext(),
                ArtistAppConfig.APP_FOLDER_CODELIBS);
    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || CompilerPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Log.d(TAG, "Starting Home Activity");
            final Intent compileActivityintent = new Intent(ArtistGuiSettingsGeneral.this, MainActivity.class);
            ArtistGuiSettingsGeneral.this.startActivity(compileActivityintent);
        } else if (id == R.id.nav_compiler) {
            Toast.makeText(ArtistGuiSettingsGeneral.this, "Compiler already running!",
                    Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "Starting Settings");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(ArtistAppConfig.KEY_PREF_GENERAL_LOGLEVEL));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CompilerPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_compiler);

            setHasOptionsMenu(true);

            setupPreferenceListener();
        }

        @Override
        public void onStart() {
            super.onStart();
            setupCodeLibSelectionListPreference();
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(ArtistAppConfig.PREF_KEY_CODELIB_SELECTION));
            bindPreferenceSummaryToValue(findPreference(ArtistAppConfig.KEY_PREF_COMPILER_THREADS));
        }

        private void setupCodeLibSelectionListPreference() {
            ListPreference codeLibSelection = (ListPreference)
                    findPreference(ArtistAppConfig.PREF_KEY_CODELIB_SELECTION);

            final String[] assetCodeLibs = GuiHelper.listAssetCodeLibs(getContext());
            final String[] importedCodeLibs = GuiHelper.listImportedCodeLibs(getContext());

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

        private void setupPreferenceListener() {

            Preference codeLibPref = findPreference(ArtistAppConfig.PREF_KEY_CODELIB_IMPORT);
            codeLibPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 44556;

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(TAG, "performFileSearch()");

                    // Assume thisActivity is the current activity
                    final int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        // No explanation needed, we can request the permission.
                        Log.d(TAG, "Requesting Permission: " + Manifest.permission.READ_EXTERNAL_STORAGE);

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    } else {
                        GuiHelper.showFileChooserApks(getActivity());
                    }

                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ArtistGuiSettingsGeneral.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }
}
