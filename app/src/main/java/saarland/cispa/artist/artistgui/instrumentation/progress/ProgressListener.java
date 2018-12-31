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

package saarland.cispa.artist.artistgui.instrumentation.progress;

import androidx.annotation.NonNull;

public interface ProgressListener {
    void prepareReporter();

    void reportProgressStage(@NonNull String packageName, int progress, @NonNull final String stage);

    void reportProgressDetails(@NonNull String packageName, @NonNull final String message);

    void onSuccess(@NonNull String packageName);

    void onFailure(@NonNull String packageName);
}
