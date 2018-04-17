package com.thelostnomad.tone.network;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.util.gui.SyncableContainer;
import com.thelostnomad.tone.util.gui.SyncableTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class GUIUpdatePacket implements IMessage {

    NBTTagCompound toSync;

    public GUIUpdatePacket(){}

    public GUIUpdatePacket(SyncableTileEntity toSync) {
        this.toSync = toSync.getSyncable();
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
    public static class Handler implements IMessageHandler<GUIUpdatePacket, IMessage> {

        @Override
        public MessageCraftingSync onMessage(GUIUpdatePacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player != null)
                ((WorldServer) player.world).addScheduledTask(() -> handle(player, message));
            return null;
        }

        /** Do the operation on the server thread */
        public void handle(EntityPlayerMP player, GUIUpdatePacket message) {
            Container container = player.openContainer;
            if(container != null){
                if(container instanceof SyncableContainer){
                    SyncableContainer synCont = (SyncableContainer) container;
                    SyncableTileEntity tileSyncable = synCont.getSyncableTileEntity();
                    tileSyncable.doSync(message.toSync);
                    // We have adjusted the clientside values for at least one client. We need to send these updated
                    // values to all players so that they can all see it
                    TileEntity tile = (TileEntity) tileSyncable;
                    for(EntityPlayerMP pl : player.getServerWorld().getPlayers(EntityPlayerMP.class, new Predicate<EntityPlayerMP>() {
                        @Override
                        public boolean apply(@Nullable EntityPlayerMP input) {
                            if(Math.sqrt(input.getPosition().distanceSq(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())) < 5D){
                                if(input.openContainer != null){
                                    if(input.openContainer instanceof SyncableContainer){
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    })){
                        TonePacketHandler.sendTo(new PlayerGuiSyncMessage(message.toSync), pl);
                    }
                }
            }
        }

    }

}