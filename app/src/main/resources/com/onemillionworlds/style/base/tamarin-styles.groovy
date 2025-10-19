import com.simsilica.lemur.*;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.component.*;
import com.jme3.material.RenderState.BlendMode;

def gradient = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/bordered-gradient.png",
                generateMips:false ),
        1, 1, 1, 126, 126,
        1f, false );

def bevel = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/bevel-quad.png",
                generateMips:false ),
        0.125f, 8, 8, 119, 119,
        1f, false );

def border = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/border.png",
                generateMips:false ),
        1, 1, 1, 6, 6,
        1f, false );
def border2 = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/border.png",
                generateMips:false ),
        1, 2, 2, 6, 6,
        1f, false );

def doubleGradient = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );
doubleGradient.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png",
        generateMips:false )

selector( "glass" ) {
    fontSize = 14
}

selector( "label", "glass" ) {
    insets = new Insets3f( 2, 2, 0, 2 );
    color = color(0.5, 0.75, 0.75, 0.85)
}

selector( "container", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "slider", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

def pressedCommand = new Command<Button>() {
    public void execute( Button source ) {
        if( source.isPressed() ) {
            source.move(1, -1, 0);
        } else {
            source.move(-1, 1, 0);
        }
    }
};

def repeatCommand = new Command<Button>() {
    private long startTime;
    private long lastClick;

    public void execute( Button source ) {
        // Only do the repeating click while the mouse is
        // over the button (and pressed of course)
        if( source.isPressed() && source.isHighlightOn() ) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            // After half a second pause, click 8 times a second
            if( elapsedTime > 500 ) {
                if( elapsedTime - lastClick > 125 ) {
                    source.click();

                    // Try to quantize the last click time to prevent drift
                    lastClick = ((elapsedTime - 500) / 125) * 125 + 500;
                }
            }
        } else {
            startTime = System.currentTimeMillis();
            lastClick = 0;
        }
    }
};

def stdButtonCommands = [
        (ButtonAction.Down):[pressedCommand],
        (ButtonAction.Up):[pressedCommand]
];

def sliderButtonCommands = [
        (ButtonAction.Hover):[repeatCommand]
];

selector( "title", "glass" ) {
    color = color(0.8, 0.9, 1, 0.85f)
    highlightColor = color(1, 0.8, 1, 0.85f)
    shadowColor = color(0, 0, 0, 0.75f)
    shadowOffset = new com.jme3.math.Vector3f(2, -2, -1);
    background = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );
    background.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png",
            generateMips:false )
    insets = new Insets3f( 2, 2, 2, 2 );

    buttonCommands = stdButtonCommands;
}


selector( "button", "glass" ) {
    background = gradient.clone()
    color = color(0.8, 0.9, 1, 0.85f)
    background.setColor(color(0, 0.75, 0.75, 0.5))
    insets = new Insets3f( 2, 2, 2, 2 );

    buttonCommands = stdButtonCommands;
}

selector( "slider", "glass" ) {
    insets = new Insets3f( 1, 3, 1, 2 );
}

selector( "slider", "button", "glass" ) {
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    insets = new Insets3f( 0, 0, 0, 0 );
}

selector( "slider.thumb.button", "glass" ) {
    text = "[]"
    color = color(0.6, 0.8, 0.8, 0.85)
}

selector( "slider.left.button", "glass" ) {
    text = "-"
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    background.setMargin(5, 0);
    color = color(0.6, 0.8, 0.8, 0.85)

    buttonCommands = sliderButtonCommands;
}

selector( "slider.right.button", "glass" ) {
    text = "+"
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    background.setMargin(4, 0);
    color = color(0.6, 0.8, 0.8, 0.85)

    buttonCommands = sliderButtonCommands;
}

selector( "slider.up.button", "glass" ) {
    buttonCommands = sliderButtonCommands;
}

selector( "slider.down.button", "glass" ) {
    buttonCommands = sliderButtonCommands;
}

selector( "checkbox", "glass" ) {
    def on = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-on.png", 1f,
            0, 0, 1f, false );
    on.setColor(color(0.5, 0.9, 0.9, 0.9))
    on.setMargin(5, 0);
    def off = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-off.png", 1f,
            0, 0, 1f, false );
    off.setColor(color(0.6, 0.8, 0.8, 0.8))
    off.setMargin(5, 0);

    onView = on;
    offView = off;

    color = color(0.8, 0.9, 1, 0.85f)
}

selector( "rollup", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "tabbedPanel", "glass" ) {
    activationColor = color(0.8, 0.9, 1, 0.85f)
}

selector( "tabbedPanel.container", "glass" ) {
    background = null
}

selector( "tab.button", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
    color = color(0.4, 0.45, 0.5, 0.85f)
    insets = new Insets3f( 4, 2, 0, 2 );

    buttonCommands = stdButtonCommands;
}

def transparent = new QuadBackgroundComponent(color(0, 0, 0, 0))


selector( "optionPanel", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "optionPanel.container", "glass" ) {

    background = gradient.clone()
    background.color = color(0.25, 0.4, 0.6, 0.25)
    background.setMargin(10, 10)
    insets = new Insets3f( 2, 2, 2, 2 )
}

selector( "title.label", "glass" ) {
    color = color(0.8, 0.9, 1, 0.85f)
    highlightColor = color(1, 0.8, 1, 0.85f)
    shadowColor = color(0, 0, 0, 0.75f)
    shadowOffset = new com.jme3.math.Vector3f(2, -2, -1);
    background = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );
    background.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png",
            generateMips:false )
    insets = new Insets3f( 2, 2, 2, 2 );
}

selector( "list.container", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "list.item", "glass" ) {
    color = color(0.5, 0.75, 0.75, 0.85)
    background = transparent;
}

selector( "list.selector", "glass" ) {
    background = gradient.clone();
    background.color = color(0.4, 0.6, 0.6, 0.5)
    //background.material.material.additionalRenderState.blendMode = BlendMode.Exclusion;
    background.material.material.additionalRenderState.blendMode = BlendMode.AlphaAdditive;
}

selector( "colorChooser.value", "glass" ) {
    border = gradient.clone()
    border.setColor(color(0.25, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "colorChooser.colors", "glass" ) {
    border = gradient.clone()
    border.setColor(color(0.25, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "selector.container", "glass" ) {
    color = color(0.8, 0.9, 1, 0.85f)
    background = gradient.clone()
    background.setColor(color(0, 0.6, 0.6, 0.5))
}

selector( "selector.item", "glass" ) {
    color = color(0.8, 0.8, 0.9, 0.9)
    background = transparent;
    insets = new Insets3f(1, 1, 1, 1, 1, 1);
}

selector( "selector.popup", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0, 0.75, 0.75, 0.75))
}

selector( "selector.down.button", "glass" ) {
    insets = new Insets3f(0, 0, 0, 0, 0, 0);
}


selector( "spinner.value", "glass" ) {
    color = color(0.8, 0.8, 0.9, 0.9)
    background = gradient.clone();
    background.color = color(0, 0.2, 0.2, 0.75);
}

selector( "spinner.buttons.container", "glass" ) {
    background = transparent;
    insets = new Insets3f(0, 0, 0, 0);
}

selector( "spinner", "button", "glass" ) {
    background = gradient.clone()
    // A negative margin works here when the font can support it.
    // It helps eat up the extra whitespace above/below the '+'/'-' signs.
    background.setMargin(2, -3.5);
    background.setColor(color(0, 0.75, 0.75, 0.5))
    insets = new Insets3f(0, 0, 0, 0);
    textHAlignment = HAlignment.Center;
}