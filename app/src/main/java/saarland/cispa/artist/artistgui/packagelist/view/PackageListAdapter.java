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

package saarland.cispa.artist.artistgui.packagelist.view;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.packagelist.view.broadcastreceiver.OnPackageModifiedListener;

class PackageListAdapter extends RecyclerView.Adapter<PackageListAdapter.ViewHolder>
        implements OnPackageModifiedListener {

    private static final int PACKAGE_MANAGER_EMPTY_FLAG = 0;

    private PackageManager mPackageManager;
    private AppIconCache mAppIconCache;

    private List<Package> mPackageList;
    private List<PackageListView.OnPackageSelectedListener> mListeners;

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

    PackageListAdapter(Context context, List<Package> packageList,
                       List<PackageListView.OnPackageSelectedListener> listeners) {
        mAppIconCache = new AppIconCache(context);
        mPackageList = packageList;
        mListeners = listeners;
        mPackageManager = context.getPackageManager();
    }

    @Override
    public PackageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_entry,
                parent, false);

        ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        TextView appName = (TextView) view.findViewById(R.id.app_name);
        TextView packageName = (TextView) view.findViewById(R.id.package_name);

        return new ViewHolder(view, appIcon, appName, packageName);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Package packageEntry = mPackageList.get(position);

        String packageName = packageEntry.getPackageName();
        Drawable appIcon = mAppIconCache.get(packageEntry);

        holder.mAppIcon.setImageDrawable(appIcon);
        holder.mAppName.setText(packageEntry.getAppName());
        holder.mPackageName.setText(packageName);
        holder.itemView.setOnClickListener((view) -> {
            for (PackageListView.OnPackageSelectedListener l : mListeners) {
                l.onPackageSelected(packageName);
            }
        });
    }

    @Override
    public void onPackageInstalled(String packageName) {
        try {
            ApplicationInfo info = mPackageManager.getApplicationInfo(packageName,
                    PACKAGE_MANAGER_EMPTY_FLAG);
            String appName = mPackageManager.getApplicationLabel(info).toString();

            mPackageList.add(new Package(appName, info.packageName, info.icon));
            mPackageList.sort(Package.sComparator);
            notifyDataSetChanged();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPackageRemoved(String packageName) {
        String p;
        for (int i = 0; i < mPackageList.size(); i++) {
            p = mPackageList.get(i).getPackageName();
            if (p.equals(packageName)) {
                mPackageList.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPackageList.size();
    }
}
