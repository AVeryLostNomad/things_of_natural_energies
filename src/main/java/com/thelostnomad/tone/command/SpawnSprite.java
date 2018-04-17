package com.thelostnomad.tone.command;

import com.google.common.collect.Lists;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.network.SayWhatKindOfSpriteIsThat;
import com.thelostnomad.tone.network.TonePacketHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

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
            NatureSpriteEntity nse = new NatureSpriteEntity(player.world, SpeciesHelper.fromInternalName(type));
            nse.setSpeciesHelper(SpeciesHelper.fromInternalName(type));
            player.world.spawnEntity(nse);
            nse.setPositionAndUpdate(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());

//            TonePacketHandler.sendToServer(new SayWhatKindOfSpriteIsThat(nse.getPosition()));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

}
