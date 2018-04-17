package com.thelostnomad.tone.network;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TEAcceptor;
import com.thelostnomad.tone.block.tileentity.TEFocusPusher;
import com.thelostnomad.tone.block.tileentity.TEKeeper;
import com.thelostnomad.tone.util.gui.SyncableContainer;
import com.thelostnomad.tone.util.gui.SyncableTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class KindlyGiveMeAnUpdatedGUI implements IMessage {

    public KindlyGiveMeAnUpdatedGUI(){}

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    /** Draw items from the server's inventory to fulfill the crafting matrix request */
    public static class Handler implements IMessageHandler<KindlyGiveMeAnUpdatedGUI, IMessage> {

        @Override
        public MessageCraftingSync onMessage(KindlyGiveMeAnUpdatedGUI message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player != null)
                ((WorldServer) player.world).addScheduledTask(() -> handle(player, message));
            return null;
        }

        /** Do the operation on the server thread */
        public void handle(EntityPlayerMP player, KindlyGiveMeAnUpdatedGUI message) {
            Container container = player.openContainer;
            if(container != null){
                if(container instanceof SyncableContainer){
                    SyncableContainer synCont = (SyncableContainer) container;
                    SyncableTileEntity tileSyncable = synCont.getSyncableTileEntity();
                    if(tileSyncable instanceof TEKeeper){
                        TEKeeper keeper = (TEKeeper) tileSyncable;
                        ThingsOfNaturalEnergies.logger.error("Server side: " + keeper.isRedstoneRequired());
                    }
                    if(tileSyncable instanceof TEAcceptor){
                        TEAcceptor keeper = (TEAcceptor) tileSyncable;
                        ThingsOfNaturalEnergies.logger.error("Server side: " + keeper.getVoidExcess());
                    }
                    if(tileSyncable instanceof TEFocusPusher){
                        TEFocusPusher keeper = (TEFocusPusher) tileSyncable;
                        ThingsOfNaturalEnergies.logger.error("Server side: " + keeper.getRate());
                    }
                    TonePacketHandler.sendTo(new PlayerGuiSyncMessage(tileSyncable.getSyncable()), player);
                }
            }
        }

    }

}