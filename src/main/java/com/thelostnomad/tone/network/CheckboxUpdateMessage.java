package com.thelostnomad.tone.network;

import com.thelostnomad.tone.block.gui.GuiLivingCraftingStation;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CheckboxUpdateMessage implements IMessage {

    public CheckboxUpdateMessage() {
    }

    boolean value;

    public CheckboxUpdateMessage(boolean val) {
        this.value = val;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(value);
    }

    public static class Handler implements IMessageHandler<CheckboxUpdateMessage, IMessage> {

        @Override
        public IMessage onMessage(CheckboxUpdateMessage message, MessageContext ctx) {

            if (Minecraft.getMinecraft().currentScreen instanceof GuiLivingCraftingStation){
                //((GuiLivingCraftingStation) Minecraft.getMinecraft().currentScreen).getContainer().lastRecipe = message.rec;
            }
            return null;
        }

    }

}
