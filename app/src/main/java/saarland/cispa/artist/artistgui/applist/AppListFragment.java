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

package saarland.cispa.artist.artistgui.applist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import java.util.List;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.appdetails.AppDetailsDialog;
import saarland.cispa.artist.artistgui.appdetails.AppDetailsDialogPresenter;
import saarland.cispa.artist.artistgui.applist.adapter.AppIconCache;
import saarland.cispa.artist.artistgui.applist.adapter.AppListAdapter;
import saarland.cispa.artist.artistgui.applist.adapter.OnPackageSelectedListener;
import saarland.cispa.artist.artistgui.applist.loader.AppListLoader;
import saarland.cispa.artist.artistgui.database.Package;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManagerImpl;

public class AppListFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<List<Package>>, OnPackageSelectedListener {

    private static final int LOADER_ID = 9574583;

    private SearchView mSearchView;
    private ProgressBar mProgressBar;
    private RecyclerView mAppListView;
    private AppListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mAppListView = rootView.findViewById(R.id.recycler_view);

        mAppListView.setHasFixedSize(true);

        final Context context = getContext();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        mAppListView.setLayoutManager(mLayoutManager);

        mAdapter = new AppListAdapter(new AppIconCache(context));
        mAdapter.registerPackageSelectedListener(this);
        mAppListView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            Fragment dialog = getFragmentManager().findFragmentByTag(AppDetailsDialog.TAG);
            if (dialog != null) {
                // connect presenter to old fragment
                new AppDetailsDialogPresenter((AppDetailsDialog) dialog, context,
                        new SettingsManagerImpl(context));
            }
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_app_list, menu);

        final MenuItem item = menu.findItem(R.id.search);
        mSearchView = (SearchView) item.getActionView();

        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchView.setIconified(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mAdapter.cancelSearchRequest();
                mSearchView.setQuery("", false);
                return true;
            }
        });


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.handleSearchRequest(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.handleSearchRequest(newText);
                return true;
            }
        });

        mSearchView.setOnCloseListener(() -> {
            mAdapter.cancelSearchRequest();
            return false;
        });
    }

    @Override
    public void onPackageSelected(Package selectedPackage) {
        AppDetailsDialog detailsDialog = new AppDetailsDialog();
        // constructor connects presenter to view
        final Context context = getContext();
        new AppDetailsDialogPresenter(detailsDialog, context, new SettingsManagerImpl(context));

        Bundle bundle = new Bundle();
        bundle.putParcelable(AppDetailsDialog.PACKAGE_KEY, selectedPackage);
        detailsDialog.setArguments(bundle);

        detailsDialog.show(getFragmentManager(), AppDetailsDialog.TAG);
    }

    @Override
    public Loader<List<Package>> onCreateLoader(int id, Bundle args) {
        return new AppListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Package>> loader, List<Package> data) {
        // Set the new data in the adapter.
        mAdapter.setPackagesList(data);

        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
            mAppListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Package>> loader) {
        // Clear the data in the adapter.
        mAdapter.setPackagesList(null);
    }
}
