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

package saarland.cispa.artist.artistgui.applist.adapter;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.database.Package;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private AppIconCache mAppIconCache;

    private List<Package> mPackagesList;
    private List<Package> mDisplayingList;
    private List<OnPackageSelectedListener> mListeners;

    // Reference for performance instead of slow findByView lookup
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mAppIcon;
        TextView mAppName;
        TextView mPackageName;

        ViewHolder(View v, ImageView appIcon, TextView appName, TextView packageName) {
            super(v);
            mAppIcon = appIcon;
            mAppName = appName;
            mPackageName = packageName;
        }
    }

    public AppListAdapter(AppIconCache iconCache) {
        mAppIconCache = iconCache;
        mDisplayingList = new ArrayList<>();
        mListeners = new ArrayList<>();
    }

    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_entry,
                parent, false);

        ImageView appIcon = view.findViewById(R.id.app_icon);
        TextView appName = view.findViewById(R.id.app_name);
        TextView packageName = view.findViewById(R.id.package_name);

        return new ViewHolder(view, appIcon, appName, packageName);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Package packageEntry = mDisplayingList.get(position);

        String packageName = packageEntry.packageName;
        Drawable appIcon = mAppIconCache.get(packageEntry);

        holder.mAppIcon.setImageDrawable(appIcon);
        holder.mAppName.setText(packageEntry.appName);
        holder.mPackageName.setText(packageName);
        holder.itemView.setOnClickListener((view) -> {
            for (OnPackageSelectedListener l : mListeners) {
                l.onPackageSelected(packageEntry);
            }
        });
    }

    public void setPackagesList(List<Package> packages) {
        mPackagesList = new ArrayList<>();
        if (packages != null) {
            mPackagesList.addAll(packages);
            mDisplayingList = mPackagesList;
            notifyDataSetChanged();
        }
    }

    public void handleSearchRequest(@NonNull String searchText) {
        List<Package> resultingList = new ArrayList<>();
        for (Package p : mPackagesList) {
            if (p.packageName.contains(searchText) || p.appName.contains(searchText)) {
                resultingList.add(p);
            }
        }
        mDisplayingList = resultingList;
        notifyDataSetChanged();
    }

    public void cancelSearchRequest() {
        mDisplayingList = mPackagesList;
        notifyDataSetChanged();
    }

    public void registerPackageSelectedListener(OnPackageSelectedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public int getItemCount() {
        return mDisplayingList.size();
    }
}
