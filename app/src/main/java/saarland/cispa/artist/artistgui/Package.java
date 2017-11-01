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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Comparator;

public class Package implements Parcelable {

    public static final Comparator<Package> sComparator =
            (p1, p2) -> p1.getAppName().compareTo(p2.getAppName());

    private String appName;
    private String packageName;
    private int appIconId;
    private long lastInstrumentationTimestamp;
    private boolean keepInstrumented;

    public Package(@NonNull String packageName) {
        this.packageName = packageName;
    }

    public Package(@NonNull String packageName, long lastInstrumentationTimestamp,
                   boolean keepInstrumented) {
        this.packageName = packageName;
        this.lastInstrumentationTimestamp = lastInstrumentationTimestamp;
        this.keepInstrumented = keepInstrumented;
    }

    public Package(@NonNull String appName, @NonNull String packageName, int appIconId) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIconId = appIconId;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getAppIconId() {
        return appIconId;
    }

    public long getLastInstrumentationTimestamp() {
        return lastInstrumentationTimestamp;
    }

    public boolean isKeepInstrumented() {
        return keepInstrumented;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppIconId(int appIconId) {
        this.appIconId = appIconId;
    }

    public void setKeepInstrumented(boolean keepInstrumented) {
        this.keepInstrumented = keepInstrumented;
    }

    public void updateLastInstrumentationTimestamp() {
        this.lastInstrumentationTimestamp = System.currentTimeMillis();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeInt(appIconId);
        dest.writeLong(lastInstrumentationTimestamp);
    }

    public static final Parcelable.Creator<Package> CREATOR = new Parcelable.Creator<Package>() {
        public Package createFromParcel(Parcel in) {
            return new Package(in);
        }

        public Package[] newArray(int size) {
            return new Package[size];
        }
    };

    private Package(Parcel in) {
        appName = in.readString();
        packageName = in.readString();
        appIconId = in.readInt();
        lastInstrumentationTimestamp = in.readLong();
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
