package com.onemillionworlds.example;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.onemillionworlds.example.actions.ActionHandles;
import com.onemillionworlds.tamarin.actions.ActionType;
import com.onemillionworlds.tamarin.actions.HandSide;
import com.onemillionworlds.tamarin.actions.actionprofile.Action;
import com.onemillionworlds.tamarin.actions.actionprofile.ActionManifest;
import com.onemillionworlds.tamarin.actions.actionprofile.ActionSet;
import com.onemillionworlds.tamarin.actions.controllerprofile.GoogleDaydreamController;
import com.onemillionworlds.tamarin.actions.controllerprofile.HtcViveController;
import com.onemillionworlds.tamarin.actions.controllerprofile.MixedRealityMotionController;
import com.onemillionworlds.tamarin.actions.controllerprofile.OculusGoController;
import com.onemillionworlds.tamarin.actions.controllerprofile.OculusTouchController;
import com.onemillionworlds.tamarin.actions.controllerprofile.ValveIndexController;

public class Manifest {
    public static ActionManifest manifest(){
        Action grip = Action.builder()
                .actionHandle(ActionHandles.GRIP)
                .translatedName("Grip an item")
                .actionType(ActionType.FLOAT)
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().leftHand().trackpadClick())
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().rightHand().trackpadClick())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().leftHand().squeezeClick())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().rightHand().squeezeClick())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().leftHand().squeezeClick())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().rightHand().squeezeClick())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().leftHand().trackpadX())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().rightHand().trackpadX())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().leftHand().squeeze())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().rightHand().squeeze())
                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().leftHand().squeezeValue())
                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().rightHand().squeezeValue())
                .withDesktopSimulationKeyTrigger(HandSide.LEFT, new KeyTrigger(KeyInput.KEY_F1), true)
                .withDesktopSimulationKeyTrigger(HandSide.RIGHT, new KeyTrigger(KeyInput.KEY_F2), true)
                .build();

        Action haptic = Action.builder()
                .actionHandle(ActionHandles.HAPTIC)
                .translatedName("Haptic feedback")
                .actionType(ActionType.HAPTIC)
                .withSuggestAllKnownHapticBindings()
                .build();

        Action trigger = Action.builder()
                .actionHandle(ActionHandles.TRIGGER)
                .translatedName("Trigger action")
                .actionType(ActionType.FLOAT)
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().leftHand().selectClick())
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().rightHand().selectClick())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().leftHand().triggerValue())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().rightHand().triggerValue())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().leftHand().triggerValue())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().rightHand().triggerValue())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().leftHand().triggerClick())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().rightHand().triggerClick())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().leftHand().triggerValue())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().rightHand().triggerValue())
                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().leftHand().triggerValue())
                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().rightHand().triggerValue())
                .withDesktopSimulationKeyTrigger(HandSide.LEFT, new KeyTrigger(KeyInput.KEY_F3), false)
                .withDesktopSimulationKeyTrigger(HandSide.RIGHT, new KeyTrigger(KeyInput.KEY_F4), false)
                .build();

        Action handPose = Action.builder()
                .actionHandle(ActionHandles.HAND_POSE)
                .translatedName("Hand Pose")
                .actionType(ActionType.POSE)
                .withSuggestAllKnownAimPoseBindings()
                .build();


        /*
         * This puts all the dpad controls into a single action, this sucks but is the only way to do it on older runtimes
         * which may not support the XR_EXT_dpad_binding extension
         */
        Action movementDPad = Action.builder()
                .actionHandle(ActionHandles.MOVEMENT_DPAD)
                .translatedName("Step Movement controls")
                .actionType(ActionType.VECTOR2F)
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().leftHand().trackpad())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().leftHand().trackpad())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().leftHand().trackpad())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().leftHand().trackpad())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().leftHand().thumbStick())
                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().leftHand().thumbStick())
                .build();

        Action walk = Action.builder()
                .actionHandle(ActionHandles.WALK)
                .translatedName("Walk")
                .actionType(ActionType.VECTOR2F)
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().rightHand().trackpad())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().rightHand().trackpad())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().rightHand().trackpad())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().rightHand().trackpad())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().rightHand().thumbStick())
                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().rightHand().thumbStick())
                .build();

        Action openHandMenu = Action.builder()
                .actionHandle(ActionHandles.OPEN_HAND_MENU)
                .translatedName("Open Hand Menu")
                .actionType(ActionType.BOOLEAN)
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().leftHand().trackpadClick())
                .withSuggestedBinding(GoogleDaydreamController.PROFILE, GoogleDaydreamController.pathBuilder().rightHand().trackpadClick())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().leftHand().trackpadClick())
                .withSuggestedBinding(HtcViveController.PROFILE, HtcViveController.pathBuilder().rightHand().trackpadClick())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().leftHand().trackpadClick())
                .withSuggestedBinding(MixedRealityMotionController.PROFILE, MixedRealityMotionController.pathBuilder().rightHand().trackpadClick())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().leftHand().trackpadClick())
                .withSuggestedBinding(OculusGoController.PROFILE, OculusGoController.pathBuilder().rightHand().trackpadClick())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().leftHand().thumbStickClick())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().rightHand().thumbStickClick())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().rightHand().aClick())
                .withSuggestedBinding(OculusTouchController.PROFILE, OculusTouchController.pathBuilder().rightHand().aTouch())

                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().leftHand().thumbStickClick())
                .withSuggestedBinding(ValveIndexController.PROFILE, ValveIndexController.pathBuilder().rightHand().thumbStickClick())
                .withDesktopSimulationKeyTrigger(HandSide.LEFT, new KeyTrigger(KeyInput.KEY_F5), true)
                .withDesktopSimulationKeyTrigger(HandSide.RIGHT, new KeyTrigger(KeyInput.KEY_F6), true)
                .build();

        return ActionManifest.builder()
                .withActionSet(ActionSet
                        .builder()
                        .name("main")
                        .translatedName("Main Actions")
                        .priority(1)
                        .withAction(grip)
                        .withAction(haptic)
                        .withAction(trigger)
                        .withAction(handPose)
                        .withAction(movementDPad)
                        .withAction(walk)
                        .withAction(openHandMenu)
                        .build()
                ).build();
    }
}
