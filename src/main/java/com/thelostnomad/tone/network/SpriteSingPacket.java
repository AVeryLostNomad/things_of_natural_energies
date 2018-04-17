package com.thelostnomad.tone.network;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpriteSingPacket implements IMessage {

    private BlockPos whereSing;
//    private SpeciesHelper spriteSpecies;

    public SpriteSingPacket() {}

    public SpriteSingPacket(BlockPos where) {
        this.whereSing = where;
        //this.spriteSpecies = base;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        whereSing = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));

        //spriteSpecies = SpriteSpecies.from(tag.getCompoundTag("species"));
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

    /** Update the clientside crafting matrix */
    public static class Handler implements IMessageHandler<SpriteSingPacket, IMessage> {

        @Override
        public IMessage onMessage(SpriteSingPacket message, MessageContext ctx) {
            EntityPlayer player = ThingsOfNaturalEnergies.proxy.getClientPlayer();
            if (player != null)
                Minecraft.getMinecraft().addScheduledTask(() -> handle(player, message));
            return null; // end of message chain
        }

        /** Do the crafting sync */
        @SideOnly(Side.CLIENT)
        public void handle(EntityPlayer player, SpriteSingPacket message) {
            player.world.playSound(player, message.whereSing, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        }
    }

}
