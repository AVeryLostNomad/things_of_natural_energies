package com.thelostnomad.tone.util.gui;

import net.minecraft.nbt.NBTTagCompound;

public interface SyncableTileEntity {

    NBTTagCompound getSyncable();
    void doSync(NBTTagCompound fromClient);

}
