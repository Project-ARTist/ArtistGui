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

import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import saarland.cispa.artist.artistgui.database.Module;
import saarland.cispa.artist.artistgui.utils.FileUtils;

public class ModuleImporter {

    private static final String TAG = "ModuleImporter";

    private static final String TEMP_EXTRACTION_DIR = "tmp_extraction";
    private static final String CODELIB_FILE = "codelib.apk";
    private static final String MANIFEST_FILE = "Manifest.json";
    private static final String MODULE_SO_FILE_NAME = "artist-module.so";
    private static final String MODULE_SO_PATH = "lib/%s/" + MODULE_SO_FILE_NAME;

    private static final int EOF = -1;

    // 1MB in Bytes
    private static final int MAX_MANIFEST_SIZE = 1048576;

    private final File mModulesDir;
    private final File mTempExtractionDir;

    private ZipManifestParser.ParseThread mManifestParser;

    public ModuleImporter(@NonNull File outputDir, @NonNull File cacheDir) {
        mModulesDir = outputDir;
        mTempExtractionDir = new File(cacheDir, TEMP_EXTRACTION_DIR);
    }

    public List<Module> importModules(@NonNull InputStream... inputStreams) {
        List<Module> modules = new ArrayList<>();
        Module currentModule;
        for (InputStream inputStream : inputStreams) {
            currentModule = importModule(inputStream);
            if (currentModule != null) {
                modules.add(currentModule);
            }
        }
        return modules;
    }

    private Module importModule(@NonNull InputStream inputStream) {
        try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
            mManifestParser = new ZipManifestParser.ParseThread();

            if (extractModule(zipStream)) {
                mManifestParser.join();
                Module module = mManifestParser.getResult();

                File importedModuleDst = new File(mModulesDir, module.packageName);
                if (importedModuleDst.exists()) {
                    FileUtils.delete(importedModuleDst);
                    module.isUpdating = true;
                }

                mTempExtractionDir.renameTo(importedModuleDst);
                return module;
            }

        } catch (IOException | InterruptedException e) {
            // TODO: Proper error handling
            e.printStackTrace();
        }
        return null;
    }



    private boolean extractModule(ZipInputStream zipInputStream) {
        if (mTempExtractionDir.exists()) {
            FileUtils.delete(mTempExtractionDir);
            Log.d(TAG, "Deleted old module.");
        }

        try {
            if (mTempExtractionDir.mkdir()) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    switch (entryName) {
                        case CODELIB_FILE:
                            File file = new File(mTempExtractionDir, CODELIB_FILE);
                            extractZipEntry(zipInputStream, file);
                            break;
                        case MANIFEST_FILE:
                            int size = ((Long)entry.getSize()).intValue();
                            if (size > MAX_MANIFEST_SIZE) {
                                throw new IllegalStateException("Manifest.json size is more " +
                                        "than a megabyte.");
                            }
                            String manifestString = readManifestIntoString(zipInputStream, size);
                            mManifestParser.prepare(manifestString);
                            mManifestParser.start();
                            break;
                        default:
                            if (entryName.contains(MODULE_SO_FILE_NAME)) {
                                // Currently we do only support 32bit
                                List<String> deviceSupportedArchs = Arrays
                                        .asList(Build.SUPPORTED_32_BIT_ABIS);
                                String arch;
                                for (String devArch : deviceSupportedArchs) {
                                    arch = String.format(MODULE_SO_PATH, devArch);
                                    if (arch.equals(entryName)) {
                                        File file2 = new File(mTempExtractionDir, MODULE_SO_FILE_NAME);
                                        extractZipEntry(zipInputStream, file2);
                                    }
                                }
                            }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void extractZipEntry(ZipInputStream inputStream, File destination)
            throws IOException {
        FileOutputStream outputStream = new FileOutputStream(destination);
        byte[] buffer = new byte[1024];
        int numberOfReadBytes;
        while ((numberOfReadBytes = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, numberOfReadBytes);
        }
        outputStream.close();
    }

    private static String readManifestIntoString(ZipInputStream zipInputStream, int size)
            throws IOException {
        ByteBuffer result = ByteBuffer.allocate(size);
        int resultBufferSize = 0;

        int bufferSize = 1024;
        byte[] buffer = new byte[size < bufferSize ? size : bufferSize];
        int readBytesCount;
        while ((readBytesCount = zipInputStream.read(buffer)) != EOF) {
            result.put(buffer, resultBufferSize, readBytesCount);
            resultBufferSize += readBytesCount;
        }
        return new String(result.array(), 0, resultBufferSize, "UTF-8");
    }
}
