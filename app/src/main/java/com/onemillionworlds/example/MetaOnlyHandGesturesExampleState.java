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
import com.onemillionworlds.tamarin.actions.state.HandTrackingAimState;
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

/**
 * <b>META ONLY.</b> Demonstrates two Meta specific hand tracking (bare hands, no controllers) features by moving
 * cubes around. Each hand controls its own cube (the left hand controls the left cube, the right hand the right cube).
 * On other runtimes nothing in this example will respond to gestures (but nothing will go wrong either).
 * <p>
 *     1) Thumb microgestures (XR_META_hand_tracking_microgestures). These go through the normal action system: see the
 *     MICROGESTURE_* actions in the action manifest, which are bound using
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
 *     2) Per finger pinch information (XR_FB_hand_tracking_aim). This doesn't go through the action system, instead it is
 *     read each frame from {@link XrActionBaseAppState#getHandTrackingAimState(HandSide)}. This lets you use the
 *     thumb against fingers other than the index finger, which the action system can't express:
 * </p>
 * <ul>
 *     <li>pinch the thumb and middle finger together to grow the cube (it grows with the pinch strength)</li>
 *     <li>pinch the thumb and ring finger together to spin the cube</li>
 *     <li>pinch the thumb and little finger together to reset the cube</li>
 * </ul>
 * <p>
 *     The runtime's stabilised aim pose (also from XR_FB_hand_tracking_aim) is shown as a ray from each hand, which
 *     turns white while the index finger is pinching.
 * </p>
 * <p>
 *     In desktop simulation the microgestures are on the numpad (4/6/8/2 to swipe, 5 to tap) and act as the right
 *     hand, the finger pinches have no simulation.
 * </p>
 */
public class MetaOnlyHandGesturesExampleState extends BaseAppState{

    private static final float SWIPE_STEP = 0.1f;
    private static final float RAY_LENGTH = 1.5f;

    private static final float PINCH_BAR_MAX_HEIGHT = 0.25f;
    private static final float PINCH_BAR_WIDTH = 0.03f;
    private static final float PINCH_BAR_SPACING = 0.05f;
    private static final ColorRGBA PINCH_BAR_NOT_PINCHING = new ColorRGBA(0.2f, 0.6f, 1f, 1f);
    private static final ColorRGBA PINCH_BAR_PINCHING = ColorRGBA.Green;

    private static final ColorRGBA[] CUBE_COLOURS = new ColorRGBA[]{
            ColorRGBA.Orange, ColorRGBA.Cyan, ColorRGBA.Magenta, ColorRGBA.Yellow, ColorRGBA.Green
    };

    Node rootNodeDelegate = new Node("MetaOnlyHandGesturesExampleState");

    XrBaseAppState xrAppState;
    XrActionBaseAppState xrActionAppState;

    Label statusLabel;
    Label lastGestureLabel;

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
        lemurWindow.addChild(new Label("META ONLY hand gestures. Put the controllers down and use your bare hands.\n" +
                "Each hand controls its own cube (left hand, left cube. Right hand, right cube).\n\n" +
                "Thumb microgestures (raise your thumb above your index finger first):\n" +
                "   - Swipe thumb left/right along index finger: move cube left/right\n" +
                "   - Swipe thumb forward/backward: move cube away/towards you\n" +
                "   - Tap thumb on side of index finger: cube changes colour\n\n" +
                "Finger pinches:\n" +
                "   - Thumb + middle finger: grow the cube (pinch harder for bigger)\n" +
                "   - Thumb + ring finger: spin the cube\n" +
                "   - Thumb + little finger: reset the cube\n\n" +
                "The rays from your hands are the runtime's stabilised aim (white while index pinching)\n\n" +
                "The bars under each cube are that hand's pinch strength for the Index, Middle, Ring and\n" +
                "Little fingers. A bar turns green when the runtime itself decides that finger is pinching."));
        statusLabel = lemurWindow.addChild(new Label(""));
        lastGestureLabel = lemurWindow.addChild(new Label("Last microgesture: none yet"));

        lemurWindow.addChild(new Button("Back to menu")).addClickCommands(source -> {
            getStateManager().detach(this);
            getStateManager().attach(new MenuExampleState());
        });

        lemurWindow.setLocalTranslation(-1.3f, 1.9f, 8.6f);
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

    @Override
    public void update(float tpf){
        super.update(tpf);

        StringBuilder pinchDataStatus = new StringBuilder();
        for(HandCube handCube : handCubes){
            handCube.handleMicrogestures();
            boolean tracked = handCube.handleFingerPinches(tpf);
            handCube.updateCube(tpf);
            pinchDataStatus.append(", ").append(handCube.handSide).append(": ").append(yesNo(tracked));
        }

        statusLabel.setText(
                "Microgestures available: " + yesNo(xrAppState.checkExtensionLoaded(MetaHandMicrogestures.REQUIRED_EXTENSION)) +
                ", finger pinches available: " + yesNo(xrAppState.checkExtensionLoaded("XR_FB_hand_tracking_aim")) + "\n" +
                "Finger pinch data" + pinchDataStatus);
    }

    private static String yesNo(boolean value){
        return value ? "yes" : "no";
    }

    @Override
    protected void cleanup(Application app){
        rootNodeDelegate.removeFromParent();
        functionRegistrations.forEach(FunctionRegistration::endFunction);
    }

    @Override protected void onEnable(){}

    @Override protected void onDisable(){}

    /**
     * A cube, the hand that controls it, and that hand's aim ray.
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

        final Node aimRay;
        final Material aimRayMaterial;

        /**
         * One bar per finger (indexed by PinchFinger ordinal). The bar's height is scaled by the pinch strength.
         */
        final Node[] pinchBars = new Node[HandTrackingAimState.PinchFinger.values().length];
        final Material[] pinchBarMaterials = new Material[HandTrackingAimState.PinchFinger.values().length];

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

            aimRayMaterial = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            aimRayMaterial.setColor("Color", new ColorRGBA(0.2f, 0.6f, 1f, 0.6f));
            aimRayMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            aimRay = aimRay(handSide, aimRayMaterial);
            rootNodeDelegate.attachChild(aimRay);

            // underneath the cube (low enough that even a fully grown cube doesn't cover them)
            rootNodeDelegate.attachChild(createPinchBars(startPosition.add(0, -0.5f, 0)));
        }

        /**
         * Creates a little bar chart, one bar per finger, with a faint full height "track" behind each bar so it's
         * clear how far it is from a full pinch.
         */
        private Node createPinchBars(Vector3f bottomCentre){
            Node chart = new Node("pinchBars_" + handSide);
            chart.setLocalTranslation(bottomCentre);

            Material trackMaterial = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            trackMaterial.setColor("Color", new ColorRGBA(1, 1, 1, 0.15f));
            trackMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

            HandTrackingAimState.PinchFinger[] fingers = HandTrackingAimState.PinchFinger.values();
            for(int i = 0; i < fingers.length; i++){
                float x = (i - (fingers.length - 1) / 2f) * PINCH_BAR_SPACING;

                Node track = unitHeightBar("pinchTrack_" + fingers[i], trackMaterial, RenderQueue.Bucket.Transparent);
                track.setLocalScale(1, PINCH_BAR_MAX_HEIGHT, 1);
                track.setLocalTranslation(x, 0, -0.01f);
                chart.attachChild(track);

                pinchBarMaterials[i] = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                pinchBarMaterials[i].setColor("Color", PINCH_BAR_NOT_PINCHING);
                pinchBars[i] = unitHeightBar("pinchBar_" + fingers[i], pinchBarMaterials[i], RenderQueue.Bucket.Opaque);
                pinchBars[i].setLocalTranslation(x, 0, 0);
                pinchBars[i].setLocalScale(1, 0.001f, 1);
                chart.attachChild(pinchBars[i]);

                // I, M, R or L under each bar
                Label label = new Label(fingers[i].name().substring(0, 1));
                label.setLocalScale(0.003f);
                label.setLocalTranslation(x - 0.01f, -0.01f, 0);
                chart.attachChild(label);
            }
            return chart;
        }

        /**
         * A bar with its bottom at the node's origin and a height of 1, so the node can be scaled in y to set the
         * bar's height (and it grows upwards from the bottom, rather than from its centre).
         */
        private Node unitHeightBar(String name, Material material, RenderQueue.Bucket bucket){
            Geometry bar = new Geometry(name, new Box(PINCH_BAR_WIDTH / 2, 0.5f, PINCH_BAR_WIDTH / 2));
            bar.setMaterial(material);
            bar.setQueueBucket(bucket);
            bar.setLocalTranslation(0, 0.5f, 0);
            Node node = new Node(name + "_node");
            node.attachChild(bar);
            return node;
        }

        /**
         * Sets each bar's height to the pinch strength, and colours it by whether the runtime thinks it's a pinch.
         * Comparing the two shows whether the runtime can see the pinch at all (the bar rises) but hasn't decided it's a
         * pinch (the bar stays blue), in which case a lower threshold on the strength might work better.
         */
        private void updatePinchBars(Optional<HandTrackingAimState> aimStateOpt){
            for(HandTrackingAimState.PinchFinger finger : HandTrackingAimState.PinchFinger.values()){
                float strength = aimStateOpt.map(state -> state.getPinchStrength(finger)).orElse(0f);
                boolean pinching = aimStateOpt.map(state -> state.isPinching(finger)).orElse(false);
                // never scale to exactly zero, JME doesn't like it
                pinchBars[finger.ordinal()].setLocalScale(1, Math.max(0.001f, strength * PINCH_BAR_MAX_HEIGHT), 1);
                pinchBarMaterials[finger.ordinal()].setColor("Color", pinching ? PINCH_BAR_PINCHING : PINCH_BAR_NOT_PINCHING);
            }
        }

        /**
         * Microgestures are ordinary boolean actions, they become true once the gesture is recognised. Using
         * hasChangedToTrue means each gesture moves the cube exactly once.
         */
        void handleMicrogestures(){
            if (gestureJustHappened(ActionHandles.MICROGESTURE_SWIPE_LEFT)){
                targetPosition.x -= SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe left");
            }
            if (gestureJustHappened(ActionHandles.MICROGESTURE_SWIPE_RIGHT)){
                targetPosition.x += SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe right");
            }
            if (gestureJustHappened(ActionHandles.MICROGESTURE_SWIPE_FORWARD)){
                // the player is looking down the negative z axis, so forward (away from the player) is -z
                targetPosition.z -= SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe forward");
            }
            if (gestureJustHappened(ActionHandles.MICROGESTURE_SWIPE_BACKWARD)){
                targetPosition.z += SWIPE_STEP;
                lastGestureLabel.setText("Last microgesture: " + handSide + " swipe backward");
            }
            if (gestureJustHappened(ActionHandles.MICROGESTURE_TAP)){
                cubeColourIndex = (cubeColourIndex + 1) % CUBE_COLOURS.length;
                setCubeColour(CUBE_COLOURS[cubeColourIndex]);
                lastGestureLabel.setText("Last microgesture: " + handSide + " tap");
            }
        }

        /**
         * The action is bound on both hands, passing the hand's input string restricts the state to just this hand.
         */
        private boolean gestureJustHappened(ActionHandle actionHandle){
            return xrActionAppState.getBooleanActionState(actionHandle, handSide.restrictToInputString).hasChangedToTrue();
        }

        /**
         * The hand tracking aim state isn't an action, it's read directly every frame. It is empty if the extension isn't
         * available or the hand isn't being tracked (e.g. the user is holding a controller).
         *
         * @return if there was finger pinch data for this hand
         */
        boolean handleFingerPinches(float tpf){
            Optional<HandTrackingAimState> aimStateOpt = xrActionAppState.getHandTrackingAimState(handSide);

            // the bars are purely diagnostic, so they are shown even during the system gesture
            updatePinchBars(aimStateOpt);

            // while the user is doing the system gesture (on Quest: looking at the palm and pinching) ignore this hand
            // so opening the system menu doesn't also do things in the game
            if (!aimStateOpt.isPresent() || aimStateOpt.get().isSystemGestureActive()){
                aimRay.setCullHint(Spatial.CullHint.Always);
                cube.setLocalScale(1);
                return aimStateOpt.isPresent();
            }
            HandTrackingAimState aimState = aimStateOpt.get();

            // the pinch strength is continuous (0 to 1) so it can drive something continuous, like the size of the cube
            cube.setLocalScale(1 + 1.5f * aimState.getPinchStrength(HandTrackingAimState.PinchFinger.MIDDLE));

            // for discrete actions prefer the runtime's own isPinching decision over applying a threshold to the pinch strength
            if (aimState.isPinching(HandTrackingAimState.PinchFinger.RING)){
                spinAngle += tpf * FastMath.PI;
            }
            if (aimState.isPinching(HandTrackingAimState.PinchFinger.LITTLE)){
                reset();
            }

            if (aimState.isAimValid()){
                aimRay.setCullHint(Spatial.CullHint.Inherit);
                aimRay.setLocalTranslation(aimState.getAimPosition());
                aimRay.setLocalRotation(aimState.getAimOrientation());
                aimRayMaterial.setColor("Color", aimState.isPinching(HandTrackingAimState.PinchFinger.INDEX) ? ColorRGBA.White : new ColorRGBA(0.2f, 0.6f, 1f, 0.6f));
            } else{
                aimRay.setCullHint(Spatial.CullHint.Always);
            }
            return true;
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
     * A ray pointing along the aim pose's negative z axis (the OpenXR convention for aim poses).
     */
    private static Node aimRay(HandSide handSide, Material material){
        Cylinder cylinder = new Cylinder(6, 6, 0.003f, RAY_LENGTH, true); // a cylinder's axis is its z axis
        Geometry geometry = new Geometry("aimRay_" + handSide, cylinder);
        geometry.setMaterial(material);
        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
        geometry.setLocalTranslation(0, 0, -RAY_LENGTH / 2);

        Node node = new Node("aimRayNode_" + handSide);
        node.attachChild(geometry);
        node.setCullHint(Spatial.CullHint.Always);
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
