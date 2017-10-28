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
import android.os.Build;
import android.support.v4.app.Fragment;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private Intent mIntent;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        mPresenter = new MainActivityPresenter(null, null, mView, mSettingsManager);
    }

    @Test
    public void incompatibilityTest() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), Build.VERSION_CODES.LOLLIPOP);
        mPresenter.checkCompatibility();
        verify(mView).onIncompatibleAndroidVersion();
    }

    @Test
    public void startActivityWithoutIntent() throws Exception {
        when(mIntent.hasExtra(MainActivity.EXTRA_PACKAGE)).thenReturn(false);
        mPresenter.processIntent(mIntent);

        verify(mView).onFragmentSelected(mArgumentCaptor.capture());
        assertTrue(mArgumentCaptor.getValue() instanceof InfoFragment);
    }

    @Test
    public void startActivityWithIntent() throws Exception {
        when(mIntent.hasExtra(MainActivity.EXTRA_PACKAGE)).thenReturn(true);
        mPresenter.processIntent(mIntent);

        verify(mView).onFragmentSelected(mArgumentCaptor.capture());
        assertTrue(mArgumentCaptor.getValue() instanceof AppListFragment);
    }

    @Test
    public void selectInfoFragment() throws Exception {
        mPresenter.selectFragment(MainActivityPresenter.INFO_FRAGMENT);
        verify(mView).onFragmentSelected(mArgumentCaptor.capture());
        assertTrue(mArgumentCaptor.getValue() instanceof InfoFragment);
    }

    @Test
    public void selectCompileFragment() throws Exception {
        mPresenter.selectFragment(MainActivityPresenter.COMPILATION_FRAGMENT);
        verify(mView).onFragmentSelected(mArgumentCaptor.capture());
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
