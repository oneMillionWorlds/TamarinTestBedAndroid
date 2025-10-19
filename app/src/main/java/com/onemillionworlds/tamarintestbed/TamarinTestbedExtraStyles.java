package com.onemillionworlds.tamarintestbed;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.Styles;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;

/**
 * Java port of tamarintestbed-extra-styles.groovy.
 */
public final class TamarinTestbedExtraStyles {

    private TamarinTestbedExtraStyles() {}

    public static void initialize(AssetManager assets) {
        Styles styles = GuiGlobals.getInstance().getStyles();

        TbtQuadBackgroundComponent borderedContainer = TbtQuadBackgroundComponent.create(
                assets.loadTexture(new TextureKey("tamarintestbed/uitextures/bordered-container.png", false)),
                1, 1, 1, 126, 126,
                1f, false);

        Attributes solidBackground = styles.getSelector("solidbackground", "glass");
        solidBackground.set("background", borderedContainer.clone());
    }
}
