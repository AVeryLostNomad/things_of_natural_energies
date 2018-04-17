package com.thelostnomad.tone.network;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpritePlayEffectPacket implements IMessage {

    private BlockPos whereSing;
    private EnumParticleTypes type;
    private int amount;
//    private SpeciesHelper spriteSpecies;

    public SpritePlayEffectPacket() {}

    public SpritePlayEffectPacket(BlockPos where, EnumParticleTypes type, int amount) {
        this.whereSing = where;
        this.type = type;
        this.amount = amount;
        //this.spriteSpecies = base;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        whereSing = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));
        type = EnumParticleTypes.getByName(tag.getString("type"));
        amount = tag.getInteger("amt");
        //spriteSpecies = SpriteSpecies.from(tag.getCompoundTag("species"));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("posX", whereSing.getX());
        tag.setInteger("posY", whereSing.getY());
        tag.setInteger("posZ", whereSing.getZ());

        tag.setString("type", type.getParticleName());
        tag.setInteger("amt", amount);

        //tag.setTag("species", spriteSpecies.toTag());
        ByteBufUtils.writeTag(buf, tag);
    }

    /** Update the clientside crafting matrix */
    public static class Handler implements IMessageHandler<SpritePlayEffectPacket, IMessage> {

        @Override
        public IMessage onMessage(SpritePlayEffectPacket message, MessageContext ctx) {
            EntityPlayer player = ThingsOfNaturalEnergies.proxy.getClientPlayer();
            if (player != null)
                Minecraft.getMinecraft().addScheduledTask(() -> handle(player, message));
            return null; // end of message chain
        }

        /** Do the crafting sync */
        @SideOnly(Side.CLIENT)
        public void handle(EntityPlayer player, SpritePlayEffectPacket message) {
            for(int i = 0; i < message.amount; i++){
                double d0 = player.world.rand.nextGaussian() * 0.02D;
                double d1 = player.world.rand.nextGaussian() * 0.02D;
                double d2 = player.world.rand.nextGaussian() * 0.02D;
                player.world.spawnParticle(message.type, (double)((float)message.whereSing.getX() +
                        player.world.rand.nextFloat()), (double)message.whereSing.getY() +
                        (double)player.world.rand.nextFloat() * player.world.getBlockState(message.whereSing).
                                getBoundingBox(player.world, message.whereSing).maxY, (double)((float)message.
                        whereSing.getZ() + player.world.rand.nextFloat()), d0, d1, d2);
            }

        }
    }

}