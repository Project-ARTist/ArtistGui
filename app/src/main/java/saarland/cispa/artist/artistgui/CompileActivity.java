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
package saarland.cispa.artist.artistgui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import saarland.cispa.artist.ArtistImpl;
import saarland.cispa.artist.artistgui.gui.ApkArrayAdapter;
import saarland.cispa.artist.artistgui.gui.CompileNotificationManager;
import saarland.cispa.artist.artistgui.settings.ArtistAppConfig;
import saarland.cispa.artist.artistgui.settings.ArtistGuiSettingsGeneral;
import saarland.cispa.artist.log.Logg;
import saarland.cispa.artist.utils.AndroidUtils;
import saarland.cispa.artist.utils.GuiUtils;
import trikita.log.Log;

/**
 * @author Sebastian Weisgerber (weisgerber@cispa.saarland)
 * @author Oliver Schranz (oliver.schranz@cispa.saarland)
 */
public class CompileActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener {

    public static final String TAG = "CompileActivity";

    private static  final String VERSION = "0001";
    public static final String PATH_ASSETS_APPS = "apps";

    ArtistAppConfig config = null;

    CompilationService compileService;

    public CompileActivity() {
        this.config = new ArtistAppConfig();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection compileServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected()");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CompilationService.CompilationServiceBinder binder
                    = (CompilationService.CompilationServiceBinder ) service;
            compileService = binder.getService();
            if(!compileService.isCompiling()) {
                CompileNotificationManager.cancelNotification(getApplicationContext());
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected()");
            compileService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ArtistGUI Version:" + VERSION);
        setupActivity();
    }

    private void connectToCompilationService() {
        Log.d(TAG, "connectToCompilationService()");
        CompilationService.startService(getApplicationContext(), null);
        Intent intent = new Intent(this, CompilationService.class);
        bindService(intent, compileServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupActivity() {
        setContentView(R.layout.activity_compiler_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // check for preconditions and warn user if some are not met, e.g., missing settings
        checkPreconditions();
    }

    private void checkPreconditions() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String userCodeLib = sharedPref.getString(ArtistAppConfig.PREF_KEY_CODELIB_SELECTION, null);

        final boolean codeLibChosen = userCodeLib != null && !userCodeLib.equals("-1");
        final boolean shouldMerge = sharedPref.getBoolean(ArtistAppConfig.KEY_PREF_COMPILER_INJECT_CODELIB, true);
        // warn the user IFF no code lib is chosen AND code lib should be merged
        if (!codeLibChosen && shouldMerge) {
            // can be any named view in the current layout
            final View view = findViewById(R.id.appsListView);
            GuiUtils.displaySnackForever(view, "WARNING: No codelib chosen yet. Compilation migh crash.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Logg.setUserLogLevel(getApplicationContext());

        executeAvailableIntentTasks();

        setupAppList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectToCompilationService();
        createArtistFolders();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Log.d(TAG, "Starting Home Activity");
            final Intent compileActivityintent = new Intent(CompileActivity.this, ArtistMainActivity.class);
            CompileActivity.this.startActivity(compileActivityintent);
        } else if (id == R.id.nav_compiler) {
            Toast.makeText(CompileActivity.this, "Compiler already running!",
                    Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "Starting Settings");
            final Intent generalSettings = new Intent(CompileActivity.this, ArtistGuiSettingsGeneral.class);
            CompileActivity.this.startActivity(generalSettings);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(compileServiceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.d(TAG, String.format(Locale.US, "onActivityResult() requestcode[%d] resultcode[%d] Data[%s]",
                requestCode, resultCode, data));
        switch (requestCode) {
            case CompileDialogActivity.COMPILE_DIALOG_ID: {
                onActivityResultCompileDialog(resultCode, data);
                break;
            }
        }
        Log.d(TAG, String.format(Locale.US, "onActivityResult() requestcode[%d] resultcode[%d] Data[%s] DONE",
                requestCode, resultCode, data));
    }

    private void onActivityResultCompileDialog(int resultCode, Intent data) {
        String applicationName = "";
        if (data != null) {
            applicationName = data.getStringExtra(ArtistImpl.INTENT_EXTRA_APP_NAME);
        }
        Log.d(TAG, "onActivityResult() ResultCode: " + resultCode);
        final View navView = findViewById(R.id.nav_view);

        boolean success = false;
        String userMessage;
        switch (resultCode) {
            case RESULT_OK: {
                userMessage = getResources().getString(R.string.snack_compilation_success);
                userMessage += " " + applicationName;
                success = true;
                maybeStartRecompiledApp(applicationName);
                break;
            }
            case RESULT_CANCELED: {
                userMessage = getResources().getString(R.string.snack_compilation_failed);
                userMessage += " " + applicationName;
                break;
            }
            default: {
                // TODO possible implementation bug here if this case is hit. Warning? Exit?
                userMessage = "";
                break;
            }
        }
        writeResultFile(applicationName, success);
        GuiUtils.displaySnackLong(navView, userMessage);
    }

    private boolean writeResultFile(String packageName, boolean success) {
        // TODO get rid of hardcoded path
        final File resultsDir = new File(getExternalFilesDir(null), "ArtistResults");
        if (!resultsDir.exists()) {
            if (!resultsDir.mkdir()) {
                Log.e(TAG, "Could not create results dir.");
                return false;
            }
        }
        final File resultsFile = new File(resultsDir, packageName.replaceAll("/", "_").replace(".apk",""));
        Log.d(TAG, "Writing success '" + success + "' to file " +resultsFile.getAbsolutePath());
        try {
            if (resultsFile.exists()) {
                if (!resultsFile.delete()) {
                    Log.e(TAG, "Could not delete existing results file.");
                    return false;
                }
            }
            if (!resultsFile.createNewFile()) {
                Log.e(TAG, "Could not create new results file.");
                return false;
            }
            try (FileOutputStream fos = new FileOutputStream(resultsFile)) {
                fos.write((success + "").getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not write results file.", e);
            return false;
        }
        return true;
    }

    private void maybeStartRecompiledApp(final String applicationName) {
        Log.d(TAG, "maybeStartRecompiledApp() ? " + applicationName);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean launchActivity =
                sharedPref.getBoolean(ArtistAppConfig.KEY_PREF_COMPILER_LAUNCH_ACTIVITY, false);
        if (launchActivity) {
            Log.d(TAG, "Starting compiled app: " + applicationName);
            final Intent launchIntent = getPackageManager().getLaunchIntentForPackage(applicationName);
            startActivity(launchIntent);
        }
    }

    private void setupAppList() {
        final AssetManager assetManager = getAssets();

        final String[] list = setupAppListBundledApps(assetManager);

        final ArrayList<String> applicationList = new ArrayList<>();

        applicationList.add("<< boot image >>");
        applicationList.add(GuiUtils.LIST_VIEW_SEPARATOR);
        applicationList.addAll(Arrays.asList(list));
        applicationList.add(GuiUtils.LIST_VIEW_SEPARATOR);

        final List<String> installedApps = AndroidUtils.getInstalledPackages(getApplicationContext());

        applicationList.addAll(installedApps);

        setupAppListView(applicationList);
    }

    private void setupAppListView(ArrayList<String> applicationList) {
        ListView listView = (ListView) findViewById(R.id.appsListView);

        ApkArrayAdapter apkAdapter = new ApkArrayAdapter(this, R.layout.app_list_entry, applicationList);

        listView.setAdapter(apkAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final String apkFilename;

            if (position == 0) {
                apkFilename = "";
            } else {
                apkFilename = (String) parent.getItemAtPosition(position);
            }

            queueCompilation(apkFilename);
        });
    }

    @NonNull
    private String[] setupAppListBundledApps(AssetManager assetManager) {

        String list[];

        try {
            list = assetManager.list(PATH_ASSETS_APPS);

            if (list != null) {
                Log.d(TAG, "Bundled App count: " + list.length);
                for (final String bundledApk : list) {
                    Log.d("Bundle App:", "apps/" + bundledApk);
                }
            }
        } catch (final IOException e) {
            Log.w(TAG, "Bundled Applist Error", e);
            list = new String[0];
        }
        //noinspection ConstantConditions
        return list;
    }

    private void executeAvailableIntentTasks() {
        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ArtistMainActivity.EXTRA_PACKAGE)) {
            final String packageName = intent.getStringExtra(ArtistMainActivity.EXTRA_PACKAGE);
            if (packageName != null) {
                Log.d(TAG, "CompilationTask() Execute: " + packageName);
                queueCompilation(packageName);
            }
        }
    }

    private void queueCompilation(String packageName) {
        Log.d(TAG, "compileInstalledApp(): " + packageName);
        CompileDialogActivity.compile(CompileActivity.this, packageName);
    }

    private void createArtistFolders() {
        if (config.apkBackupFolderLocation.isEmpty()) {
            config.apkBackupFolderLocation =
                    AndroidUtils.createFoldersInFilesDir(getApplicationContext(),
                            ArtistAppConfig.APP_FOLDER_APK_BACKUP);
        }
        if (config.codeLibFolder.isEmpty()) {
            config.codeLibFolder = AndroidUtils.createFoldersInFilesDir(getApplicationContext(),
                            ArtistAppConfig.APP_FOLDER_CODELIBS);
        }
    }
}
