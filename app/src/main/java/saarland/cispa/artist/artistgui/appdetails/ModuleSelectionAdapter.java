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

package saarland.cispa.artist.artistgui.appdetails;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import saarland.cispa.artist.artistgui.database.Module;

public class ModuleSelectionAdapter extends RecyclerView.Adapter<ModuleSelectionAdapter
        .ViewHolder> {

    private List<Module> mDataset;
    private BitSet mBitSet;

    // Reference for performance instead of slow findByView lookup
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CheckedTextView view = (CheckedTextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
        return new ModuleSelectionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CheckedTextView view = (CheckedTextView) holder.itemView;
        view.setOnClickListener(l -> {
            boolean isChecked = view.isChecked();
            view.setChecked(!isChecked);
            mBitSet.flip(position);
        });
        Module module = mDataset.get(position);
        view.setText(module.name);
    }

    @Override
    public int getItemCount() {
        return mDataset != null ? mDataset.size() : 0;
    }

    public void setData(List<Module> modules) {
        mDataset = modules == null ? new ArrayList<>() : modules;
        mBitSet = new BitSet();
        notifyDataSetChanged();
    }

    public List<Module> getSelectedModules() {
        List<Module> result = new ArrayList<>();
        for (int i = 0; i < mBitSet.size(); i++) {
            if (mBitSet.get(i)) {
                Module m = mDataset.get(i);
                result.add(m);
            }
        }
        return result;
    }
}
