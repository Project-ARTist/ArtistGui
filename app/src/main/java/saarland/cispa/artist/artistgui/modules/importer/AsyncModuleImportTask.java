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

package saarland.cispa.artist.artistgui.modules.importer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import saarland.cispa.artist.artistgui.Application;
import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.database.ModuleDao;
import saarland.cispa.artist.artistgui.modules.ModuleContract;

public class AsyncModuleImportTask extends AsyncTask<InputStream, Void, List<Module>> {

    private static final String MODULES_DIR = "modules";

    private Application mApplication;
    private WeakReference<ModuleContract.View> mWeakView;

    public AsyncModuleImportTask(Application application, ModuleContract.View view) {
        this.mApplication = application;
        this.mWeakView = new WeakReference<>(view);
    }

    @Override
    protected List<Module> doInBackground(InputStream... inputStreams) {
        File modulesDir = mApplication.getDir(MODULES_DIR, Context.MODE_PRIVATE);
        File cacheDir = mApplication.getCacheDir();
        ModuleImporter moduleImporter = new ModuleImporter(modulesDir, cacheDir);
        List<Module> moduleList = moduleImporter.importModules(inputStreams);

        // Add to db
        ModuleDao moduleDao = mApplication.getDatabase().moduleDao();
        for (Module module : moduleList) {
            if (module.isUpdating) {
                moduleDao.update(module);
            } else {
                moduleDao.insertAll(module);
            }
        }

        return moduleList;
    }

    @Override
    protected void onPostExecute(List<Module> modules) {
        ModuleContract.View view;
            if ((view = mWeakView.get()) != null) {
                view.addModules(modules);
            }
    }
}
