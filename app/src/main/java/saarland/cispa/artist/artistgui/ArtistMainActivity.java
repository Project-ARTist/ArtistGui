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

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import saarland.cispa.artist.StringUtils;
import saarland.cispa.artist.artistgui.settings.ArtistGuiSettingsGeneral;
import saarland.cispa.artist.artistgui.utils.CompatUtils;
import saarland.cispa.artist.log.Logg;
import trikita.log.Log;

public class ArtistMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ArtistMainActivity";

    public static final String EXTRA_PACKAGE = "INTENT_EXTRA_PACKAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // compatibility check
        if (!CompatUtils.supportedByArtist()) {
            new AlertDialog.Builder(this).setTitle("Incompatible Android version")
                    .setMessage("Artist does not support your Android version. For more information, " +
                            "please visit https://artist.cispa.saarland\n" +
                            "You can still navigate the application. However, the core functionality, e.g., recompiling applications, will most probably not work.")
                    .setPositiveButton("Close", (dialog, which) -> {}).show();
        }


        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PACKAGE)) {
            final String packageName = intent.getStringExtra(EXTRA_PACKAGE);
            if (packageName == null) {
                Log.e(TAG, "Ignoring NULL package name in intent extra");
                return;
            }
            final Intent compileActivityintent = new Intent(ArtistMainActivity.this, CompileActivity.class);
            compileActivityintent.putExtra(EXTRA_PACKAGE, packageName);
            ArtistMainActivity.this.startActivity(compileActivityintent);
        }

        setupTextView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Logg.setUserLogLevel(getApplicationContext());

        Logg.logTest();
    }

    private void setupTextView() {

        StringBuilder artistGuiVersion = new StringBuilder();
        StringBuilder artistVersion = new StringBuilder();

        try {

            AssetManager assetMan = getAssets();
            final String[] rootAssets = assetMan.list("");
            for (final String asset : rootAssets) {
                if (asset.startsWith("VERSION_ARTIST-")) {
                    InputStream artistVersionIs = assetMan.open(asset);
                    artistVersion.append("Artist ApiLevel: ")
                            .append(asset)
                            .append(" \n")
                            .append(StringUtils.readIntoString(artistVersionIs))
                            .append("\n");
                } else if (asset.startsWith("VERSION_ARTISTGUI.md")) {
                    InputStream guiVersionIs = assetMan.open(asset);
                    artistGuiVersion.append(StringUtils.readIntoString(guiVersionIs))
                            .append("\n");
                }
            }

        } catch (final IOException e) {
            Log.e(TAG, "Could not Read Artist Version files from assets.", e);
        }

        TextView mainTextView = (TextView) findViewById(R.id.main_activity_textview);
        mainTextView.append("\n\n");
        mainTextView.append("ArtistGUI Version:\n");
        mainTextView.append(artistGuiVersion.toString());
        mainTextView.append("\n\n");
        mainTextView.append("Artist Dex2oat Versions:\n");
        mainTextView.append(artistVersion.toString());
        mainTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.artist_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            navigateSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_compiler) {
            Log.d(TAG, "Starting Artist Compiler GUI");
            final Intent compileActivityintent = new Intent(ArtistMainActivity.this, CompileActivity.class);
            ArtistMainActivity.this.startActivity(compileActivityintent);
        } else if (id == R.id.nav_settings) {
            navigateSettings();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateSettings() {
        Log.d(TAG, "Starting Settings");
        final Intent generalSettings = new Intent(ArtistMainActivity.this, ArtistGuiSettingsGeneral.class);
        ArtistMainActivity.this.startActivity(generalSettings);
    }

}
