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
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class PackageListView extends RecyclerView implements ReadInstalledPackagesAsyncTask
        .OnReadInstalledPackages {

    public interface OnPackageSelectedListener {
        void onPackageSelected(String packageName);
    }

    private Context mContext;
    private List<OnPackageSelectedListener> mListeners;

    public PackageListView(Context context) {
        this(context, null);
    }

    public PackageListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PackageListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mListeners = new ArrayList<>();
        mContext = context;

        setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        setLayoutManager(mLayoutManager);

        PackageManager packageManager = context.getPackageManager();
        new ReadInstalledPackagesAsyncTask(this).execute(packageManager);
    }

    @Override
    public void onReadInstalledPackages(List<Package> packages) {
        PackageListAdapter adapter = new PackageListAdapter(mContext, packages, mListeners);
        setAdapter(adapter);
    }

    public void addOnPackageSelectedListener(OnPackageSelectedListener listener) {
        mListeners.add(listener);
    }

    public void removeOnPackageSelectedListener(OnPackageSelectedListener listener) {
        mListeners.remove(listener);
    }
}
