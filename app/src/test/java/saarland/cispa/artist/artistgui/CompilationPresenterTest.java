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


import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import saarland.cispa.artist.artistgui.compilation.CompilationContract;
import saarland.cispa.artist.artistgui.compilation.CompilationPresenter;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompilationPresenterTest {

    private static final String INVALID_CODE_LIB = "-1";
    private static final String VALID_CODE_LIB = "Test CodeLib";

    @Captor
    private ArgumentCaptor<Boolean> mBoolArgCaptor;

    @Captor
    private ArgumentCaptor<String> mStringArgCaptor;

    private CompilationPresenter mPresenter;

    @Mock
    private CompilationContract.View mView;

    @Mock
    private SettingsManager mSettingsManager;

    @Mock
    private Intent mIntent;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        mPresenter = new CompilationPresenter(null, mView, mSettingsManager);
    }

    @Test
    public void nullCodeLibChosenTest() throws Exception {
        when(mSettingsManager.getSelectedCodeLib()).thenReturn(null);
        when(mSettingsManager.shouldInjectCodeLib()).thenReturn(true);

        mPresenter.checkIfCodeLibIsChosen();

        verify(mView).showNoCodeLibChosenMessage();
    }

    @Test
    public void invalidCodeLibChosenTest() throws Exception {
        when(mSettingsManager.getSelectedCodeLib()).thenReturn(INVALID_CODE_LIB);
        when(mSettingsManager.shouldInjectCodeLib()).thenReturn(true);

        mPresenter.checkIfCodeLibIsChosen();

        verify(mView).showNoCodeLibChosenMessage();
    }

    @Test
    public void validCodeLibChosenTest() throws Exception {
        when(mSettingsManager.getSelectedCodeLib()).thenReturn(VALID_CODE_LIB);
        when(mSettingsManager.shouldInjectCodeLib()).thenReturn(true);

        mPresenter.checkIfCodeLibIsChosen();

        verify(mView, never()).showNoCodeLibChosenMessage();
    }
}
