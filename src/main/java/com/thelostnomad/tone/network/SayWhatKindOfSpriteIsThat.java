package com.thelostnomad.tone.network;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.List;

public class SayWhatKindOfSpriteIsThat implements IMessage {

    private BlockPos whereSing;

    public SayWhatKindOfSpriteIsThat() {
    }

    public SayWhatKindOfSpriteIsThat(BlockPos where) {
        this.whereSing = where;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        whereSing = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("posX", whereSing.getX());
        tag.setInteger("posY", whereSing.getY());
        tag.setInteger("posZ", whereSing.getZ());

        //tag.setTag("species", spriteSpecies.toTag());
        ByteBufUtils.writeTag(buf, tag);
    }

    /**
     * Draw items from the server's inventory to fulfill the crafting matrix request
     */
    public static class Handler implements IMessageHandler<SayWhatKindOfSpriteIsThat, IMessage> {

        @Override
        public MessageCraftingSync onMessage(SayWhatKindOfSpriteIsThat message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player != null)
                ((WorldServer) player.world).addScheduledTask(() -> handle(player, message));
            return null;
        }

        /**
         * Do the operation on the server thread
         */
        public void handle(EntityPlayerMP player, SayWhatKindOfSpriteIsThat message) {
            // Here we are. Server side
            ThingsOfNaturalEnergies.logger.error("Received a request for sprite identificaiton server-side");

            NatureSpriteEntity guesstimatedNSE = null;

            List<NatureSpriteEntity> entities = player.world.getEntities(NatureSpriteEntity.class, new Predicate<NatureSpriteEntity>() {
                @Override
                public boolean apply(@Nullable NatureSpriteEntity input) {
                    return true;
                }
            });

            double oldMax = Double.MAX_VALUE;
            for(NatureSpriteEntity nse : entities){
                double dif = nse.getPosition().distanceSq(message.whereSing.getX(), message.whereSing.getY(),message.whereSing.getZ());
                if(dif < oldMax){
                    oldMax = dif;
                    guesstimatedNSE = nse;
                }
            }

            TonePacketHandler.sendTo(new WhyThatsASprite(message.whereSing, guesstimatedNSE.getSpeciesHelper().getInternalName()), player);
        }

    }
}

