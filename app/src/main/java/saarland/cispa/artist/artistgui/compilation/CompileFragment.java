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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import saarland.cispa.artist.artistgui.R;
import saarland.cispa.artist.artistgui.packagelist.view.PackageListView;
import saarland.cispa.artist.android.GuiUtils;

public class CompileFragment extends Fragment implements CompilationContract.View,
        PackageListView.OnPackageSelectedListener {

    private CompilationContract.Presenter mPresenter;
    private PackageListView mPackageListView;

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mPresenter.getCompileServiceConnection());
    }

    @Override
    public void setPresenter(CompilationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mPackageListView = (PackageListView) inflater
                .inflate(R.layout.fragment_package_list, container, false);
        mPackageListView.addOnPackageSelectedListener(this);
        return mPackageListView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mPresenter.checkIfCodeLibIsChosen();
        }
    }

    @Override
    public void onPackageSelected(String packageName) {
        mPresenter.queueCompilation(packageName);
    }

    @Override
    public void showNoCodeLibChosenMessage() {
        GuiUtils.displaySnackForever(mPackageListView, getString(R.string.no_codelib_chosen));
    }

    @Override
    public void showCompilationResult(boolean isSuccess, String packageName) {
        int stringResourceId = isSuccess ? R.string.snack_compilation_success :
                R.string.snack_compilation_failed;
        String userMessage = getResources().getString(stringResourceId) + packageName;
        GuiUtils.displaySnackLong(mPackageListView, userMessage);
    }
}
