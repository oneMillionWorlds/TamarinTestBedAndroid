package com.onemillionworlds.style.base;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.style.Styles;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.Button.ButtonAction;
import com.jme3.texture.Texture;

import java.util.*;

/**
 * Java port of the former Groovy style definitions
 * This avoids Groovy on Android while keeping identical stylings.
 */
public final class AndroidGlassStyles {

    private AndroidGlassStyles() {}

    public static void initialize(AssetManager assets) {
        // Ensure base glass style is present

        Styles styles = GuiGlobals.getInstance().getStyles();

        // Textures and reusable components
        Texture gradientTex = assets.loadTexture(new TextureKey("com/simsilica/lemur/icons/bordered-gradient.png", false));
        TbtQuadBackgroundComponent gradient = TbtQuadBackgroundComponent.create(
                gradientTex,
                1, 1, 1, 126, 126,
                1f, false);

        Texture bevelTex = assets.loadTexture(new TextureKey("com/simsilica/lemur/icons/bevel-quad.png", false));
        TbtQuadBackgroundComponent bevel = TbtQuadBackgroundComponent.create(
                bevelTex,
                0.125f, 8, 8, 119, 119,
                1f, false);

        Texture borderTex = assets.loadTexture(new TextureKey("com/simsilica/lemur/icons/border.png", false));
        TbtQuadBackgroundComponent border = TbtQuadBackgroundComponent.create(
                borderTex,
                1, 1, 1, 6, 6,
                1f, false);
        TbtQuadBackgroundComponent border2 = TbtQuadBackgroundComponent.create(
                borderTex,
                1, 2, 2, 6, 6,
                1f, false);

        QuadBackgroundComponent doubleGradient = new QuadBackgroundComponent(new ColorRGBA(0.5f, 0.75f, 0.85f, 0.5f));
        Texture doubleGradTex = assets.loadTexture(new TextureKey("com/simsilica/lemur/icons/double-gradient-128.png", false));
        doubleGradient.setTexture(doubleGradTex);

        // Commands
        Command<Button> pressedCommand = new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (source.isPressed()) {
                    source.move(1, -1, 0);
                } else {
                    source.move(-1, 1, 0);
                }
            }
        };

        Command<Button> repeatCommand = new Command<Button>() {
            long startTime;
            long lastClick;

            @Override
            public void execute(Button source) {
                if (source.isPressed() && source.isHighlightOn()) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (elapsedTime > 500) {
                        if (elapsedTime - lastClick > 125) {
                            source.click();
                            lastClick = ((elapsedTime - 500) / 125) * 125 + 500;
                        }
                    }
                } else {
                    startTime = System.currentTimeMillis();
                    lastClick = 0;
                }
            }
        };

        Map<ButtonAction, List<Command<Button>>> stdButtonCommands = new HashMap<>();
        stdButtonCommands.put(ButtonAction.Down, Collections.singletonList(pressedCommand));
        stdButtonCommands.put(ButtonAction.Up, Collections.singletonList(pressedCommand));

        Map<ButtonAction, List<Command<Button>>> sliderButtonCommands = new HashMap<>();
        sliderButtonCommands.put(ButtonAction.Hover, Collections.singletonList(repeatCommand));

        QuadBackgroundComponent transparent = new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0));

        // Style: base glass
        styles.getSelector("glass").set("fontSize", 14);

        // label.glass
        Attributes labelGlass = styles.getSelector("label", "glass");
        labelGlass.set("insets", new Insets3f(2, 2, 0, 2));
        labelGlass.set("color", new ColorRGBA(0.5f, 0.75f, 0.75f, 0.85f));

        // container.glass
        Attributes containerGlass = styles.getSelector("container", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            containerGlass.set("background", bg);
        }

        // slider.glass (general background)
        Attributes sliderGlass = styles.getSelector("slider", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            sliderGlass.set("background", bg);
        }

        // title.glass
        Attributes titleGlass = styles.getSelector("title", "glass");
        titleGlass.set("color", new ColorRGBA(0.8f, 0.9f, 1f, 0.85f));
        titleGlass.set("highlightColor", new ColorRGBA(1f, 0.8f, 1f, 0.85f));
        titleGlass.set("shadowColor", new ColorRGBA(0, 0, 0, 0.75f));
        titleGlass.set("shadowOffset", new Vector3f(2, -2, -1));
        {
            QuadBackgroundComponent bg = new QuadBackgroundComponent(new ColorRGBA(0.5f, 0.75f, 0.85f, 0.5f));
            bg.setTexture(doubleGradTex);
            titleGlass.set("background", bg);
        }
        titleGlass.set("insets", new Insets3f(2, 2, 2, 2));
        titleGlass.set("buttonCommands", stdButtonCommands);

        // button.glass
        Attributes buttonGlass = styles.getSelector("button", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0f, 0.75f, 0.75f, 0.5f));
            buttonGlass.set("background", bg);
        }
        buttonGlass.set("color", new ColorRGBA(0.8f, 0.9f, 1f, 0.85f));
        buttonGlass.set("insets", new Insets3f(2, 2, 2, 2));
        buttonGlass.set("buttonCommands", stdButtonCommands);

        // slider.glass insets
        styles.getSelector("slider", "glass").set("insets", new Insets3f(1, 3, 1, 2));

        // slider.button.glass
        Attributes sliderButtonGlass = styles.getSelector("slider", "button", "glass");
        {
            QuadBackgroundComponent bg = doubleGradient.clone();
            bg.setColor(new ColorRGBA(0.5f, 0.75f, 0.75f, 0.5f));
            sliderButtonGlass.set("background", bg);
        }
        sliderButtonGlass.set("insets", new Insets3f(0, 0, 0, 0));

        // slider.thumb.button.glass
        Attributes sliderThumb = styles.getSelector("slider.thumb.button", "glass");
        sliderThumb.set("text", "[]");
        sliderThumb.set("color", new ColorRGBA(0.6f, 0.8f, 0.8f, 0.85f));

        // slider.left.button.glass
        Attributes sliderLeft = styles.getSelector("slider.left.button", "glass");
        sliderLeft.set("text", "-");
        {
            QuadBackgroundComponent bg = doubleGradient.clone();
            bg.setColor(new ColorRGBA(0.5f, 0.75f, 0.75f, 0.5f));
            bg.setMargin(5, 0);
            sliderLeft.set("background", bg);
        }
        sliderLeft.set("color", new ColorRGBA(0.6f, 0.8f, 0.8f, 0.85f));
        sliderLeft.set("buttonCommands", sliderButtonCommands);

        // slider.right.button.glass
        Attributes sliderRight = styles.getSelector("slider.right.button", "glass");
        sliderRight.set("text", "+");
        {
            QuadBackgroundComponent bg = doubleGradient.clone();
            bg.setColor(new ColorRGBA(0.5f, 0.75f, 0.75f, 0.5f));
            bg.setMargin(4, 0);
            sliderRight.set("background", bg);
        }
        sliderRight.set("color", new ColorRGBA(0.6f, 0.8f, 0.8f, 0.85f));
        sliderRight.set("buttonCommands", sliderButtonCommands);

        // slider.up.button.glass and slider.down.button.glass
        styles.getSelector("slider.up.button", "glass").set("buttonCommands", sliderButtonCommands);
        styles.getSelector("slider.down.button", "glass").set("buttonCommands", sliderButtonCommands);

        // checkbox.glass
        Attributes checkboxGlass = styles.getSelector("checkbox", "glass");
        IconComponent on = new IconComponent("/com/simsilica/lemur/icons/Glass-check-on.png", 1f, 0, 0, 1f, false);
        on.setColor(new ColorRGBA(0.5f, 0.9f, 0.9f, 0.9f));
        on.setMargin(5, 0);
        IconComponent off = new IconComponent("/com/simsilica/lemur/icons/Glass-check-off.png", 1f, 0, 0, 1f, false);
        off.setColor(new ColorRGBA(0.6f, 0.8f, 0.8f, 0.8f));
        off.setMargin(5, 0);
        checkboxGlass.set("onView", on);
        checkboxGlass.set("offView", off);
        checkboxGlass.set("color", new ColorRGBA(0.8f, 0.9f, 1f, 0.85f));

        // rollup.glass
        Attributes rollupGlass = styles.getSelector("rollup", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            rollupGlass.set("background", bg);
        }

        // tabbedPanel.glass
        styles.getSelector("tabbedPanel", "glass").set("activationColor", new ColorRGBA(0.8f, 0.9f, 1f, 0.85f));
        // tabbedPanel.container.glass
        styles.getSelector("tabbedPanel.container", "glass").set("background", null);

        // tab.button.glass
        Attributes tabButtonGlass = styles.getSelector("tab.button", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            tabButtonGlass.set("background", bg);
        }
        tabButtonGlass.set("color", new ColorRGBA(0.4f, 0.45f, 0.5f, 0.85f));
        tabButtonGlass.set("insets", new Insets3f(4, 2, 0, 2));
        tabButtonGlass.set("buttonCommands", stdButtonCommands);

        // optionPanel.glass
        Attributes optionPanel = styles.getSelector("optionPanel", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            optionPanel.set("background", bg);
        }
        // optionPanel.container.glass
        Attributes optionPanelContainer = styles.getSelector("optionPanel.container", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.25f, 0.4f, 0.6f, 0.25f));
            bg.setMargin(10, 10);
            optionPanelContainer.set("background", bg);
        }
        optionPanelContainer.set("insets", new Insets3f(2, 2, 2, 2));

        // title.label.glass
        Attributes titleLabel = styles.getSelector("title.label", "glass");
        titleLabel.set("color", new ColorRGBA(0.8f, 0.9f, 1f, 0.85f));
        titleLabel.set("highlightColor", new ColorRGBA(1f, 0.8f, 1f, 0.85f));
        titleLabel.set("shadowColor", new ColorRGBA(0, 0, 0, 0.75f));
        titleLabel.set("shadowOffset", new Vector3f(2, -2, -1));
        {
            QuadBackgroundComponent bg = new QuadBackgroundComponent(new ColorRGBA(0.5f, 0.75f, 0.85f, 0.5f));
            bg.setTexture(doubleGradTex);
            titleLabel.set("background", bg);
        }
        titleLabel.set("insets", new Insets3f(2, 2, 2, 2));

        // list.container.glass
        Attributes listContainer = styles.getSelector("list.container", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            listContainer.set("background", bg);
        }
        listContainer.set("insets", new Insets3f(2, 2, 2, 2, 2, 2));

        // list.item.glass
        Attributes listItem = styles.getSelector("list.item", "glass");
        listItem.set("color", new ColorRGBA(0.5f, 0.75f, 0.75f, 0.85f));
        listItem.set("background", transparent);

        // list.selector.glass
        Attributes listSelector = styles.getSelector("list.selector", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0.4f, 0.6f, 0.6f, 0.5f));
            // blend mode
            GuiMaterial gm = bg.getMaterial();
            if (gm != null && gm.getMaterial() != null && gm.getMaterial().getAdditionalRenderState() != null) {
                gm.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.AlphaAdditive);
            }
            listSelector.set("background", bg);
        }

        // colorChooser.value.glass
        Attributes ccValue = styles.getSelector("colorChooser.value", "glass");
        {
            TbtQuadBackgroundComponent b = gradient.clone();
            b.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            ccValue.set("border", b);
        }
        ccValue.set("insets", new Insets3f(2, 2, 2, 2, 2, 2));

        // colorChooser.colors.glass
        Attributes ccColors = styles.getSelector("colorChooser.colors", "glass");
        {
            TbtQuadBackgroundComponent b = gradient.clone();
            b.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 0.5f));
            ccColors.set("border", b);
        }
        ccColors.set("insets", new Insets3f(2, 2, 2, 2, 2, 2));

        // selector.container.glass
        Attributes selectorContainer = styles.getSelector("selector.container", "glass");
        selectorContainer.set("color", new ColorRGBA(0.8f, 0.9f, 1f, 0.85f));
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0f, 0.6f, 0.6f, 0.5f));
            selectorContainer.set("background", bg);
        }

        // selector.item.glass
        Attributes selectorItem = styles.getSelector("selector.item", "glass");
        selectorItem.set("color", new ColorRGBA(0.8f, 0.8f, 0.9f, 0.9f));
        selectorItem.set("background", transparent);
        selectorItem.set("insets", new Insets3f(1, 1, 1, 1, 1, 1));

        // selector.popup.glass
        Attributes selectorPopup = styles.getSelector("selector.popup", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0f, 0.75f, 0.75f, 0.75f));
            selectorPopup.set("background", bg);
        }

        // selector.down.button.glass
        styles.getSelector("selector.down.button", "glass").set("insets", new Insets3f(0, 0, 0, 0, 0, 0));

        // spinner.value.glass
        Attributes spinnerValue = styles.getSelector("spinner.value", "glass");
        spinnerValue.set("color", new ColorRGBA(0.8f, 0.8f, 0.9f, 0.9f));
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setColor(new ColorRGBA(0f, 0.2f, 0.2f, 0.75f));
            spinnerValue.set("background", bg);
        }

        // spinner.buttons.container.glass
        Attributes spinnerButtonsContainer = styles.getSelector("spinner.buttons.container", "glass");
        spinnerButtonsContainer.set("background", transparent);
        spinnerButtonsContainer.set("insets", new Insets3f(0, 0, 0, 0));

        // spinner.button.glass
        Attributes spinnerButton = styles.getSelector("spinner", "button", "glass");
        {
            TbtQuadBackgroundComponent bg = gradient.clone();
            bg.setMargin(2, -3.5f);
            bg.setColor(new ColorRGBA(0f, 0.75f, 0.75f, 0.5f));
            spinnerButton.set("background", bg);
        }
        spinnerButton.set("insets", new Insets3f(0, 0, 0, 0));
        spinnerButton.set("textHAlignment", HAlignment.Center);
    }
}
