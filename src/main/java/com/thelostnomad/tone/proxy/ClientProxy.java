package com.thelostnomad.tone.proxy;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.network.SayWhatKindOfSpriteIsThat;
import com.thelostnomad.tone.network.SpriteEntitySyncPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.registry.ModEntities;
import com.thelostnomad.tone.registry.ModModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private static int id = 0;
    public static Map<Integer, NatureSpriteEntity> lastAskedAbout = new HashMap<Integer, NatureSpriteEntity>();

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        ModModelManager mm = ModModelManager.INSTANCE;
        ModEntities.initModels();
    }

    @SubscribeEvent
    public static void entityEnterWorld(EntityJoinWorldEvent event){
        Entity e = event.getEntity();
        if(!(e instanceof NatureSpriteEntity)){
            return;
        }
        NatureSpriteEntity nse = (NatureSpriteEntity) e;
        if(!e.world.isRemote) return;

        lastAskedAbout.put(id++, nse);

        TonePacketHandler.sendToServer(new SayWhatKindOfSpriteIsThat(e.getPosition()));
        playSpriteSpawnEffect(nse.world, nse.getPosition(), 5);
    }

    private static void playSpriteSpawnEffect(World world, BlockPos pos, int amount){
        for(int i = 0; i < amount; i++){
            double d0 = world.rand.nextGaussian() * 0.02D;
            double d1 = world.rand.nextGaussian() * 0.02D;
            double d2 = world.rand.nextGaussian() * 0.02D;
            world.spawnParticle(EnumParticleTypes.DRAGON_BREATH, (double)((float)pos.getX() +
                    world.rand.nextFloat()), (double)pos.getY() +
                    (double)world.rand.nextFloat() * world.getBlockState(pos).
                            getBoundingBox(world, pos).maxY, (double)((float)
                    pos.getZ() + world.rand.nextFloat()), d0, d1, d2);
        }
    }

    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

}
