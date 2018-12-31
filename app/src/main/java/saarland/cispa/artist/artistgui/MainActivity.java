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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import saarland.cispa.artist.artistgui.settings.SettingsActivity;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManagerImpl;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MainActivityContract.View {

    private MainActivityContract.Presenter mPresenter;
    private FragmentManager mFragmentManager;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentManager = getSupportFragmentManager();
        mPresenter = new MainActivityPresenter(this, new SettingsManagerImpl(this), this);

        if (savedInstanceState == null) {
            mPresenter.checkCompatibility();
            mPresenter.selectFragment(MainActivityPresenter.INFO_FRAGMENT);
        } else {
            mPresenter.restoreSavedInstanceState(savedInstanceState, mFragmentManager);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenter.saveInstanceState(outState);
    }

    @Override
    public void setPresenter(MainActivityContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showIncompatibleAndroidVersionDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.incompatible_android_version)
                .setMessage(R.string.unsupported_android_version_info)
                .setPositiveButton("Close", (dialog, which) -> {
                }).show();
    }

    @Override
    public void showMissingDex2OatFilesDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.missing_dex2oat_files)
                .setMessage(R.string.missing_dex2oat_files_description)
                .setPositiveButton("Learn more", (dialog, which) ->
                        mPresenter.openDex2OatHelpPage()).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                mPresenter.selectFragment(MainActivityPresenter.INFO_FRAGMENT);
                break;
            case R.id.nav_app_list:
                mPresenter.selectFragment(MainActivityPresenter.INSTRUMENTATION_FRAGMENT);
                break;
            case R.id.nav_modules:
                mPresenter.selectFragment(MainActivityPresenter.MODULES_FRAGMENT);
                break;
            case R.id.nav_settings:
                final Intent generalSettings = new Intent(this, SettingsActivity.class);
                startActivity(generalSettings);
                return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void showSelectedFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
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
}
