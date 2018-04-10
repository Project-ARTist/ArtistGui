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

package saarland.cispa.artist.artistgui.modules;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import saarland.cispa.artist.artistgui.Application;
import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.modules.importer.AsyncModuleImportTask;
import saarland.cispa.artist.artistgui.modules.importer.AsyncModuleRemovalTask;

public class ModulePresenter implements ModuleContract.Presenter {

    private Application mApplication;
    private ModuleContract.View mView;

    public ModulePresenter(Context context, ModuleContract.View view) {
        mApplication = (Application) context.getApplicationContext();
        mView = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }


    @Override
    public void addModule(Intent fileChooserIntent) {
        List<Uri> uris = extractUris(fileChooserIntent);
        if (uris.isEmpty()) {
            mView.moduleImportFailed();
            return;
        }

        InputStream[] inputStreams = urisToInputStreams(uris);
        new AsyncModuleImportTask(mApplication, mView)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, inputStreams);
    }

    @Override
    public void removeModule(Module module) {
        new AsyncModuleRemovalTask(mApplication, mView)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, module);
    }

    private List<Uri> extractUris(Intent intent) {
        List<Uri> result = new ArrayList<>();
        Uri uri = intent.getData();

        if (uri != null) {
            result.add(uri);
        } else {
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                ClipData.Item item;
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    item = clipData.getItemAt(i);
                    uri = item.getUri();
                    result.add(uri);
                }
            }
        }
        return result;
    }

    private InputStream[] urisToInputStreams(List<Uri> uris) {
        InputStream[] result = new InputStream[uris.size()];
        ContentResolver contentResolver = mApplication.getContentResolver();
        Uri uri;
        for (int i = 0; i < uris.size(); i++) {
            uri = uris.get(i);
            try {
                result[i] = contentResolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                mView.moduleImportFailed();
            }
        }
        return result;
    }

}
