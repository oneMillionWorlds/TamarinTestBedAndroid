package com.onemillionworlds.example.actions;

import com.onemillionworlds.tamarin.actions.actionprofile.ActionHandle;

public class ActionHandles{

    public static final ActionHandle GRIP = new ActionHandle(ActionSets.MAIN, "grip");
    public static final ActionHandle HAPTIC = new ActionHandle(ActionSets.MAIN, "haptic");
    public static final ActionHandle TRIGGER = new ActionHandle(ActionSets.MAIN, "trigger");
    public static final ActionHandle HAND_POSE = new ActionHandle(ActionSets.MAIN, "handpose");

    /**
     * This puts all the dpad controls into a single action, this sucks but is the only way to do it on older runtimes
     * which may not support the XR_EXT_dpad_binding extension
     */
    public static final ActionHandle MOVEMENT_DPAD = new ActionHandle(ActionSets.MAIN, "movement_dpad");

    public static final ActionHandle WALK = new ActionHandle(ActionSets.MAIN, "walk");
    public static final ActionHandle OPEN_HAND_MENU = new ActionHandle(ActionSets.MAIN, "open_hand_menu");

    /*
     * META ONLY thumb microgestures (bare hands). See MetaOnlyHandGesturesExampleState
     */
    public static final ActionHandle MICROGESTURE_TAP = new ActionHandle(ActionSets.MAIN, "microgesture_tap");
    public static final ActionHandle MICROGESTURE_SWIPE_LEFT = new ActionHandle(ActionSets.MAIN, "microgesture_swipe_left");
    public static final ActionHandle MICROGESTURE_SWIPE_RIGHT = new ActionHandle(ActionSets.MAIN, "microgesture_swipe_right");
    public static final ActionHandle MICROGESTURE_SWIPE_FORWARD = new ActionHandle(ActionSets.MAIN, "microgesture_swipe_forward");
    public static final ActionHandle MICROGESTURE_SWIPE_BACKWARD = new ActionHandle(ActionSets.MAIN, "microgesture_swipe_backward");
}
