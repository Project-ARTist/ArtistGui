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

package saarland.cispa.artist.artistgui.modules.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import saarland.cispa.artist.artistgui.Application;
import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.database.ModuleDao;

public class ModuleListLoader extends AsyncTaskLoader<List<Module>> {

    private ModuleDao mModuleDao;
    private List<Module> mCachedResult;

    public ModuleListLoader(Context context) {
        super(context);
        Application appContext = (Application) getContext();
        mModuleDao = appContext.getDatabase().moduleDao();
    }

    @Override
    protected void onStartLoading() {
        if (mCachedResult != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mCachedResult);
        }

        if (takeContentChanged() || mCachedResult == null) {
            forceLoad();
        }
    }

    @Override
    public List<Module> loadInBackground() {
        return mModuleDao.getAll();
    }

    @Override
    public void deliverResult(List<Module> data) {
        mCachedResult = data;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        mCachedResult = null;
    }
}
