package com.thelostnomad.tone.proxy;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.*;
import com.thelostnomad.tone.block.berries.FuncoBerry;
import com.thelostnomad.tone.block.berries.GlutoBerry;
import com.thelostnomad.tone.block.berries.HastoBerry;
import com.thelostnomad.tone.block.berries.RezzoBerry;
import com.thelostnomad.tone.block.fluid.BlockTransmutationGas;
import com.thelostnomad.tone.block.fluid_hollows.BasicFluidHollow;
import com.thelostnomad.tone.block.storage_hollows.*;
import com.thelostnomad.tone.block.tileentity.*;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.integration.IToneIntegration;
import com.thelostnomad.tone.integration.ae2.ToneAE2;
import com.thelostnomad.tone.network.SpriteEntitySyncPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.registry.*;
import com.thelostnomad.tone.util.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class CommonProxy {

    public static ItemBlock sentientLog;

    public static IToneIntegration[] toneIntegrations = new IToneIntegration[]{
            new ToneAE2()
    };

    /** Gets the client player clientside, or null serverside */
    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void preInit(FMLPreInitializationEvent e) {
        // Each of your tile entities needs to be registered with a name that is unique to your mod.
        GameRegistry.registerTileEntity(TESentientTreeCore.class, TESentientTreeCore.NAME);
        GameRegistry.registerTileEntity(TEStorageHollow.class, TEStorageHollow.NAME);
        GameRegistry.registerTileEntity(TEFluidHollow.class, TEFluidHollow.NAME);
        GameRegistry.registerTileEntity(TEPuller.class, TEPuller.NAME);
        GameRegistry.registerTileEntity(TEPusher.class, TEPusher.NAME);
        GameRegistry.registerTileEntity(TELivingCraftingStation.class, TELivingCraftingStation.NAME);
        GameRegistry.registerTileEntity(TEKeeper.class, TEKeeper.NAME);
        GameRegistry.registerTileEntity(TEAcceptor.class, TEAcceptor.NAME);
        GameRegistry.registerTileEntity(TEFocusPusher.class, TEFocusPusher.NAME);

        for(IToneIntegration integration : toneIntegrations){
            if(Loader.isModLoaded(integration.getIntegrationModid())){
                integration.registerTileEntities();
            }
        }

        NetworkRegistry.INSTANCE.registerGuiHandler(ThingsOfNaturalEnergies.instance, ModGuiHandler.getInstance());
        ModFluids.registerFluids();

        ModEntities.init();

//        blockSimple = (BlockSimple)(new BlockSimple().setUnlocalizedName("mbe01_block_simple_unlocalised_name"));
//        blockSimple.setRegistryName("mbe01_block_simple_registry_name");
//        ForgeRegistries.BLOCKS.register(blockSimple);
//
//        // We also need to create and register an ItemBlock for this block otherwise it won't appear in the inventory
//        itemBlockSimple = new ItemBlock(blockSimple);
//        itemBlockSimple.setRegistryName(blockSimple.getRegistryName());
//        ForgeRegistries.ITEMS.register(itemBlockSimple);
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new RootBlock());
        event.getRegistry().register(new SentientTreeCore());
        event.getRegistry().register(new BasicStorageHollow());
        event.getRegistry().register(new BigStorageHollow());
        event.getRegistry().register(new LargeStorageHollow());
        event.getRegistry().register(new MassiveStorageHollow());
        event.getRegistry().register(new GargantuanStorageHollow());
        event.getRegistry().register(new QuiteBigStorageHollow());
        event.getRegistry().register(new EvenBiggerStorageHollow());
        //event.getRegistry().register(new SingularityStorageHollow());
        event.getRegistry().register(new BasicFluidHollow());
        event.getRegistry().register(new SentientSapling());
        event.getRegistry().register(new SentientLog());
        event.getRegistry().register(new SentientLeaves());
        event.getRegistry().register(new BlockPuller());
        event.getRegistry().register(new BlockPusher());
        event.getRegistry().register(new BlockKeeper());
        event.getRegistry().register(new BlockFocusPusher());
        event.getRegistry().register(new BlockAcceptor());
//        event.getRegistry().register(new BlockTransmutationGas());
        event.getRegistry().register(new BlockLivingCraftingStation());

        //Berries
        event.getRegistry().register(new HastoBerry());
        event.getRegistry().register(new GlutoBerry());
        event.getRegistry().register(new FuncoBerry());
        event.getRegistry().register(new RezzoBerry());

        for(IToneIntegration integration : toneIntegrations){
            if(Loader.isModLoaded(integration.getIntegrationModid())){
                integration.registerBlocks(event);
            }
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(ModBlocks.rootsBlock).setRegistryName(ModBlocks.rootsBlock.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.sentientTreeCore).setRegistryName(ModBlocks.sentientTreeCore.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowBasic).setRegistryName(ModBlocks.storageHollowBasic.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowBig).setRegistryName(ModBlocks.storageHollowBig.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowLarge).setRegistryName(ModBlocks.storageHollowLarge.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowMassive).setRegistryName(ModBlocks.storageHollowMassive.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowGargantuan).setRegistryName(ModBlocks.storageHollowGargantuan.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowQuiteBig).setRegistryName(ModBlocks.storageHollowQuiteBig.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowEvenBigger).setRegistryName(ModBlocks.storageHollowEvenBigger.getRegistryName()));
        //event.getRegistry().register(new ItemBlock(ModBlocks.storageHollowSingularity).setRegistryName(ModBlocks.storageHollowSingularity.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.fluidHollowBasic).setRegistryName(ModBlocks.fluidHollowBasic.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.sentientLeaves).setRegistryName(ModBlocks.sentientLeaves.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.sentientLog).setRegistryName(ModBlocks.sentientLog.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.sentientSapling).setRegistryName(ModBlocks.sentientSapling.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.blockPuller).setRegistryName(ModBlocks.blockPuller.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.blockPusher).setRegistryName(ModBlocks.blockPusher.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.hastoBerry).setRegistryName(ModBlocks.hastoBerry.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.glutoBerry).setRegistryName(ModBlocks.glutoBerry.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.funcoBerry).setRegistryName(ModBlocks.funcoBerry.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.rezzoBerry).setRegistryName(ModBlocks.rezzoBerry.getRegistryName()));
//        event.getRegistry().register(new ItemBlock(ModBlocks.transmutationGas).setRegistryName(ModBlocks.transmutationGas.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.livingCraftingStation).setRegistryName(ModBlocks.livingCraftingStation.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.blockKeeper).setRegistryName(ModBlocks.blockKeeper.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.blockAcceptor).setRegistryName(ModBlocks.blockAcceptor.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ModBlocks.focusPusher).setRegistryName(ModBlocks.focusPusher.getRegistryName()));

        event.getRegistry().register(ModItems.tokenPullAll);
        event.getRegistry().register(ModItems.hastoBerryItem);
        event.getRegistry().register(ModItems.glutoBerryItem);
        event.getRegistry().register(ModItems.funcoBerryItem);
        event.getRegistry().register(ModItems.rezzoBerryItem);
        event.getRegistry().register(ModItems.shardOfSentience);

        event.getRegistry().register(ModItems.natureSpriteBaseItem);
        event.getRegistry().register(ModItems.greedySprite);
        event.getRegistry().register(ModItems.packerSprite);

        for(IToneIntegration integration : toneIntegrations){
            if(Loader.isModLoaded(integration.getIntegrationModid())){
                integration.registerItems(event);
            }
        }
    }

    @SubscribeEvent
    public static void entityEnterWorld(EntityJoinWorldEvent event){
        if(event.getWorld().isRemote) return;
        Entity e = event.getEntity();
        if(!(e instanceof NatureSpriteEntity)){
            return;
        }
        NatureSpriteEntity nse = (NatureSpriteEntity) e;
        // Server side reload the entity to get its AI right.
        nse.reload(nse.world, nse.getSpeciesHelper());
    }

    @SubscribeEvent
    public static void breakBlock(BlockEvent.BreakEvent event){
        if(event.getWorld().isRemote) return; // We don't need to do a thing clientside.

        World w = event.getWorld();
        BlockPos place = event.getPos();

        if(w.getBiome(place) != Biomes.JUNGLE && w.getBiome(place) != Biomes.JUNGLE_HILLS && w.getBiome(place) != Biomes.ROOFED_FOREST){
            // We're not in a jungle - so this is irrelevant.
            return;
        }


        IBlockState blockState = event.getState();
        if(blockState.getMaterial() != Material.WOOD){
            return; // We're not cutting wood here
        }

        // Chance to drop shard of sentience
        if(w.rand.nextInt(200) > 5){
            return;
        }

        // We are wanting to drop something here.
        EntityPlayer player = event.getPlayer();

        List<ITextComponent> toSend = new ArrayList<ITextComponent>();
        toSend.add(new TextComponentString("  Something suspicious falls out of the tree.  ").setStyle(new Style().setItalic(true)));
        ChatUtil.sendChat(player, toSend.toArray(new ITextComponent[toSend.size()]));

        EntityItem item = new EntityItem(w, place.getX() + 0.5, place.getY() + 0.5, place.getZ() + 0.5, new ItemStack(ModItems.shardOfSentience, 1));

        // Apply some random motion to the item
        float multiplier = 0.1f;
        float motionX = w.rand.nextFloat() - 0.5f;
        float motionY = w.rand.nextFloat() - 0.5f;
        float motionZ = w.rand.nextFloat() - 0.5f;

        item.motionX = motionX * multiplier;
        item.motionY = motionY * multiplier;
        item.motionZ = motionZ * multiplier;

        // Spawn the item in the world
        w.spawnEntity(item);
    }

}
