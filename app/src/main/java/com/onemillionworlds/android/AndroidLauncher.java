package com.onemillionworlds.android;

import android.os.Bundle;

import com.jme3.system.AppSettings;
import com.onemillionworlds.game.TamarinTestBedAndroid;
import com.jme3.app.AndroidHarness;


public class AndroidLauncher extends AndroidHarness {

    public AndroidLauncher() {
        appClass = TamarinTestBedAndroid.class.getCanonicalName();
        eglSamples = 2;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((TamarinTestBedAndroid)getJmeApplication()).setAndroidActivity(this);
    }

    /**
     * When the application is stopped (e.g. app.stop()) the jME context is torn down on the GL thread (nulling its
     * input handler) and then the activity is finished, which triggers onPause -> loseFocus. AndroidHarness's
     * loseFocus unconditionally asks the (now destroyed) context for its JoyInput, which throws a
     * NullPointerException. If the context has already been destroyed there is nothing left to pause, so skip it.
     */
    @Override
    public void loseFocus() {
        if (app != null && app.getContext() != null && !app.getContext().isCreated()) {
            if (view != null) {
                view.onPause();
            }
            isGLThreadPaused = true;
            return;
        }
        super.loseFocus();
    }
}
