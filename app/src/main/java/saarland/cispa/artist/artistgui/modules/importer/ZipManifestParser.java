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

import org.json.JSONException;
import org.json.JSONObject;

import saarland.cispa.artist.artistgui.database.Module;

class ZipManifestParser {

    public static class ParseThread extends Thread {

        private String mManifestJson;
        private Module mResult;

        public void prepare(String manifestJson) {
            this.mManifestJson = manifestJson;
        }

        @Override
        public void run() {
            if (mManifestJson == null) {
                throw new RuntimeException("Never called ParseThread.prepare().");
            }

            try {
                mResult = parse(mManifestJson);
            } catch (JSONException e) {
                // Ignore exception. mResult will be null.
            }
        }

        public Module getResult() {
            return mResult;
        }
    }

    // JSON element keys
    private static final String PACKAGE_NAME_ELEMENT = "package_name";
    private static final String NAME_ELEMENT = "name";
    private static final String DESCRIPTION_ELEMENT = "description";
    private static final String AUTHOR_ELEMENT = "author";
    private static final String VERSION_ELEMENT = "version";

    private static Module parse(String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);
        String packageName = root.getString(PACKAGE_NAME_ELEMENT);
        String name = root.getString(NAME_ELEMENT);
        String description = root.getString(DESCRIPTION_ELEMENT);
        String author = root.getString(AUTHOR_ELEMENT);
        int version = root.getInt(VERSION_ELEMENT);

        return new Module(packageName, name, description, author, version);
    }
}
