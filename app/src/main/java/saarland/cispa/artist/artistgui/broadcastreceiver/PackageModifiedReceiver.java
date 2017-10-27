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

package saarland.cispa.artist.artistgui.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class PackageModifiedReceiver extends BroadcastReceiver {

    public static final String PACKAGE_NAME_PREFIX = "package:";

    private OnPackageModifiedListener mListener;

    public PackageModifiedReceiver(@NonNull OnPackageModifiedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getDataString().replace(PACKAGE_NAME_PREFIX, "");
        switch (intent.getAction()) {
            case Intent.ACTION_PACKAGE_ADDED:
                mListener.onPackageInstalled(packageName);
                break;
            case Intent.ACTION_PACKAGE_REMOVED:
                mListener.onPackageRemoved(packageName);
                break;
        }
    }

}