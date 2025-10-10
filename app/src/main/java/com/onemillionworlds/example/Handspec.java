package com.onemillionworlds.example;

import com.jme3.material.Material;
import com.onemillionworlds.example.actions.ActionHandles;
import com.onemillionworlds.tamarin.vrhands.HandSpec;

public class Handspec {
    public static HandSpec handSpec(){
        return HandSpec.builder(
                        ActionHandles.HAND_POSE,
                        ActionHandles.HAND_POSE)
                .applyMaterialToLeftHand((hand, assetManager) -> {
                    //use the standard Tamarin texture but use a lit material instead
                    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                    mat.setTexture("DiffuseMap", assetManager.loadTexture("Tamarin/Textures/basicHands_pinStripe.png"));
                    hand.setMaterial(mat);
                })
                .applyMaterialToRightHand((hand, assetManager) -> {
                    //use the standard Tamarin texture but use a lit material instead
                    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                    mat.setTexture("DiffuseMap", assetManager.loadTexture("Tamarin/Textures/basicHands_pinStripe.png"));
                    hand.setMaterial(mat);
                })
                .build();
    }
}
