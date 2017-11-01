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

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import saarland.cispa.artist.artistgui.applist.AppListFragment;
import saarland.cispa.artist.artistgui.settings.manager.SettingsManager;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MainActivityPresenterTest {

    private MainActivityPresenter mPresenter;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<Fragment> mArgumentCaptor;

    @Mock
    private MainActivityContract.View mView;

    @Mock
    private SettingsManager mSettingsManager;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        mPresenter = new MainActivityPresenter(mView, mSettingsManager);
    }

    @Test
    public void incompatibilityTest() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), Build.VERSION_CODES.LOLLIPOP);
        mPresenter.checkCompatibility();
        verify(mView).showIncompatibleVersionDialog();
    }

    @Test
    public void compatibilityTest() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), Build.VERSION_CODES.N);
        mPresenter.checkCompatibility();
        verify(mView, never()).showIncompatibleVersionDialog();
    }

    @Test
    public void selectInfoFragment() throws Exception {
        mPresenter.selectFragment(MainActivityPresenter.INFO_FRAGMENT);
        verify(mView).showSelectedFragment(mArgumentCaptor.capture());
        assertTrue(mArgumentCaptor.getValue() instanceof InfoFragment);
    }

    @Test
    public void selectAppListFragment() throws Exception {
        mPresenter.selectFragment(MainActivityPresenter.INSTRUMENTATION_FRAGMENT);
        verify(mView).showSelectedFragment(mArgumentCaptor.capture());
        assertTrue(mArgumentCaptor.getValue() instanceof AppListFragment);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
