package com.thelostnomad.tone.network;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.container.ContainerLivingCraftingStation;
import com.thelostnomad.tone.block.tileentity.TELivingCraftingStation;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.util.crafting.StackUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The JEI crafting recipe server sync message */
public class MessageRecipeSync implements IMessage {

    private NBTTagCompound recipe;

    public MessageRecipeSync() {}

    public MessageRecipeSync(NBTTagCompound recipe) {
        this.recipe = recipe;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        recipe = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.recipe);
    }

    /** Draw items from the server's inventory to fulfill the crafting matrix request */
    public static class Handler implements IMessageHandler<MessageRecipeSync, MessageCraftingSync> {

        ItemStack[][] recipe;

        @Override
        public MessageCraftingSync onMessage(MessageRecipeSync message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player != null)
                ((WorldServer) player.world).addScheduledTask(() -> handle(player, message));
            return null;
        }

        /** Do the operation on the server thread */
        public void handle(EntityPlayerMP player, MessageRecipeSync message) {
            Container container = player.openContainer;
            if (container instanceof ContainerLivingCraftingStation) {
                ContainerLivingCraftingStation con = (ContainerLivingCraftingStation) container;
                TELivingCraftingStation tileEntity = con.lcs;

                // Empty grid into inventory
                //con.clearGrid(player);

                // They have sent us a recipe they want to make.

                this.recipe = new ItemStack[9][];
                for (int x = 0; x < this.recipe.length; x++) {
                    NBTTagList list = message.recipe.getTagList("#" + x, 10);
                    if (list.tagCount() > 0) {
                        NBTTagCompound tag = list.getCompoundTagAt(0);
                        boolean hasOre = tag.hasKey("ore");
                        if (hasOre) { // sent an oredict entry
                            List<ItemStack> items = OreDictionary.getOres(tag.getString("ore"));
                            this.recipe[x] = new ItemStack[items.size()];
                            for (int y = 0; y < items.size(); y++) {
                                this.recipe[x][y] = items.get(y).copy();
                            }
                        } else { // sent an itemstack list
                            this.recipe[x] = new ItemStack[list.tagCount()];
                            for (int y = 0; y < list.tagCount(); y++) {
                                this.recipe[x][y] = new ItemStack(list.getCompoundTagAt(y));
                            }
                        }
                    }
                }
                List<String> alerts = new ArrayList<String>();

                TESentientTreeCore core = (TESentientTreeCore) tileEntity.getWorld().getTileEntity(tileEntity.getCoreLocation());
                for (int i = 0; i < this.recipe.length; i++) {
                    if (this.recipe[i] != null && this.recipe[i].length > 0) {
                        Slot slot = con.getSlotFromInventory(con.craftMatrix, i);
                        if (slot != null) {
                            // We need to fit an item here:

                            ItemStack retreived = core.getFirstItemstackFromInventoryMatching(this.recipe[i][0]);
                            if(retreived == null){
                                // Can we maybe make it craft the thing?
                                boolean result = core.autocraftIfPossible(Arrays.asList(this.recipe[i]));
                                if(result){
                                    retreived = core.getFirstItemstackFromInventoryMatching(this.recipe[i][0]);
                                }
                            }

                            // Try to put slot there
                            if(retreived != null){
                                slot.putStack(retreived);
                            }
                        }
                    }
                }
                // reply with a crafting matrix sync message
                TonePacketHandler.sendTo(new MessageCraftingSync(con.craftMatrix, alerts), player);
            }
        }

    }

}