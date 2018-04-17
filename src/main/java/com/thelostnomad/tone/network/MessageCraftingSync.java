package com.thelostnomad.tone.network;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.container.ContainerLivingCraftingStation;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/** A message to sync the crafting matrix to the client */
public class MessageCraftingSync implements IMessage {

    private NonNullList<ItemStack> stackList;
    private List<String> alert;

    public MessageCraftingSync() {}

    public MessageCraftingSync(InventoryCrafting matrix, List<String> alerts) {
        stackList = NonNullList.<ItemStack>withSize(matrix.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < matrix.getSizeInventory(); i++) {
            stackList.set(i, matrix.getStackInSlot(i));
        }
        this.alert = alerts;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        int count = tag.getInteger("count");
        stackList = NonNullList.<ItemStack>withSize(count, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, stackList);

        NBTTagList alertTags = tag.getTagList("alerts", 8);
        alert = new ArrayList<String>();
        for(int i = 0; i < alertTags.tagCount(); i++){
            String stringTag = alertTags.getStringTagAt(i);
            this.alert.add(stringTag);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("count", stackList.size());
        ItemStackHelper.saveAllItems(tag, this.stackList, false);
        ByteBufUtils.writeTag(buf, tag);

        NBTTagList alertList = new NBTTagList();
        for(String s : alert){
            alertList.appendTag(new NBTTagString(s));
        }
        tag.setTag("alerts", alertList);
    }

    /** Update the clientside crafting matrix */
    public static class Handler implements IMessageHandler<MessageCraftingSync, IMessage> {

        @Override
        public IMessage onMessage(MessageCraftingSync message, MessageContext ctx) {
            EntityPlayer player = ThingsOfNaturalEnergies.proxy.getClientPlayer();
            if (player != null)
                Minecraft.getMinecraft().addScheduledTask(() -> handle(player, message));
            return null; // end of message chain
        }

        /** Do the crafting sync */
        @SideOnly(Side.CLIENT)
        public void handle(EntityPlayer player, MessageCraftingSync message) {
            if (player.openContainer != null && player.openContainer instanceof ContainerLivingCraftingStation) {
                InventoryCrafting craft = ((ContainerLivingCraftingStation) player.openContainer).craftMatrix;
                int i = 0;
                for (ItemStack s : message.stackList) {
                    craft.setInventorySlotContents(i++, s);
                }
                for(String s : message.alert){
                    ThingsOfNaturalEnergies.logger.error("Got alert: " + s);
                }
            }
        }
    }

}