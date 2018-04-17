package com.thelostnomad.tone.guide;

import amerifrance.guideapi.api.GuideBook;
import amerifrance.guideapi.api.IGuideBook;
import amerifrance.guideapi.api.impl.Book;
import amerifrance.guideapi.api.impl.abstraction.CategoryAbstract;
import amerifrance.guideapi.category.CategoryItemStack;
import amerifrance.guideapi.entry.EntryItemStack;
import amerifrance.guideapi.page.PageText;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.registry.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

@GuideBook
public class ToneGuidebookT2 implements IGuideBook {

    public static Book book;

    @Nullable
    @Override
    public Book buildBook() {
        book = new Book();
        book.setAuthor("TheLostNomad");
        book.setColor(Color.YELLOW);
        book.setDisplayName("Natural Energies Handbook, Second Edition");
        book.setTitle("Things of Natural Energies v" + ThingsOfNaturalEnergies.VERSION);
        book.setWelcomeMessage("Index");
        book.setCreativeTab(ThingsOfNaturalEnergies.creativeTab);

        // Lore category
        CategoryAbstract testCategory = new CategoryItemStack("lore.expl", new ItemStack(Items.PAPER)).withKeyBase("guide_lore_category");
        testCategory.addEntry("entry", new EntryItemStack("test.entry.name", new ItemStack(Items.POTATO)));
        testCategory.getEntry("entry").addPage(new PageText("Hello, this is\nsome text"));



        book.addCategory(testCategory);

        book.setRegistryName(new ResourceLocation(ThingsOfNaturalEnergies.MODID, "guide_book_tier_2"));
        return book;
    }

    @Nullable
    @Override
    public IRecipe getRecipe(@Nonnull ItemStack bookStack) {
        return new ShapelessOreRecipe(null, bookStack, Items.BOOK, ModItems.shardOfSentience).setRegistryName(book.getRegistryName());
    }
}