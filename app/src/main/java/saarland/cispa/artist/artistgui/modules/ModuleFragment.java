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

package saarland.cispa.artist.artistgui.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.modules.adapter.ModuleListAdapter;
import saarland.cispa.artist.artistgui.modules.loader.ModuleListLoader;

import static android.app.Activity.RESULT_OK;

public class ModuleFragment extends Fragment implements ModuleContract.View,
        LoaderManager.LoaderCallbacks<List<Module>> {

    private static final int LOADER_ID = 325554131;

    private static final int FILE_CHOOSER_REQUEST_CODE = 64967;
    private static final String ZIP_MIME_TYPE = "application/zip";

    private Context mContext;
    private ModuleContract.Presenter mPresenter;

    private ProgressBar mProgressBar;
    private RecyclerView mModulesListView;
    private ModuleListAdapter mAdapter;

    @Override
    public void setPresenter(ModuleContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mModulesListView = rootView.findViewById(R.id.recycler_view);

        mModulesListView.setHasFixedSize(true);

        mContext = getContext();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mModulesListView.setLayoutManager(mLayoutManager);

        mAdapter = new ModuleListAdapter(mContext, this);
        mModulesListView.setAdapter(mAdapter);

        mProgressBar.setVisibility(View.GONE);
        mModulesListView.setVisibility(View.VISIBLE);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_modules, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_module:
                openFileChooser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Module>> onCreateLoader(int id, Bundle args) {
        return new ModuleListLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<List<Module>> loader, List<Module> data) {
        // Set the new data in the adapter.
        mAdapter.setData(data);

        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
            mModulesListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Module>> loader) {
        mAdapter.setData(null);
    }

    @Override
    public void openFileChooser() {
        Intent intent = new Intent()
                .setType(ZIP_MIME_TYPE)
                .setAction(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            mPresenter.addModule(data);
        }
    }

    @Override
    public void addModules(List<Module> modules) {
        mAdapter.addModules(modules);
        Toast.makeText(getContext(), getString(R.string.module_import_success),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeModules(Module[] modules) {
        mAdapter.removeModules(modules);
        Toast.makeText(getContext(), getString(R.string.module_removal_success),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void moduleImportFailed() {
        Toast.makeText(getContext(), getString(R.string.module_import_failed),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRemovalDialog(Module module) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.remove_module, module.name))
                .setPositiveButton(R.string.remove, (dialog, id) -> mPresenter.removeModule(module))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
