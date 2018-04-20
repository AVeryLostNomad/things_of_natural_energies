package com.thelostnomad.tone.network;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SpriteSingPacket implements IMessage {

    private BlockPos whereSing;
    private SoundEvent thing;
//    private SpeciesHelper spriteSpecies;

    public SpriteSingPacket() {
    }

    public SpriteSingPacket(BlockPos where) {
        this(where, SoundEvents.BLOCK_NOTE_HARP);
        //this.spriteSpecies = base;
    }

    public SpriteSingPacket(BlockPos where, SoundEvent sound){
        this.whereSing = where;
        this.thing = sound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        whereSing = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));

        thing = fromString(tag.getString("sound"));

        //spriteSpecies = SpriteSpecies.from(tag.getCompoundTag("species"));
    }

    public String soundToString(SoundEvent se){
        return se.getSoundName().getResourcePath() + se.getSoundName().getResourceDomain();
    }

    public SoundEvent fromString(String s){
        Field[] declaredFields = SoundEvents.class.getDeclaredFields();
        List<Field> staticFields = new ArrayList<Field>();
        for(Field f : declaredFields){
            if(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())){
                staticFields.add(f);
            }
        }
        for(Field f : staticFields){
            try {
                SoundEvent se = (SoundEvent) f.get(null);
                if(s.equals(soundToString(se))){
                    return se;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("posX", whereSing.getX());
        tag.setInteger("posY", whereSing.getY());
        tag.setInteger("posZ", whereSing.getZ());

        tag.setString("sound", soundToString(thing));

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
            player.world.playSound(player, message.whereSing, message.thing, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        }
    }

}
