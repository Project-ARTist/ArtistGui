/*
 * The ARTist Project (https://artist.cispa.saarland)
 * <p>
 * Copyright (C) 2017 CISPA (https://cispa.saarland), Saarland University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author "Oliver Schranz <oliver.schranz@cispa.saarland>"
 * @author "Sebastian Weisgerber <weisgerber@cispa.saarland>"
 */
package saarland.cispa.artist.artistgui.progress;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import saarland.cispa.artist.artistgui.R;

public class ProgressDialogFragment extends DialogFragment implements ProgressContract.View {

    public static final String TAG = "ProgressDialogFragment";

    private static final String STATE_KEY_STAGE_TEXT = "stage_text";
    private static final String STATE_KEY_DETAILS_TEXT = "details_text";

    private ProgressContract.Presenter mPresenter;
    private TextView mStageTextView;
    private TextView mDetailsTextView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mPresenter == null) {
            setPresenter(new ProgressPresenter(getContext(), this));
            mPresenter.start();
        }
    }

    @Override
    public void setPresenter(ProgressContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.destroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_progress, container, false);

        mStageTextView = rootView.findViewById(R.id.stage_status);
        mDetailsTextView = rootView.findViewById(R.id.status_extended);
        mDetailsTextView.setMovementMethod(new ScrollingMovementMethod());

        Button cancel = rootView.findViewById(R.id.button_compile_cancel);
        cancel.setOnClickListener(onClickListener -> mPresenter.cancelInstrumentation());
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_KEY_STAGE_TEXT, mStageTextView.getText().toString());
        outState.putString(STATE_KEY_DETAILS_TEXT, mDetailsTextView.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String stage = savedInstanceState.getString(STATE_KEY_STAGE_TEXT);
            mStageTextView.setText(stage);

            String details = savedInstanceState.getString(STATE_KEY_DETAILS_TEXT);
            mDetailsTextView.setText(details);
        }
    }

    @Override
    public void onProgressStageChanged(int progress, @NonNull String packageName,
                                       @NonNull String stage) {
        String textToShow = String.format(Locale.US, "%s: %s (%d)", packageName, stage, progress);
        mStageTextView.setText(textToShow);
    }

    @Override
    public void onProgressDetailChanged(@NonNull String packageName, @NonNull String message) {
        mDetailsTextView.append("> " + message + "\n");
    }

    @Override
    public void onFinishedInstrumentation() {
        dismiss();
    }
}
