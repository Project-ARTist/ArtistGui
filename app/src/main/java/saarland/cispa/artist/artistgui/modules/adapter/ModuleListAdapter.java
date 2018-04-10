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

package saarland.cispa.artist.artistgui.modules.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.modules.ModuleContract;

public class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ViewHolder> {

    private Context mContext;
    private ModuleContract.View mView;

    private List<Module> mDataset;

    private Drawable mDefaultIcon;

    // Reference for performance instead of slow findByView lookup
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mModuleIcon;
        TextView mModuleName;
        TextView mModulePackageName;

        ViewHolder(View v, ImageView moduleIcon, TextView moduleName, TextView author) {
            super(v);
            mModuleIcon = moduleIcon;
            mModuleName = moduleName;
            mModulePackageName = author;
        }
    }

    public ModuleListAdapter(Context context, ModuleContract.View view) {
        mContext = context;
        mView = view;
        mDataset = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_entry,
                parent, false);

        ImageView appIcon = view.findViewById(R.id.app_icon);
        TextView appName = view.findViewById(R.id.app_name);
        TextView packageName = view.findViewById(R.id.package_name);

        if (mDefaultIcon == null) {
            mDefaultIcon = mContext.getDrawable(android.R.mipmap.sym_def_app_icon);
        }

        return new ViewHolder(view, appIcon, appName, packageName);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Module module = mDataset.get(position);
        holder.mModuleIcon.setImageDrawable(mDefaultIcon);
        holder.mModuleName.setText(module.name);
        holder.mModulePackageName.setText(module.packageName);
        holder.itemView.setOnClickListener((view) -> mView.showRemovalDialog(module));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public void addModules(List<Module> modules) {
        boolean modifiedDataset = false;
        for (Module module : modules) {
            if (!module.isUpdating) {
                mDataset.add(module);
                modifiedDataset = true;
            }
        }
        if (modifiedDataset) {
            notifyDataSetChanged();
        }
    }

    public void removeModules(Module... modules) {
        for (Module module : modules) {
            mDataset.remove(module);
        }
        notifyDataSetChanged();
    }

    public void setData(List<Module> modules) {
        mDataset = modules == null ? new ArrayList<>() : modules;
        notifyDataSetChanged();
    }
}
