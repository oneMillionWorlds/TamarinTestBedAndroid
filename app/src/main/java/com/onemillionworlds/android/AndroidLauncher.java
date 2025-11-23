package com.onemillionworlds.android;

import android.os.Bundle;

import com.onemillionworlds.game.TamarinTestBedAndroid;
import com.jme3.app.AndroidHarness;


public class AndroidLauncher extends AndroidHarness {

    public AndroidLauncher() {
        appClass = TamarinTestBedAndroid.class.getCanonicalName();
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


}
