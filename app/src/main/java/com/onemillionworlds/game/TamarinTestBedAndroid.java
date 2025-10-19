package com.onemillionworlds.game;


import android.util.Log;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;

import com.jme3.app.StatsAppState;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.audio.AudioListenerState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.onemillionworlds.example.MenuExampleState;
import com.onemillionworlds.example.actions.ActionSets;
import com.onemillionworlds.style.base.AndroidGlassStyles;
import com.onemillionworlds.tamarin.openxr.XrBaseAppState;
import com.onemillionworlds.tamarin.openxr.XrSettings;
import com.onemillionworlds.tamarin.vrhands.VRHandsAppState;
import com.onemillionworlds.tamarintestbed.TamarinTestbedExtraStyles;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import tamarin.android.actions.XrActionAndroidAppState;
import tamarin.android.openxr.XrAndroidAppState;

import static com.onemillionworlds.example.Handspec.handSpec;
import static com.onemillionworlds.example.Manifest.manifest;

public class TamarinTestBedAndroid extends SimpleApplication {

    public TamarinTestBedAndroid() {
        super(new XrAndroidAppState(new XrSettings()),
                new XrActionAndroidAppState(manifest(), ActionSets.MAIN),
                new VRHandsAppState(handSpec()),
                //these are just the default JME states (that we have to explicitly select because of using the constructor that takes states)
                new StatsAppState(),
                new ConstantVerifierState(),
                new AudioListenerState(),
                new DebugKeysAppState());
    }

    @Override
    public void simpleInitApp() {

        getViewPort().setBackgroundColor(ColorRGBA.Brown);

        XrBaseAppState vrAppState = getStateManager().getState(XrBaseAppState.ID, XrBaseAppState.class);

        vrAppState.movePlayersFeetToPosition(new Vector3f(0,0,10));
        vrAppState.playerLookAtPosition(new Vector3f(0,0,0));

        getStateManager().attach(new MenuExampleState());

        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        AndroidGlassStyles.initialize(assetManager);
        TamarinTestbedExtraStyles.initialize(assetManager);
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        getCamera().setFrustumPerspective(120f, (float)cam.getWidth() / cam.getHeight(), 0.01f, 1000f);

        XrBaseAppState xrAppState = getStateManager().getState(XrBaseAppState.ID, XrBaseAppState.class);
        xrAppState.runAfterInitialisation(() -> Log.i("TamarinTestBed", "System is: "+xrAppState.getSystemName()));
        xrAppState.setMainViewportConfiguration(vp -> {
            vp.setBackgroundColor(ColorRGBA.Brown);
        });


        //set up some lights to make the hands look better
        rootNode.addLight(new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal(), new ColorRGBA(0.6f, 0.6f, 0.4f, 1f)));
        rootNode.addLight(new AmbientLight(new ColorRGBA(0.1f, 0.1f, 0.1f, 1f)));
        rootNode.addLight(new DirectionalLight(new Vector3f(0, -1, 1).normalizeLocal(), new ColorRGBA(0.5f, 0.45f, 0.5f, 1f)));

    }
}
