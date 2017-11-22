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

import android.arch.persistence.room.Room;

import saarland.cispa.artist.artistgui.database.AppDatabase;
import saarland.cispa.artist.artistgui.utils.LogA;

public class Application extends android.app.Application {

    public static final String DATABASE_NAME = "app-database";
    private AppDatabase mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        LogA.setUserLogLevel(this);
    }

    public AppDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME).build();
        }
        return mDatabase;
    }
}
