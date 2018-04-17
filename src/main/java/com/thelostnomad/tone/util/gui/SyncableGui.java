package com.thelostnomad.tone.util.gui;

import net.minecraft.nbt.NBTTagCompound;

public interface SyncableGui {

    void doSync(NBTTagCompound fromServer);

}
