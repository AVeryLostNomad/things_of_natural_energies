package com.thelostnomad.tone.registry;

import com.thelostnomad.tone.item.ShardOfSentience;
import com.thelostnomad.tone.item.berries.FuncoBerry;
import com.thelostnomad.tone.item.berries.GlutoBerry;
import com.thelostnomad.tone.item.berries.HastoBerry;
import com.thelostnomad.tone.item.berries.RezzoBerry;
import com.thelostnomad.tone.item.sprites.NatureSpriteItem;
import com.thelostnomad.tone.item.tokens.TokenPullAll;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class ModItems {

    public static TokenPullAll tokenPullAll = new TokenPullAll();
    public static HastoBerry hastoBerryItem = new HastoBerry();
    public static GlutoBerry glutoBerryItem = new GlutoBerry();
    public static FuncoBerry funcoBerryItem = new FuncoBerry();
    public static RezzoBerry rezzoBerryItem = new RezzoBerry();
    public static ItemBlock sentientSaplingItem = new ItemBlock(ModBlocks.sentientSapling);
    public static Item shardOfSentience = new ShardOfSentience();

    public static NatureSpriteItem natureSpriteBaseItem = new NatureSpriteItem("nature_sprite_base");
    public static NatureSpriteItem greedySprite = new NatureSpriteItem("greedy_sprite");
    public static NatureSpriteItem packerSprite = new NatureSpriteItem("packer_sprite");
    public static NatureSpriteItem voidSprite = new NatureSpriteItem("void_sprite");
    public static NatureSpriteItem allegiantSprite = new NatureSpriteItem("allegiant_sprite");
    public static NatureSpriteItem storerSprite = new NatureSpriteItem("storer_sprite");

}
