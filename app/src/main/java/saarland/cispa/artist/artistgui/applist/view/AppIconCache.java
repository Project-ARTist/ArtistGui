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

package saarland.cispa.artist.artistgui.applist.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;

import saarland.cispa.artist.artistgui.Package;

class AppIconCache extends LruCache<Package, Drawable> {

    private static final int MAX_SIZE = 15;
    private static final Package sDefaultIconPackage =
            new Package("Default_Icon", "default_app_icon", 0);

    private Context mContext;

    AppIconCache(Context context) {
        super(MAX_SIZE);
        mContext = context;
    }

    @Override
    protected Drawable create(Package packageEntry) {
        if (packageEntry.equals(sDefaultIconPackage)) {
            return mContext.getDrawable(android.R.mipmap.sym_def_app_icon);
        } else if (packageEntry.getAppIconId() == android.R.mipmap.sym_def_app_icon) {
            return get(sDefaultIconPackage);
        }

        Drawable appIcon = null;
        try {
            Context mdpiContext = mContext.createPackageContext(packageEntry.getPackageName(),
                    Context.CONTEXT_IGNORE_SECURITY);
            appIcon = mdpiContext.getResources().getDrawableForDensity(packageEntry.getAppIconId(),
                    DisplayMetrics.DENSITY_XHIGH, null);
            put(packageEntry, appIcon);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appIcon;
    }
}
