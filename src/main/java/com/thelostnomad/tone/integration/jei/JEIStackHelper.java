package com.thelostnomad.tone.integration.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

/** The JEI stack helper (by mezz), important parts copied here to avoid JEI internal-change crashes. Notable change: No UID cache */
public class JEIStackHelper {

    // The main needed method here

    /**
     * Get the equivalent oredict entry for a list of ItemStacks
     */
    @Nullable
    public String getOreDictEquivalent(@Nonnull Collection<ItemStack> itemStacks) {
        if (itemStacks.size() < 2) {
            return null;
        }

        final ItemStack firstStack = itemStacks.iterator().next();
        if (firstStack != null) {
            for (final int oreId : OreDictionary.getOreIDs(firstStack)) {
                final String oreName = OreDictionary.getOreName(oreId);
                List<ItemStack> ores = OreDictionary.getOres(oreName);
                ores = getAllSubtypes(ores);
                if (containsSameStacks(itemStacks, ores)) {
                    return oreName;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if all stacks from "contains" are found in "stacks" and the opposite is true as well.
     */
    public boolean containsSameStacks(@Nonnull Iterable<ItemStack> stacks, @Nonnull Iterable<ItemStack> contains) {
        for (ItemStack stack : contains) {
            if (containsStack(stacks, stack) == null) {
                return false;
            }
        }

        for (ItemStack stack : stacks) {
            if (containsStack(contains, stack) == null) {
                return false;
            }
        }

        return true;
    }

    /* Returns an ItemStack from "stacks" if it isEquivalent to an ItemStack from "contains" */
    @Nullable
    public ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable Iterable<ItemStack> contains) {
        if (stacks == null || contains == null) {
            return null;
        }

        for (ItemStack containStack : contains) {
            ItemStack matchingStack = containsStack(stacks, containStack);
            if (matchingStack != null) {
                return matchingStack;
            }
        }

        return null;
    }

    /* Returns an ItemStack from "stacks" if it isEquivalent to "contains" */
    @Nullable
    public ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable ItemStack contains) {
        if (stacks == null || contains == null) {
            return null;
        }

        for (ItemStack stack : stacks) {
            if (isEquivalent(contains, stack)) {
                return stack;
            }
        }
        return null;
    }

    /**
     * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the INbtIgnoreList
     */
    public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs) {
        if (lhs == rhs) {
            return true;
        }

        if (lhs == null || rhs == null) {
            return false;
        }

        if (lhs.getItem() != rhs.getItem()) {
            return false;
        }

        if (lhs.getMetadata() != OreDictionary.WILDCARD_VALUE) {
            if (lhs.getMetadata() != rhs.getMetadata()) {
                return false;
            }
        }

        if (lhs.getHasSubtypes()) {
            String keyLhs = getUniqueIdentifierForStack(lhs, UidMode.NORMAL);
            String keyRhs = getUniqueIdentifierForStack(rhs, UidMode.NORMAL);
            return Objects.equals(keyLhs, keyRhs);
        } else {
            return true;
        }
    }

    @Nonnull
    public List<ItemStack> getSubtypes(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyList();
        }

        Item item = itemStack.getItem();
        if (item == null) {
            return Collections.emptyList();
        }

        if (itemStack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            return Collections.singletonList(itemStack);
        }

        return getSubtypes(item, itemStack.getCount());
    }

    @Nonnull
    public List<ItemStack> getSubtypes(@Nonnull final Item item, final int stackSize) {
        List<ItemStack> itemStacks = new ArrayList<>();

        for (CreativeTabs itemTab : item.getCreativeTabs()) {
            List<ItemStack> subItems = new ArrayList<>();
            try {
                subItems = ToneJEIIntegration.jeiHelpers.getStackHelper().getSubtypes(new ItemStack(item, 1));
            } catch (RuntimeException | LinkageError e) {
            }
            for (ItemStack subItem : subItems) {
                if (subItem == null) {
                } else if (subItem.getItem() == null) {
                } else {
                    if (subItem.getCount() != stackSize) {
                        ItemStack subItemCopy = subItem.copy();
                        subItemCopy.setCount(stackSize);
                        itemStacks.add(subItemCopy);
                    } else {
                        itemStacks.add(subItem);
                    }
                }
            }
        }

        return itemStacks;
    }

    @Nonnull
    public List<ItemStack> getAllSubtypes(@Nullable Iterable stacks) {
        if (stacks == null) {
            return Collections.emptyList();
        }

        List<ItemStack> allSubtypes = new ArrayList<>();
        getAllSubtypes(allSubtypes, stacks);
        return allSubtypes;
    }

    private void getAllSubtypes(@Nonnull List<ItemStack> subtypesList, @Nonnull Iterable stacks) {
        for (Object obj : stacks) {
            if (obj instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) obj;
                List<ItemStack> subtypes = getSubtypes(itemStack);
                subtypesList.addAll(subtypes);
            } else if (obj instanceof Iterable) {
                getAllSubtypes(subtypesList, (Iterable) obj);
            } else if (obj != null) {
            }
        }
    }

    @Nonnull
    public String getUniqueIdentifierForStack(@Nonnull ItemStack stack) {
        return getUniqueIdentifierForStack(stack, UidMode.NORMAL);
    }

    @Nonnull
    public String getUniqueIdentifierForStack(@Nonnull ItemStack stack, @Nonnull UidMode mode) {
        Item item = stack.getItem();
        if (item == null) {
            throw new NullPointerException("Found an itemStack with a null item. This is an error from another mod.");
        }

        int metadata = stack.getMetadata();
        if (mode == UidMode.WILDCARD || metadata == OreDictionary.WILDCARD_VALUE) {
            ResourceLocation itemName = Item.REGISTRY.getNameForObject(item);
            if (itemName == null) {
                throw new NullPointerException("Item is not registered. Item.REGISTRY.getNameForObject returned null");
            }
            return itemName.toString();
        }

        NBTTagCompound serializedNbt = stack.serializeNBT();
        StringBuilder itemKey = new StringBuilder(serializedNbt.getString("id"));
        if (mode == UidMode.FULL) {
            itemKey.append(':').append(metadata);

            NBTTagCompound nbtTagCompound = serializedNbt.getCompoundTag("tag");
            if (serializedNbt.hasKey("ForgeCaps")) {
                if (nbtTagCompound == null) {
                    nbtTagCompound = new NBTTagCompound();
                }
                nbtTagCompound.setTag("ForgeCaps", serializedNbt.getCompoundTag("ForgeCaps"));
            }
            if (nbtTagCompound != null && !nbtTagCompound.hasNoTags()) {
                itemKey.append(':').append(nbtTagCompound);
            }
        } else if (stack.getHasSubtypes()) {
            itemKey.append(':').append(metadata);

            String subtypeInfo = String.valueOf(ToneJEIIntegration.jeiHelpers.getStackHelper().getSubtypes(stack).size());
            if (subtypeInfo != null) {
                itemKey.append(':').append(subtypeInfo);
            }
        }

        String result = itemKey.toString();
        return result;
    }

    public enum UidMode {
        NORMAL,
        WILDCARD,
        FULL
    }

    public static class MatchingItemsResult {

        @Nonnull
        public final Map<Integer, ItemStack> matchingItems = new HashMap<>();
        @Nonnull
        public final List<Integer> missingItems = new ArrayList<>();
    }
}