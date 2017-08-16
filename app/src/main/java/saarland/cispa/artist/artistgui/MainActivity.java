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

package saarland.cispa.artist.artistgui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Locale;

import saarland.cispa.artist.artistgui.compilation.CompilationContract;
import saarland.cispa.artist.artistgui.compilation.CompilationPresenter;
import saarland.cispa.artist.artistgui.compilation.CompileFragment;
import saarland.cispa.artist.artistgui.settings.SettingsActivity;
import saarland.cispa.artist.artistgui.utils.CompatUtils;
import saarland.cispa.artist.log.Logg;
import trikita.log.Log;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    public static final String EXTRA_PACKAGE = "INTENT_EXTRA_PACKAGE";

    private InfoFragment mInfoFragment;

    private CompileFragment mCompileFragment;
    CompilationContract.Presenter mCompilationPresenter;

    private FragmentManager mFragmentManager;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentManager = getSupportFragmentManager();

        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MainActivity.EXTRA_PACKAGE)) {
            mCompilationPresenter.executeIntentTasks(intent);
            selectFragmentToDisplay(R.id.nav_compiler);
        } else {
            selectFragmentToDisplay(R.id.nav_home);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Setup logging
        Logg.setUserLogLevel(getApplicationContext());

        // compatibility check
        if (!CompatUtils.supportedByArtist()) {
            new AlertDialog.Builder(this).setTitle(R.string.incompatible_android_version)
                    .setMessage(R.string.unsupported_android_version_info)
                    .setPositiveButton("Close", (dialog, which) -> {
                    }).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
            case R.id.nav_compiler:
                selectFragmentToDisplay(id);
                break;
            case R.id.nav_settings:
                final Intent generalSettings = new Intent(this, SettingsActivity.class);
                startActivity(generalSettings);
                return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectFragmentToDisplay(@IdRes int id) {
        Fragment selectedFragment = null;
        switch (id) {
            case R.id.nav_home:
                if (mInfoFragment == null) {
                    mInfoFragment = new InfoFragment();
                }
                selectedFragment = mInfoFragment;
                break;
            case R.id.nav_compiler:
                if (mCompileFragment == null) {
                    mCompileFragment = new CompileFragment();
                    mCompilationPresenter = new CompilationPresenter(this, mCompileFragment);
                }
                selectedFragment = mCompileFragment;
                break;
        }
        mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, selectedFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.d(TAG, String.format(Locale.US,
                "onActivityResult() requestcode[%d] resultcode[%d] Data[%s]",
                requestCode, resultCode, data));

        if (requestCode == CompileDialogActivity.COMPILE_DIALOG_ID) {
            mCompilationPresenter.onCompilationFinished(resultCode, data);
        }
    }
}
