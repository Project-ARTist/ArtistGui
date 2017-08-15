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

package saarland.cispa.artist.artistgui.compilation;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
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

class ApkListAdapter extends RecyclerView.Adapter<ApkListAdapter.ViewHolder> {

    private CompilationContract.Presenter mPresenter;
    private PackageManager mPackageManager;
    private List<PackageInfo> mPackageInfoList;

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

    ApkListAdapter(Context context, CompilationContract.Presenter presenter) {
        mPackageManager = context.getPackageManager();
        mPackageInfoList = mPackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        mPresenter = presenter;
    }

    @Override
    public ApkListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_entry,
                parent, false);

        ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
        TextView appName = (TextView) view.findViewById(R.id.app_name);
        TextView packageName = (TextView) view.findViewById(R.id.package_name);

        return new ViewHolder(view, appIcon, appName, packageName);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PackageInfo packageInfo = mPackageInfoList.get(position);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;

        Drawable appIcon = mPackageManager.getApplicationIcon(applicationInfo);
        String appName = mPackageManager.getApplicationLabel(applicationInfo).toString();
        String packageName = packageInfo.packageName;

        holder.mAppIcon.setImageDrawable(appIcon);
        holder.mAppName.setText(appName);
        holder.mPackageName.setText(packageName);
        holder.itemView.setOnClickListener((view) -> mPresenter.queueCompilation(packageName));
    }

    @Override
    public int getItemCount() {
        return mPackageInfoList.size();
    }
}
