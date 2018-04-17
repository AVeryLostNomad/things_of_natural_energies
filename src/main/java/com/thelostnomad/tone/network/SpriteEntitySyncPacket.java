package com.thelostnomad.tone.network;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.entities.nature_sprite.ai.NatureSpriteAI;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.cmd.Spec;

import javax.annotation.Nullable;
import java.util.List;

public class SpriteEntitySyncPacket implements IMessage {

    private BlockPos whereSing;
    private String speciesToSync;
    public SpriteEntitySyncPacket() {}

    public SpriteEntitySyncPacket(BlockPos where, String speciesToSync) {
        this.whereSing = where;
        this.speciesToSync = speciesToSync;
        //this.spriteSpecies = base;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        whereSing = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));
        speciesToSync = tag.getString("species");
        //spriteSpecies = SpriteSpecies.from(tag.getCompoundTag("species"));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("posX", whereSing.getX());
        tag.setInteger("posY", whereSing.getY());
        tag.setInteger("posZ", whereSing.getZ());

        tag.setString("species", speciesToSync);

        //tag.setTag("species", spriteSpecies.toTag());
        ByteBufUtils.writeTag(buf, tag);
    }

    /** Update the clientside crafting matrix */
    public static class Handler implements IMessageHandler<SpriteEntitySyncPacket, IMessage> {

        @Override
        public IMessage onMessage(SpriteEntitySyncPacket message, MessageContext ctx) {
            EntityPlayer player = ThingsOfNaturalEnergies.proxy.getClientPlayer();
            if (player != null)
                Minecraft.getMinecraft().addScheduledTask(() -> handle(player, message));
            return null; // end of message chain
        }

        /** Do the crafting sync */
        @SideOnly(Side.CLIENT)
        public void handle(EntityPlayer player, SpriteEntitySyncPacket message) {
            ThingsOfNaturalEnergies.logger.error("We got a message client-side");
            List<NatureSpriteEntity> nseList = player.world.getEntities(NatureSpriteEntity.class, new Predicate<NatureSpriteEntity>() {
                @Override
                public boolean apply(@Nullable NatureSpriteEntity input) {
                    return input.getPosition().distanceSq(message.whereSing.getX(), message.whereSing.getY(), message.whereSing.getZ()) < 2;
                }
            });
            SpeciesHelper sh = SpeciesHelper.fromInternalName(message.speciesToSync);
            ThingsOfNaturalEnergies.logger.error("Sending the updated value of " + sh.getInternalName());
            NatureSpriteEntity nse = nseList.get(0);
            nse.setSpeciesHelper(sh);
        }
    }

}