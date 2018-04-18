package com.thelostnomad.tone.registry;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.integration.IToneIntegration;
import com.thelostnomad.tone.proxy.CommonProxy;
import com.thelostnomad.tone.util.IVariant;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.function.ToIntFunction;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ThingsOfNaturalEnergies.MODID)
public class ModModelManager {

    public static final ModModelManager INSTANCE = new ModModelManager();
    private static final String FLUID_MODEL_PATH = "thingsofnaturalenergies:fluid";

    @SubscribeEvent
    public static void registerAllModels(final ModelRegistryEvent event) {
        INSTANCE.registerFluidModels();
        INSTANCE.registerBlockModels();
        INSTANCE.registerItemModels();
    }

    private void registerItemModels() {
        registerItemModel(ModItems.tokenPullAll, "thingsofnaturalenergies:token_pull_all");
        registerItemModel(ModItems.hastoBerryItem, "thingsofnaturalenergies:hasto_berry_item");
        registerItemModel(ModItems.glutoBerryItem, "thingsofnaturalenergies:gluto_berry_item");
        registerItemModel(ModItems.funcoBerryItem, "thingsofnaturalenergies:funco_berry_item");
        registerItemModel(ModItems.rezzoBerryItem, "thingsofnaturalenergies:rezzo_berry_item");
        registerItemModel(new ItemBlock(ModBlocks.sentientSapling), "thingsofnaturalenergies:sentient_sapling");
        registerItemModel(ModItems.shardOfSentience, "thingsofnaturalenergies:shard_of_sentience");

        registerItemModel(ModItems.natureSpriteBaseItem, "thingsofnaturalenergies:nature_sprite_base");
        registerItemModel(ModItems.greedySprite, "thingsofnaturalenergies:greedy_sprite_base");
        registerItemModel(ModItems.packerSprite, "thingsofnaturalenergies:packer_sprite_base");
        registerItemModel(ModItems.voidSprite, "thingsofnaturalenergies:void_sprite_base");
    }

    private void registerFluidModels() {
//        ModFluids.MOD_FLUID_BLOCKS.forEach(this::registerFluidModel);
    }

    private void registerItemModel(final Item item) {
        registerItemModel(item, item.getRegistryName().toString());
    }

    /**
     * Register a single model for an {@link Item}.
     * <p>
     * Uses {@code modelLocation} as the domain/path and {@link "inventory"} as the variant.
     *
     * @param item          The Item
     * @param modelLocation The model location
     */

    /**
     * Register the block and item model for a .
     *
     * @param fluidBlock The Fluid's Block
     */
    private void registerFluidModel(final IFluidBlock fluidBlock) {
        final Item item = Item.getItemFromBlock((Block) fluidBlock);
        assert item != Items.AIR;

        ModelBakery.registerItemVariants(item);

        final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(FLUID_MODEL_PATH, fluidBlock.getFluid().getName());

        ModelLoader.setCustomMeshDefinition(item, MeshDefinitionFix.create(stack -> modelResourceLocation));

        ModelLoader.setCustomStateMapper((Block) fluidBlock, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(final IBlockState p_178132_1_) {
                return modelResourceLocation;
            }
        });
    }

    private void registerBlockModels() {
        registerBlockItemModel(
                ModBlocks.sentientLog.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.storageHollowBasic.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.storageHollowBig.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.storageHollowLarge.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.storageHollowMassive.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.storageHollowGargantuan.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.storageHollowQuiteBig.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.storageHollowEvenBigger.getDefaultState()
        );
//        registerBlockItemModel(
//                ModBlocks.storageHollowSingularity.getDefaultState()
//        );
        registerBlockItemModel(
                ModBlocks.blockKeeper.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.blockAcceptor.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.focusPusher.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.fluidHollowBasic.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.rootsBlock.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.sentientTreeCore.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.sentientLeaves.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.blockPuller.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.blockPusher.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.hastoBerry.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.glutoBerry.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.funcoBerry.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.rezzoBerry.getDefaultState()
        );
        registerBlockItemModel(
                ModBlocks.sentientSapling.getDefaultState()
        );
//        registerBlockItemModel(
//                ModBlocks.transmutationGas.getDefaultState()
//        );
        registerBlockItemModel(
                ModBlocks.livingCraftingStation.getDefaultState()
        );

        for(IToneIntegration iti : CommonProxy.toneIntegrations){
            if(Loader.isModLoaded(iti.getIntegrationModid())) {
                for (IBlockState ibs : iti.getModelBlockStates()) {
                    registerBlockItemModel(ibs);
                }
            }
        }
    }

    private final StateMapperBase propertyStringMapper = new StateMapperBase() {
        @Override
        protected ModelResourceLocation getModelResourceLocation(final IBlockState state) {
            return new ModelResourceLocation("minecraft:air");
        }
    };

    private <T extends Comparable<T>> void registerVariantBlockItemModelsNoMeta(final IBlockState baseState, final IProperty<T> property) {
        property.getAllowedValues().forEach(value -> registerBlockItemModelForMeta(baseState.withProperty(property, value), 0));
    }

    /**
     * Register a model for each metadata value of the {@link Block}'s {@link Item} corresponding to the values of an {@link IProperty}.
     * <p>
     * For each value:
     * <li>The domain/path is the registry name</li>
     * <li>The variant is {@code baseState} with the {@link IProperty} set to the value</li>
     * <p>
     * The {@code getMeta} function is used to get the metadata of each value.
     *
     * @param baseState The base state to use for the variant
     * @param property  The property whose values should be used
     * @param getMeta   A function to get the metadata of each value
     * @param <T>       The value type
     */
    private <T extends Comparable<T>> void registerVariantBlockItemModels(final IBlockState baseState, final IProperty<T> property, final ToIntFunction<T> getMeta) {
        property.getAllowedValues().forEach(value -> registerBlockItemModelForMeta(baseState.withProperty(property, value), getMeta.applyAsInt(value)));
    }

    private void registerBlockItemModel(final IBlockState state) {
        final Block block = state.getBlock();
        final Item item = Item.getItemFromBlock(block);

        if (item != Items.AIR) {
            registerItemModel(item, new ModelResourceLocation(block.getRegistryName(), propertyStringMapper.getPropertyString(state.getProperties())));
        }
    }

    /**
     * Register a model for a metadata value of the {@link Block}'s {@link Item}.
     * <p>
     * Uses the registry name as the domain/path and the {@link IBlockState} as the variant.
     *
     * @param state    The state to use as the variant
     * @param metadata The item metadata to register the model for
     */
    private void registerBlockItemModelForMeta(final IBlockState state, final int metadata) {
        final Item item = Item.getItemFromBlock(state.getBlock());

        if (item != Items.AIR) {
            registerItemModelForMeta(item, metadata, propertyStringMapper.getPropertyString(state.getProperties()));
        }
    }

    /**
     * Register a model for a metadata value an {@link Item}.
     * <p>
     * Uses the registry name as the domain/path and {@code variant} as the variant.
     *
     * @param item     The Item
     * @param metadata The metadata
     * @param variant  The variant
     */
    private void registerItemModelForMeta(final Item item, final int metadata, final String variant) {
        registerItemModelForMeta(item, metadata, new ModelResourceLocation(item.getRegistryName(), variant));
    }

    /**
     * Register a model for a metadata value of an {@link Item}.
     * <p>
     * Uses {@code modelResourceLocation} as the domain, path and variant.
     *
     * @param item                  The Item
     * @param metadata              The metadata
     * @param modelResourceLocation The full model location
     */
    private void registerItemModelForMeta(final Item item, final int metadata, final ModelResourceLocation modelResourceLocation) {
        ModelLoader.setCustomModelResourceLocation(item, metadata, modelResourceLocation);
    }

    /**
     * Register a model for each metadata value of the {@link Block}'s {@link Item} corresponding to the values of an {@link IProperty}.
     * <p>
     * For each value:
     * <li>The domain/path is the registry name</li>
     * <li>The variant is {@code baseState} with the {@link IProperty} set to the value</li>
     * <p>
     * {@link IVariant#getMeta()} is used to get the metadata of each value.
     *
     * @param baseState The base state to use for the variant
     * @param property  The property whose values should be used
     * @param <T>       The value type
     */
    private <T extends IVariant & Comparable<T>> void registerVariantBlockItemModels(final IBlockState baseState, final IProperty<T> property) {
        registerVariantBlockItemModels(baseState, property, IVariant::getMeta);
    }

    private void registerItemModel(final Item item, final String modelLocation) {
        final ModelResourceLocation fullModelLocation = new ModelResourceLocation(modelLocation, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, fullModelLocation);
        registerItemModel(item, fullModelLocation);
    }

    private void registerItemModel(final Item item, final ModelResourceLocation fullModelLocation) {
        ModelBakery.registerItemVariants(item, fullModelLocation); // Ensure the custom model is loaded and prevent the default model from being loaded
        registerItemModel(item, MeshDefinitionFix.create(stack -> fullModelLocation));
    }

    private void registerItemModel(final Item item, final ItemMeshDefinition meshDefinition) {
        ModelLoader.setCustomMeshDefinition(item, meshDefinition);
    }

}
