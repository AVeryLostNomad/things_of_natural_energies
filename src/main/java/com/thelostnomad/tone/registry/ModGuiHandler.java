package com.thelostnomad.tone.registry;

import com.thelostnomad.tone.block.container.*;
import com.thelostnomad.tone.block.gui.*;
import com.thelostnomad.tone.block.tileentity.*;
import com.thelostnomad.tone.network.TonePacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ModGuiHandler implements IGuiHandler {

    public static int getGuiID() {return 888;}

    private static ModGuiHandler guiHandler = new ModGuiHandler();

    public static ModGuiHandler getInstance() {
        return guiHandler;
    }

    // Gets the server side element for the given gui id- this should return a container
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID != getGuiID()) {
            System.err.println("Invalid ID: expected " + getGuiID() + ", received " + ID);
        }

        BlockPos xyz = new BlockPos(x, y, z);
        TileEntity tileEntity = world.getTileEntity(xyz);
        if (tileEntity instanceof TEPuller) {
            TEPuller tileEntityInventoryBasic = (TEPuller) tileEntity;
            return new ContainerPuller(player.inventory, tileEntityInventoryBasic);
        }
        if (tileEntity instanceof TEPusher) {
            TEPusher tileEntityInventoryBasic = (TEPusher) tileEntity;
            return new ContainerPusher(player.inventory, tileEntityInventoryBasic);
        }
        if (tileEntity instanceof TELivingCraftingStation){
            TELivingCraftingStation tileEntityLivingCraftingStation = (TELivingCraftingStation) tileEntity;
            return new ContainerLivingCraftingStation(player.inventory, tileEntityLivingCraftingStation);
        }
        if (tileEntity instanceof TEKeeper){
            TEKeeper tileEntityLivingCraftingStation = (TEKeeper) tileEntity;
            return new ContainerKeeper(player.inventory, tileEntityLivingCraftingStation);
        }
        if (tileEntity instanceof TEAcceptor){
            TEAcceptor teAcceptor = (TEAcceptor) tileEntity;
            return new ContainerAcceptor(player.inventory, teAcceptor);
        }
        if (tileEntity instanceof TEFocusPusher){
            TEFocusPusher teFoc = (TEFocusPusher) tileEntity;
            return new ContainerFocusPusher(player.inventory, teFoc);
        }
        return null;
    }

    // Gets the client side element for the given gui id- this should return a gui
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID != getGuiID()) {
            System.err.println("Invalid ID: expected " + getGuiID() + ", received " + ID);
        }

        BlockPos xyz = new BlockPos(x, y, z);
        TileEntity tileEntity = world.getTileEntity(xyz);
        if (tileEntity instanceof TEPuller) {
            TEPuller tileEntityInventoryBasic = (TEPuller) tileEntity;
            return new GuiPuller(player.inventory, tileEntityInventoryBasic);
        }
        if (tileEntity instanceof TEPusher) {
            TEPusher tileEntityInventoryBasic = (TEPusher) tileEntity;
            return new GuiPusher(player.inventory, tileEntityInventoryBasic);
        }
        if (tileEntity instanceof TELivingCraftingStation){
            TELivingCraftingStation tileEntityLivingCraftingStation = (TELivingCraftingStation) tileEntity;
            return new GuiLivingCraftingStation(player.inventory, tileEntityLivingCraftingStation);
        }
        if (tileEntity instanceof TEKeeper){
            TEKeeper tileEntityLivingCraftingStation = (TEKeeper) tileEntity;
            return new GuiKeeper(player.inventory, tileEntityLivingCraftingStation);
        }
        if (tileEntity instanceof TEAcceptor){
            TEAcceptor teAcceptor = (TEAcceptor) tileEntity;
            return new GuiAcceptor(player.inventory, teAcceptor);
        }
        if (tileEntity instanceof TEFocusPusher){
            TEFocusPusher teFocusPusher = (TEFocusPusher) tileEntity;
            return new GuiFocusPusher(player.inventory, teFocusPusher);
        }
        return null;
    }

}
