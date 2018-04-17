package com.thelostnomad.tone.command;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.util.ChatUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SpriteInfo extends CommandBase {

    public SpriteInfo(){
        aliases = Lists.newArrayList("spriteinfo");
    }

    private final List<String> aliases;

    @Override
    @Nonnull
    public String getName() {
        return "spriteinfo";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "spriteinfo";
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
                ChatUtil.sendChat(player, "That sprite has species helper of val {" + nse.getSpeciesHelper().getInternalName() + "} (" + nse.tasks.taskEntries.size() + ")");
                for(EntityAITasks.EntityAITaskEntry taskEntry : nse.tasks.taskEntries){
                    ChatUtil.sendChat(player, " -> " + taskEntry.action.getClass().getCanonicalName());
                }
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

}
