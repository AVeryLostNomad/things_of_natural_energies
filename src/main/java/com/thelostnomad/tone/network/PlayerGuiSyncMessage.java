package com.thelostnomad.tone.network;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.util.gui.SyncableContainer;
import com.thelostnomad.tone.util.gui.SyncableGui;
import com.thelostnomad.tone.util.gui.SyncableTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayerGuiSyncMessage implements IMessage {

    NBTTagCompound toSync;

    public PlayerGuiSyncMessage(){}

    public PlayerGuiSyncMessage(NBTTagCompound toSync) {
        this.toSync = toSync;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        toSync = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.toSync);
    }

    /** Draw items from the server's inventory to fulfill the crafting matrix request */
    public static class Handler implements IMessageHandler<PlayerGuiSyncMessage, IMessage> {

        @Override
        public MessageCraftingSync onMessage(PlayerGuiSyncMessage message, MessageContext ctx) {
            EntityPlayer player = ThingsOfNaturalEnergies.proxy.getClientPlayer();
            if (player != null)
                Minecraft.getMinecraft().addScheduledTask(() -> handle(player, message));
            return null;
        }

        /** Do the operation on the server thread */
        public void handle(EntityPlayer player, PlayerGuiSyncMessage message) {
            GuiScreen openGui = Minecraft.getMinecraft().currentScreen;
            if(openGui != null){
                if(openGui instanceof SyncableGui){
                    SyncableGui synCont = (SyncableGui) openGui;
                    synCont.doSync(message.toSync);
                }
            }
        }

    }

}