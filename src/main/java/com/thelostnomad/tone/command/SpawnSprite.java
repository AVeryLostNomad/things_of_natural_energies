package com.thelostnomad.tone.command;

import com.google.common.collect.Lists;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.network.TonePacketHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class SpawnSprite extends CommandBase {

    public SpawnSprite(){
        aliases = Lists.newArrayList("sprite");
    }

    private final List<String> aliases;

    @Override
    @Nonnull
    public String getName() {
        return "sprite";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "sprite <id>";
    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 1) {
            return;
        }

        String type = args[0];
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            ResourceLocation rl = new ResourceLocation("thingsofnaturalenergies", "entities/nature_sprite");
            Entity e= ItemMonsterPlacer.spawnCreature(player.world, rl, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
            NatureSpriteEntity nse = (NatureSpriteEntity) e;
            SpeciesHelper sh = SpeciesHelper.fromInternalName(type);
            nse.setSpeciesHelper(sh);
            nse.reload(player.world, sh);
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

}
