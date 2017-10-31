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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import saarland.cispa.artist.artistgui.Package;
import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.appdetails.AppDetailsDialog;
import saarland.cispa.artist.artistgui.applist.view.AppListView;
import saarland.cispa.artist.artistgui.utils.GuiUtils;

public class AppListFragment extends Fragment implements AppListContract.View,
        AppListView.OnPackageSelectedListener {

    private AppListContract.Presenter mPresenter;
    private AppListView mAppListView;

    @Override
    public void setPresenter(AppListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mAppListView = (AppListView) inflater
                .inflate(R.layout.fragment_package_list, container, false);
        mAppListView.addOnPackageSelectedListener(this);
        return mAppListView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mPresenter.checkIfCodeLibIsChosen();
        }
    }

    @Override
    public void onPackageSelected(Package selectedPackage) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppDetailsDialog.PACKAGE_KEY, selectedPackage);

        AppDetailsDialog detailsDialog = new AppDetailsDialog();
        detailsDialog.setArguments(bundle);
        detailsDialog.show(getFragmentManager(), AppDetailsDialog.TAG);
    }

    @Override
    public void showNoCodeLibChosenMessage() {
        GuiUtils.displaySnackForever(mAppListView, getString(R.string.no_codelib_chosen));
    }
}
