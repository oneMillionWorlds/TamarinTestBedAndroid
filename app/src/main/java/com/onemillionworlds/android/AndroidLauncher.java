package com.onemillionworlds.android;

import com.onemillionworlds.game.TamarinTestBedAndroid;
import com.jme3.app.AndroidHarness;

public class AndroidLauncher extends AndroidHarness {

    public AndroidLauncher() {
        appClass = TamarinTestBedAndroid.class.getCanonicalName();
    }
}
