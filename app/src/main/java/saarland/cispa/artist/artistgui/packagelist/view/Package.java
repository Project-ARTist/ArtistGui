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

import android.support.annotation.DrawableRes;

import java.util.Comparator;

class Package {

    public static final Comparator<Package> sComparator =
            (p1, p2) -> p1.getAppName().compareTo(p2.getAppName());

    private String appName;
    private String packageName;
    private int appIconId;

    Package(String appName, String packageName, @DrawableRes int appIconId) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIconId = appIconId;
    }

    String getAppName() {
        return appName;
    }

    String getPackageName() {
        return packageName;
    }

    int getAppIconId() {
        return appIconId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Package aPackage = (Package) o;
        return appIconId == aPackage.appIconId &&
                appName.equals(aPackage.appName) &&
                packageName.equals(aPackage.packageName);

    }

    @Override
    public int hashCode() {
        int result = appName.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + appIconId;
        return result;
    }
}
