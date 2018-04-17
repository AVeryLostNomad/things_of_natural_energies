package com.thelostnomad.tone.util.world;

public interface IInteractable {

    InteractableType getType();



    enum InteractableType {
        STORAGE,
        FLUID,
        PUSHER,
        PULLER,
        INTEGRATION,
        KEEPER,
        MISC
    }

}
