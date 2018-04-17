package com.thelostnomad.tone.integration;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public interface IToneIntegration {

    String getIntegrationModid();

    void registerBlocks(RegistryEvent.Register<Block> event);

    void registerItems(RegistryEvent.Register<Item> event);

    void registerTileEntities();

    // returns ending id
    int registerNetworkMessages(SimpleNetworkWrapper wrapper, int startId);

    IBlockState[] getModelBlockStates();

    void preInit(FMLPreInitializationEvent event);
    void init(FMLInitializationEvent event);
    void postInit(FMLPostInitializationEvent event);

}
