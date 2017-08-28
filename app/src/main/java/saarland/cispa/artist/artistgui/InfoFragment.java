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

package saarland.cispa.artist.artistgui;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import saarland.cispa.artist.utils.StringUtils;
import trikita.log.Log;

public class InfoFragment extends Fragment {

    private static final String TAG = "InfoFragment";

    private static final String VERSION_TEXTVIEW_STATE_KEY = "version_textview";

    private TextView mTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        mTextView = (TextView) view.findViewById(R.id.main_activity_textview);

        if (savedInstanceState == null) {
            setupTextView();
        } else {
            String versionText = savedInstanceState.getString(VERSION_TEXTVIEW_STATE_KEY);
            mTextView.setText(versionText);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(VERSION_TEXTVIEW_STATE_KEY, mTextView.getText().toString());
    }

    private void setupTextView() {
        StringBuilder artistGuiVersion = new StringBuilder();
        StringBuilder artistVersion = new StringBuilder();

        try {
            AssetManager assetMan = getActivity().getAssets();
            final String[] rootAssets = assetMan.list("");
            for (final String asset : rootAssets) {
                if (asset.startsWith("VERSION_ARTIST-")) {
                    InputStream artistVersionIs = assetMan.open(asset);
                    artistVersion.append("Artist ApiLevel: ")
                            .append(asset)
                            .append(" \n")
                            .append(StringUtils.readIntoString(artistVersionIs))
                            .append("\n");
                } else if (asset.startsWith("VERSION_ARTISTGUI.md")) {
                    InputStream guiVersionIs = assetMan.open(asset);
                    artistGuiVersion.append(StringUtils.readIntoString(guiVersionIs))
                            .append("\n");
                }
            }

        } catch (final IOException e) {
            Log.e(TAG, "Could not read Artist Version files from assets.", e);
        }

        mTextView.append("\n\n");
        mTextView.append("ArtistGUI Version:\n");
        mTextView.append(artistGuiVersion.toString());
        mTextView.append("\n\n");
        mTextView.append("Artist Dex2oat Versions:\n");
        mTextView.append(artistVersion.toString());
        mTextView.setMovementMethod(new ScrollingMovementMethod());
    }
}
