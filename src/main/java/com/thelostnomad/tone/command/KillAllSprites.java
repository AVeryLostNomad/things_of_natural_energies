package com.thelostnomad.tone.command;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class KillAllSprites extends CommandBase {

    public KillAllSprites(){
        aliases = Lists.newArrayList("killsprite");
    }

    private final List<String> aliases;

    @Override
    @Nonnull
    public String getName() {
        return "killsprite";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "killsprite";
    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            for(NatureSpriteEntity nse : player.world.getEntities(NatureSpriteEntity.class, new Predicate<NatureSpriteEntity>() {
                @Override
                public boolean apply(@Nullable NatureSpriteEntity input) {
                    return true;
                }
            })){
                nse.setDead();
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

}