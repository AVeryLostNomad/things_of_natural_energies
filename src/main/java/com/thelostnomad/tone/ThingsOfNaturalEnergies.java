package com.thelostnomad.tone;

import com.thelostnomad.tone.command.KillAllSprites;
import com.thelostnomad.tone.command.SpawnSprite;
import com.thelostnomad.tone.command.SpriteInfo;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.entities.nature_sprite.TraitHelper;
import com.thelostnomad.tone.integration.IToneIntegration;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.proxy.CommonProxy;
import com.thelostnomad.tone.util.crafting.CraftTreeBuilder;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = ThingsOfNaturalEnergies.MODID, name = ThingsOfNaturalEnergies.MODNAME, version = ThingsOfNaturalEnergies.VERSION, useMetadata = true, dependencies="before:guideapi")
public class ThingsOfNaturalEnergies {

    public static final String MODID = "thingsofnaturalenergies";
    public static final String MODNAME = "Things of Natural Energies";
    public static final String VERSION = "0.0.1";

    public static final boolean IS_DEV = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    @SidedProxy(clientSide = "com.thelostnomad.tone.proxy.ClientProxy", serverSide = "com.thelostnomad.tone.proxy.ClientProxy")
    public static CommonProxy proxy;

    public static final CreativeTabTone creativeTab = new CreativeTabTone();

    @Mod.Instance
    public static ThingsOfNaturalEnergies instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
        TraitHelper.findAllRecipeConditions(event.getAsmData());
        TraitHelper.findAllSpriteAI(event.getAsmData());
        initializeAllSpriteSongRecipes();

        for(IToneIntegration iti : CommonProxy.toneIntegrations){
            iti.preInit(event);
        }
    }

    private void testPath(String path){
        InputStream url = ThingsOfNaturalEnergies.class.getResourceAsStream(path);
        ThingsOfNaturalEnergies.logger.error("Is null? " + (url == null) + " with path " + path);
    }

    private void initializeAllSpriteSongRecipes(){
        Field[] declaredFields = SpeciesHelper.class.getDeclaredFields();
        List<Field> staticFields = new ArrayList<Field>();
        for(Field f : declaredFields){
            if(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())){
                staticFields.add(f);
            }
        }
        for(Field f : staticFields){
            try {
                SpeciesHelper sh = (SpeciesHelper) f.get(null);
                ThingsOfNaturalEnergies.logger.error("Initializing recipe for " + sh.getInternalName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
        TonePacketHandler.init();

        for(IToneIntegration iti : CommonProxy.toneIntegrations){
            iti.init(e);
        }
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event){
        event.registerServerCommand(new KillAllSprites());
        event.registerServerCommand(new SpawnSprite());
        event.registerServerCommand(new SpriteInfo());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        for(IToneIntegration iti : CommonProxy.toneIntegrations){
            iti.postInit(event);
        }

        CraftTreeBuilder.loadRecipes();
//
//        List<ItemStack> inv = new ArrayList<ItemStack>();
//        inv.add(new ItemStack(Blocks.PLANKS, 2));
//
//        List<CraftTreeBuilder.DirectionalItemStack> dir = CraftTreeBuilder.findProcessToMake(new ItemStack(Items.STICK, 4), inv);
//        if(dir != null){
//            for(CraftTreeBuilder.DirectionalItemStack d : dir){
//                ThingsOfNaturalEnergies.logger.error(d.getStack().getDisplayName() + (d.isAdd() ? " +":" -") + d.getStack().getCount());
//            }
//        }else{
//            ThingsOfNaturalEnergies.logger.error("Can't craft");
//        }
//        FMLCommonHandler.instance().exitJava(0, true);
//
//        List<ItemStack> alreadyHave = new ArrayList<ItemStack>(Arrays.asList(new ItemStack[]{
//            new ItemStack(Blocks.IRON_BLOCK, 2),
//            new ItemStack(Blocks.REDSTONE_BLOCK, 2),
//            new ItemStack(Items.REEDS, 9)
//        }));
//        CraftingOperation co = RecipeUtil.getRequiredItemsToMakeIfPossible(Items.MAP, alreadyHave);
//
//        if(co == null){
//            ThingsOfNaturalEnergies.logger.error("Cannot craft");
//            return;
//        }
//
//        for(Map.Entry<Integer, RecipeUtil.ComparableItem> e : co.getSteps().entrySet()){
//            ThingsOfNaturalEnergies.logger.error(e.getKey() + " : " + e.getValue());
//        }
//        ThingsOfNaturalEnergies.logger.error("You will need to use:");
//        for(Map.Entry<RecipeUtil.ComparableItem, Integer> e : co.getExistingIngredients().entrySet()){
//            ThingsOfNaturalEnergies.logger.error(e.getKey() + " x" + e.getValue());
//        }
//        ThingsOfNaturalEnergies.logger.error("Complexity: " + co.getComplexity());
//        System.exit(0);
    }
}
