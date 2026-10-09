package com.onemillionworlds.example;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.onemillionworlds.tamarin.actions.HandSide;
import com.onemillionworlds.tamarin.actions.XrActionBaseAppState;
import com.onemillionworlds.tamarin.actions.actionprofile.ActionHandle;
import com.onemillionworlds.tamarin.actions.controllerprofile.MetaHandMicrogestures;
import com.onemillionworlds.tamarin.handgestures.Finger;
import com.onemillionworlds.tamarin.handgestures.HandGestureSourcePreference;
import com.onemillionworlds.tamarin.handgestures.HandGestureState;
import com.onemillionworlds.tamarin.openxr.XrBaseAppState;
import com.onemillionworlds.tamarin.vrhands.VRHandsAppState;
import com.onemillionworlds.tamarin.vrhands.functions.FunctionRegistration;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.onemillionworlds.example.actions.ActionHandles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Demonstrates hand tracking (bare hands, no controllers) gestures by moving cubes around. Each hand controls its own
 * cube (the left hand controls the left cube, the right hand the right cube).
 * <p>
 *     1) <b>META ONLY</b> thumb microgestures (XR_META_hand_tracking_microgestures). These go through the normal action
 *     system: see the MICROGESTURE_* actions in the action manifest, which are bound using
 *     {@link MetaHandMicrogestures}. Each action is bound on both hands, and the state is read per hand by passing
 *     the hand's input string (sub action path) when getting the action state. Raise your thumb above your index finger
 *     (other fingers slightly curled) then:
 * </p>
 * <ul>
 *     <li>swipe the thumb left/right along the index finger to move the cube left/right</li>
 *     <li>swipe the thumb forward/backward across the index finger to move the cube away/towards you</li>
 *     <li>tap the thumb on the side of the index finger to make the cube change colour</li>
 * </ul>
 * <p>
 *     2) Actions with Tamarin gesture bindings (see the action manifest). Each action is bound to both controller
 *     buttons and a hand gesture, and the game code just reads the action as normal (it doesn't know or care which was
 *     used). Tamarin uses the runtime's gesture recognition where available (Meta's XR_FB_hand_tracking_aim) and its own
 *     (worked out from the hand's joints, so it works on any runtime with hand tracking) otherwise:
 * </p>
 * <ul>
 *     <li>GROW_CUBE (float): pinch the thumb and middle finger together (or squeeze the grip) to grow the cube</li>
 *     <li>SPIN_CUBE: pinch the thumb and ring finger together (or hold B/Y) to spin the cube</li>
 *     <li>RESET_CUBE: give a thumbs up (or press X on Touch, A on Index) to reset the cube</li>
 *     <li>TRIGGER: the thumb and index finger pinch. Binding that gesture also suggests the runtime's own pinch binding
 *     (pinch_ext), which is used where the runtime supports it (Quest), otherwise Tamarin detects the pinch</li>
 * </ul>
 * <p>
 *     The button switches the hand gesture source preference. AUTOMATIC uses the runtime's version of a gesture if it
 *     has one and Tamarin's otherwise. RUNTIME_ONLY only uses the runtime's pinches. TAMARIN_ONLY always uses Tamarin's
 *     gestures (while the hand is tracked). This lets Meta's and Tamarin's gestures be compared.
 * </p>
 * <p>
 *     3) {@link XrActionBaseAppState#getHandGestureState(HandSide)}, for things that aren't really actions: the bars.
 * </p>
 * <p>
 *     The bars under each cube show the raw values. The top row compares the runtime's (blue, left of each pair) and
 *     Tamarin's (orange, right of each pair) pinch strength for each finger. The bottom row is Tamarin only gestures.
 *     Bars turn green when the gesture is "on".
 * </p>
 * <p>
 *     In desktop simulation the microgestures are on the numpad (4/6/8/2 to swipe, 5 to tap) and act as the right
 *     hand. Numpad 0 resets, 7 spins and 9 grows. The hand gestures themselves have no simulation.
 * </p>
 */
public class MetaOnlyHandGesturesExampleState extends BaseAppState{

    private static final float SWIPE_STEP = 0.1f;

    /**
     * How long (seconds) after changing the gesture source further clicks on the source button are ignored
     */
    private static final float SOURCE_BUTTON_DEAD_TIME = 0.1f;

    private static final float BAR_MAX_HEIGHT = 0.2f;
    private static final float BAR_WIDTH = 0.02f;
    private static final ColorRGBA RUNTIME_BAR_COLOUR = new ColorRGBA(0.2f, 0.6f, 1f, 1f);
    private static final ColorRGBA TAMARIN_BAR_COLOUR = new ColorRGBA(1f, 0.55f, 0.1f, 1f);
    private static final ColorRGBA BAR_ACTIVE_COLOUR = ColorRGBA.Green;

    private static final ColorRGBA[] CUBE_COLOURS = new ColorRGBA[]{
            ColorRGBA.Orange, ColorRGBA.Cyan, ColorRGBA.Magenta, ColorRGBA.Yellow, ColorRGBA.Green
    };

    Node rootNodeDelegate = new Node("MetaOnlyHandGesturesExampleState");

    XrBaseAppState xrAppState;
    XrActionBaseAppState xrActionAppState;

    Label statusLabel;
    Label lastGestureLabel;
    Button sourceButton;
    float timeSinceSourceChange = Float.MAX_VALUE;

    List<FunctionRegistration> functionRegistrations = new ArrayList<>();

    /**
     * One per hand, each hand only ever affects its own cube.
     */
    List<HandCube> handCubes = new ArrayList<>();

    @Override
    protected void initialize(Application app){
        ((SimpleApplication)app).getRootNode().attachChild(rootNodeDelegate);
        xrAppState = getState(XrBaseAppState.ID, XrBaseAppState.class);
        xrActionAppState = getState(XrActionBaseAppState.ID, XrActionBaseAppState.class);

        Container lemurWindow = new Container();
        lemurWindow.setLocalScale(0.005f); //lemur defaults to 1 meter == 1 pixel (because that make sense for 2D, scale it down, so it's not huge in 3d)
        lemurWindow.addChild(new Label("Bare hands. Each hand -> its own cube.\n\n" +
                "Microgestures (Meta only, thumb raised over index finger):\n" +
                "  Swipe left/right -> move left/right\n" +
                "  Swipe forward/back -> move away/towards\n" +
                "  Tap -> change colour\n\n" +
                "Gestures (any runtime, controller button in brackets):\n" +
                "  Middle pinch (grip) -> grow\n" +
                "  Ring pinch (B/Y) -> spin\n" +
                "  Thumbs up (X, or A on Index) -> reset\n" +
                "  Index pinch -> trigger\n\n" +
                "Button: AUTOMATIC -> Meta if available, else Tamarin.\n" +
                "  RUNTIME_ONLY -> Meta only. TAMARIN_ONLY -> Tamarin only.\n\n" +
                "Top bars -> I M R L pinch, Meta blue, Tamarin orange.\n" +
                "Bottom bars -> Fist, Point, Thumbs up, Palm to you.\n" +
                "Green -> gesture on."));
        statusLabel = lemurWindow.addChild(new Label(""));
        lastGestureLabel = lemurWindow.addChild(new Label("Last microgesture: none"));

        sourceButton = lemurWindow.addChild(new Button(""));
        sourceButton.addClickCommands(source -> {
            // changing the source while pinching changes where the trigger's value comes from mid pinch, which can
            // look like a second click. So ignore clicks for a moment after a change
            if (timeSinceSourceChange < SOURCE_BUTTON_DEAD_TIME){
                return;
            }
            timeSinceSourceChange = 0;
            // cycles AUTOMATIC -> RUNTIME_ONLY -> TAMARIN_ONLY. This applies to everything using hand gestures
            // (including gesture bindings on actions), so the example resets it when it closes
            HandGestureSourcePreference[] preferences = HandGestureSourcePreference.values();
            HandGestureSourcePreference current = xrActionAppState.getHandGestureSourcePreference();
            xrActionAppState.setHandGestureSourcePreference(preferences[(current.ordinal() + 1) % preferences.length]);
            updateSourceButtonText();
        });
        updateSourceButtonText();

        lemurWindow.addChild(new Button("Back to menu")).addClickCommands(source -> {
            getStateManager().detach(this);
            getStateManager().attach(new MenuExampleState());
        });

        lemurWindow.setLocalTranslation(-1.3f, 2.1f, 8.6f);
        rootNodeDelegate.attachChild(lemurWindow);

        handCubes.add(new HandCube(HandSide.LEFT, new Vector3f(-0.3f, 1.1f, 9f), 0));
        handCubes.add(new HandCube(HandSide.RIGHT, new Vector3f(0.3f, 1.1f, 9f), 1));

        VRHandsAppState vrHands = getState(VRHandsAppState.ID, VRHandsAppState.class);
        vrHands.getHandControls().forEach(h -> {
            functionRegistrations.add(h.attachPickLine(pickLine()));
            functionRegistrations.add(h.setPickMarkerContinuous(rootNodeDelegate));
            functionRegistrations.add(h.setClickAction_lemurSupport(ActionHandles.TRIGGER, rootNodeDelegate));
        });

        xrAppState.movePlayersFeetToPosition(new Vector3f(0,0,10));
        xrAppState.playerLookAtPosition(new Vector3f(0,0,0));
    }

    private void updateSourceButtonText(){
        sourceButton.setText("Source: " + xrActionAppState.getHandGestureSourcePreference() + " (click to change)");
    }

    @Override
    public void update(float tpf){
        super.update(tpf);
        timeSinceSourceChange += tpf;

        StringBuilder cubeSources = new StringBuilder();
        StringBuilder handDataSources = new StringBuilder();
        for(HandCube handCube : handCubes){
            handCube.handleMicrogestures();
            handCube.handleCubeActions(tpf);
            Optional<HandGestureState> state = xrActionAppState.getHandGestureState(handCube.handSide);
            handCube.updateCube(tpf);
            handCube.updateBars();
            cubeSources.append(", ").append(handCube.handSide).append(": ").append(state.map(s -> s.getSource().toString()).orElse("not tracked"));
            // whether the hand joints come from the bare hand or a held controller (e.g. Index controllers on SteamVR)
            handDataSources.append(", ").append(handCube.handSide).append(": ")
                    .append(xrActionAppState.getHandTrackingDataSource(handCube.handSide).map(Enum::toString).orElse("not tracked"));
        }

        statusLabel.setText(
                "Microgestures: " + yesNo(xrAppState.checkExtensionLoaded(MetaHandMicrogestures.REQUIRED_EXTENSION)) +
                ". Meta pinches: " + yesNo(xrAppState.checkExtensionLoaded("XR_FB_hand_tracking_aim")) + "\n" +
                "Pinch source" + cubeSources + "\n" +
                "Hand data" + handDataSources);
    }

    private static String yesNo(boolean value){
        return value ? "yes" : "no";
    }

    @Override
    protected void cleanup(Application app){
        // this is a global setting, so don't leave it changed for the rest of the testbed
        xrActionAppState.setHandGestureSourcePreference(HandGestureSourcePreference.AUTOMATIC);
        rootNodeDelegate.removeFromParent();
        functionRegistrations.forEach(FunctionRegistration::endFunction);
    }

    @Override protected void onEnable(){}

    @Override protected void onDisable(){}

    /**
     * A cube, the hand that controls it, and the bars showing the hand's gestures.
     */
    private class HandCube{
        final HandSide handSide;
        final Vector3f startPosition;

        final Geometry cube;
        final Material cubeMaterial;
        int cubeColourIndex;

        /**
         * Where the cube is heading. The cube moves smoothly towards this rather than jumping.
         */
        final Vector3f targetPosition;
        final Vector3f currentPosition;
        float spinAngle = 0;

        final List<Bar> bars = new ArrayList<>();

        HandCube(HandSide handSide, Vector3f startPosition, int startingColourIndex){
            this.handSide = handSide;
            this.startPosition = startPosition;
            this.targetPosition = startPosition.clone();
            this.currentPosition = startPosition.clone();
            this.cubeColourIndex = startingColourIndex;

            cubeMaterial = new Material(getApplication().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
            cubeMaterial.setBoolean("UseMaterialColors", true);
            setCubeColour(CUBE_COLOURS[cubeColourIndex]);
            cube = new Geometry("gestureCube_" + handSide, new Box(0.1f, 0.1f, 0.1f));
            cube.setMaterial(cubeMaterial);
            cube.setLocalTranslation(startPosition);
            rootNodeDelegate.attachChild(cube);

            createBars();
        }

        /**
         * The bars are read directly from getHandGestureState with a forced source, so the runtime's and Tamarin's
         * values can be compared whatever the cube is using.
         */
        private void createBars(){
            // top row: pinches. A pair of bars per finger, runtime (Meta) on the left, Tamarin on the right
            Vector3f pinchRowBottomCentre = startPosition.add(0, -0.5f, 0);
            for(int i = 0; i < Finger.PINCHING_FINGERS.length; i++){
                Finger finger = Finger.PINCHING_FINGERS[i];
                float groupCentreX = (i - 1.5f) * 0.07f;
                addBar(pinchRowBottomCentre.add(groupCentreX - 0.012f, 0, 0), null, RUNTIME_BAR_COLOUR,
                        HandGestureSourcePreference.RUNTIME_ONLY, state -> state.getPinchStrength(finger), state -> state.isPinching(finger));
                addBar(pinchRowBottomCentre.add(groupCentreX + 0.012f, 0, 0), null, TAMARIN_BAR_COLOUR,
                        HandGestureSourcePreference.TAMARIN_ONLY, state -> state.getPinchStrength(finger), state -> state.isPinching(finger));
                addLabel(finger.name().substring(0, 1), pinchRowBottomCentre.add(groupCentreX - 0.008f, -0.01f, 0));
            }

            // bottom row: gestures only Tamarin detects
            Vector3f gestureRowBottomCentre = startPosition.add(0, -0.8f, 0);
            addBar(gestureRowBottomCentre.add(-0.105f, 0, 0), "Fist", TAMARIN_BAR_COLOUR, HandGestureSourcePreference.TAMARIN_ONLY,
                    HandGestureState::getFistStrength, HandGestureState::isFist);
            addBar(gestureRowBottomCentre.add(-0.035f, 0, 0), "Point", TAMARIN_BAR_COLOUR, HandGestureSourcePreference.TAMARIN_ONLY,
                    HandGestureState::getPointStrength, HandGestureState::isPointing);
            addBar(gestureRowBottomCentre.add(0.035f, 0, 0), "Thumb", TAMARIN_BAR_COLOUR, HandGestureSourcePreference.TAMARIN_ONLY,
                    HandGestureState::getThumbsUpStrength, HandGestureState::isThumbsUp);
            // how much the palm faces the head (if this fills when the palm is facing the user the palm direction is right)
            addBar(gestureRowBottomCentre.add(0.105f, 0, 0), "Palm", TAMARIN_BAR_COLOUR, HandGestureSourcePreference.TAMARIN_ONLY,
                    state -> state.palmFacingAmount(xrAppState.getVrCameraPosition().subtract(state.getPalmPosition())),
                    state -> state.palmFacingAmount(xrAppState.getVrCameraPosition().subtract(state.getPalmPosition())) > 0.8f);
        }

        private void addBar(Vector3f bottomCentre, String label, ColorRGBA colour, HandGestureSourcePreference source,
                            Function<HandGestureState, Float> value, Function<HandGestureState, Boolean> active){
            Bar bar = new Bar(colour, source, value, active);
            bar.node.setLocalTranslation(bottomCentre);
            rootNodeDelegate.attachChild(bar.node);
            rootNodeDelegate.attachChild(bar.track);
            bar.track.setLocalTranslation(bottomCentre.add(0, 0, -0.01f));
            bars.add(bar);
            if (label != null){
                addLabel(label, bottomCentre.add(-0.025f, -0.01f, 0));
            }
        }

        private void addLabel(String text, Vector3f topLeft){
            Label label = new Label(text);
            label.setLocalScale(0.0025f);
            label.setLocalTranslation(topLeft);
            rootNodeDelegate.attachChild(label);
        }

        void updateBars(){
            for(Bar bar : bars){
                bar.update(xrActionAppState.getHandGestureState(handSide, bar.source));
            }
        }

        /**
         * Microgestures are ordinary boolean actions, they become true once the gesture is recognised. Using
         * hasChangedToTrue means each gesture moves the cube exactly once.
         */
        void handleMicrogestures(){
            if (actionJustHappened(ActionHandles.MICROGESTURE_SWIPE_LEFT)){
                targetPosition.x -= SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe left");
            }
            if (actionJustHappened(ActionHandles.MICROGESTURE_SWIPE_RIGHT)){
                targetPosition.x += SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe right");
            }
            if (actionJustHappened(ActionHandles.MICROGESTURE_SWIPE_FORWARD)){
                // the player is looking down the negative z axis, so forward (away from the player) is -z
                targetPosition.z -= SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe forward");
            }
            if (actionJustHappened(ActionHandles.MICROGESTURE_SWIPE_BACKWARD)){
                targetPosition.z += SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe backward");
            }
            if (actionJustHappened(ActionHandles.MICROGESTURE_TAP)){
                cubeColourIndex = (cubeColourIndex + 1) % CUBE_COLOURS.length;
                setCubeColour(CUBE_COLOURS[cubeColourIndex]);
                lastGestureLabel.setText("Last microgesture: " + handSide + " tap");
            }
        }

        /**
         * These actions are bound to both controller buttons and hand gestures (Tamarin gesture bindings, in the action
         * manifest) but they are read just like any other action, this code doesn't know or care which was used.
         */
        void handleCubeActions(float tpf){
            // a thumbs up, or the X button (Touch) / A button (Index)
            if (actionJustHappened(ActionHandles.RESET_CUBE)){
                reset();
            }
            // a thumb and ring finger pinch, or the B/Y buttons
            if (xrActionAppState.getBooleanActionState(ActionHandles.SPIN_CUBE, handSide.restrictToInputString).getState()){
                spinAngle += tpf * FastMath.PI;
            }
            // a float action, so it can drive something continuous like the size of the cube. The thumb and middle
            // finger pinch strength, or the grip
            float grow = xrActionAppState.getFloatActionState(ActionHandles.GROW_CUBE, handSide.restrictToInputString).getState();
            cube.setLocalScale(1 + 1.5f * grow);
        }

        /**
         * The action is bound on both hands, passing the hand's input string restricts the state to just this hand.
         */
        private boolean actionJustHappened(ActionHandle actionHandle){
            return xrActionAppState.getBooleanActionState(actionHandle, handSide.restrictToInputString).hasChangedToTrue();
        }

        void updateCube(float tpf){
            // move the cube smoothly towards where it is meant to be, so the gestures feel physical rather than teleporting it
            currentPosition.interpolateLocal(targetPosition, Math.min(1, tpf * 10));
            cube.setLocalTranslation(currentPosition);
            cube.setLocalRotation(new Quaternion().fromAngleAxis(spinAngle, Vector3f.UNIT_Y));
        }

        void reset(){
            targetPosition.set(startPosition);
            spinAngle = 0;
        }

        void setCubeColour(ColorRGBA colour){
            cubeMaterial.setColor("Diffuse", colour);
            cubeMaterial.setColor("Ambient", colour);
        }
    }

    /**
     * A bar whose height shows a gesture's strength (0 to 1), turning green when the gesture is on. A faint full height
     * "track" behind it shows how far it is from full.
     */
    private class Bar{
        final Node node;
        final Node track;
        final Material material;
        final ColorRGBA colour;
        final HandGestureSourcePreference source;
        final Function<HandGestureState, Float> value;
        final Function<HandGestureState, Boolean> active;

        Bar(ColorRGBA colour, HandGestureSourcePreference source, Function<HandGestureState, Float> value, Function<HandGestureState, Boolean> active){
            this.colour = colour;
            this.source = source;
            this.value = value;
            this.active = active;

            material = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", colour);
            node = unitHeightBar("bar", material, RenderQueue.Bucket.Opaque);
            node.setLocalScale(1, 0.001f, 1);

            Material trackMaterial = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            trackMaterial.setColor("Color", new ColorRGBA(1, 1, 1, 0.15f));
            trackMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            track = unitHeightBar("barTrack", trackMaterial, RenderQueue.Bucket.Transparent);
            track.setLocalScale(1, BAR_MAX_HEIGHT, 1);
        }

        void update(Optional<HandGestureState> state){
            float height = state.map(value).orElse(0f) * BAR_MAX_HEIGHT;
            node.setLocalScale(1, Math.max(0.001f, height), 1);
            material.setColor("Color", state.map(active).orElse(false) ? BAR_ACTIVE_COLOUR : colour);
        }
    }

    /**
     * A bar with its bottom at the node's origin and a height of 1, so the node can be scaled in y to set the
     * bar's height (and it grows upwards from the bottom, rather than from its centre).
     */
    private static Node unitHeightBar(String name, Material material, RenderQueue.Bucket bucket){
        Geometry bar = new Geometry(name, new Box(BAR_WIDTH / 2, 0.5f, BAR_WIDTH / 2));
        bar.setMaterial(material);
        bar.setQueueBucket(bucket);
        bar.setLocalTranslation(0, 0.5f, 0);
        Node node = new Node(name + "_node");
        node.attachChild(bar);
        return node;
    }

    @SuppressWarnings("DuplicatedCode") //as the examples are supposed to be self-contained accept some duplication
    private Spatial pickLine(){
        float length = 1;

        Cylinder pickCylinder = new Cylinder(10,10, 0.002f, length, true);

        Geometry geometry = new Geometry("debugHandLine", pickCylinder);
        Material material = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", new ColorRGBA(0.4f,0.4f,0.4f,0.5f));
        geometry.setMaterial(material);

        Quaternion rotate = new Quaternion();
        rotate.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        geometry.setLocalRotation(rotate);
        geometry.setLocalTranslation(length/2, 0,0);
        geometry.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);

        return geometry;
    }
}
