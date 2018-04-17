package com.thelostnomad.tone.util.crafting;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// An set of items make up an equivalence stack if the individual items
// can be used to make all other items in a set *without* any other
// ingredients.

// Redstone and redstone blocks, for example, are computationally *the same*
// but things like alchemical blocks (Which require a philosopher's stone each
// craft) are not.

// This allows us to calculate craft trees with swappable ingredients.
// -> You don't need 4 gold ingots if you have a gold block to craft with
public class EquivalenceStack {

    // Hold all forms that this stack can take
    private Map<ItemStack, Integer> amountsByType = new HashMap<ItemStack, Integer>();
    private Map<ItemStack, ArrayList<ConversionSpec>> conversions = new HashMap<ItemStack, ArrayList<ConversionSpec>>();

    public EquivalenceStack copy(){
        EquivalenceStack newStack = new EquivalenceStack();
        newStack.amountsByType = new HashMap<>();
        for(Map.Entry<ItemStack, Integer> stack : this.amountsByType.entrySet()){
            newStack.amountsByType.put(stack.getKey().copy(), new Integer(stack.getValue()));
        }
        newStack.conversions = new HashMap<>();
        for(Map.Entry<ItemStack, ArrayList<ConversionSpec>> stack : this.conversions.entrySet()){
            newStack.conversions.put(stack.getKey().copy(), (ArrayList<ConversionSpec>) stack.getValue().clone());
        }
        return newStack;
    }

    public boolean similarType(EquivalenceStack otherStack){
        for(ItemStack thisKey : conversions.keySet()){
            boolean found = false;
            for(ItemStack other : otherStack.conversions.keySet()){
                if(StackUtil.stacksEqual(thisKey, other)){
                    found = true;
                    break;
                }
            }
            if(!found) return false;
        }
        return true;
    }

    public Map<ItemStack, Integer> getAmountsByType(){
        return amountsByType;
    }

    public void setAmount(ItemStack type, int newAmount){
        ItemStack toChoose = null;
        for(Map.Entry<ItemStack, Integer> e : amountsByType.entrySet()){
            if(StackUtil.stacksEqual(e.getKey(), type)){
                toChoose = e.getKey();
            }
        }
        if(toChoose == null) return;
        amountsByType.put(toChoose, newAmount);
    }

    public boolean hasAmount(ItemStack requested, int amount){
        // Does this stack equate to the requested item
        if(!contains(requested)) return false;

        // Do we already have the perfect amount of this item?
        if(getAmountInInv(requested) >= amount) return true;

        // Can we convert to it?
        for(Map.Entry<ItemStack, ArrayList<ConversionSpec>> e : conversions.entrySet()){
            if(!StackUtil.stacksEqual(e.getKey(), requested)) continue; // We would have caught this if we could.

            for(ConversionSpec cs : e.getValue()){
                if(StackUtil.stacksEqual(cs.convertingTo, requested)){
                    // This is a conversion spec to the target value.
                    int neededConversions = (int) Math.ceil((double) amount / (double) cs.amtToMake);
                    int neededMaterials = cs.amtToTake * neededConversions;
                    int amtHave = getAmountInInv(cs.from);

                    if(amtHave >= neededMaterials){
                        // We have enough to convert to make this item
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean contains(ItemStack stack){
        for(Map.Entry<ItemStack, Integer> entry : amountsByType.entrySet()){
            if(StackUtil.stacksEqual(entry.getKey(), stack)){
                return true;
            }
        }
        return false;
    }

    public int getAmountInInv(ItemStack stack){
        for(Map.Entry<ItemStack, Integer> entry : amountsByType.entrySet()){
            if(StackUtil.stacksEqual(entry.getKey(), stack)){
                return entry.getValue();
            }
        }
        return -1;
    }

    // Add a thing to the amountsByType list
    public void addTemplateComponent(ItemStack[] stacks){
        for(ItemStack stack : stacks){
            boolean skip = false;
            for(ItemStack s : amountsByType.keySet()){
                if(StackUtil.stacksEqual(s, stack)){
                    skip = true;
                    break;
                }
            }
            if(skip) continue;

            amountsByType.put(StackUtil.templateStack(stack), 0);
            return;
        }
    }

    private class ItemTuple {

        public ItemStack stl;
        public Integer resultAmt;

        public ItemTuple(ItemStack stk, Integer res){
            this.stl = stk;
            this.resultAmt = res;
        }

    }

    private ItemTuple getIngredientsNeeded(ItemStack i){
        int smallest = Integer.MAX_VALUE;
        int amtSpitOut = 0;
        ItemStack igType = null;
        List<IRecipe> recipes = CraftTreeBuilder.getRecipe(i);
        for(IRecipe recipe : recipes){
            int ingrdAmt = 0;
            for(Ingredient ig : recipe.getIngredients()){
                if(ig.getMatchingStacks() == null) continue;
                if(ig.getMatchingStacks().length == 0) continue;
                if(ig.getMatchingStacks()[0] == null) continue;
                if(ig.getMatchingStacks()[0].isEmpty()) continue;
                igType = ig.getMatchingStacks()[0];
                amtSpitOut = recipe.getRecipeOutput().getCount();
                ingrdAmt++;
            }
            if(ingrdAmt < smallest){
                smallest = ingrdAmt;
            }
        }
        if(igType == null){
            return null;
        }
        ItemStack toReturn = igType.copy();
        toReturn.setCount(smallest);
        return new ItemTuple(toReturn, amtSpitOut);
    }
    public void retabulateConversionSpecs(){
        // We have an item, here. It is unfortunately unordered.
        // We need to ascertain how this item converts to other items.
        for(ItemStack s : amountsByType.keySet()){
            // We've gotta go through the other items and find the one that goes in front of this.
            ArrayList<ConversionSpec> thisConversionSpecs = new ArrayList<>();
            for(ItemStack i : amountsByType.keySet()){
                if(StackUtil.stacksEqual(i, s)) continue;

                // We're checking things that are not the same
                ItemTuple neededForS = getIngredientsNeeded(s);
                ItemTuple neededForI = getIngredientsNeeded(i);
                if(neededForI != null && StackUtil.stacksEqual(neededForI.stl, s)){
                    ConversionSpec spec = new ConversionSpec(s, i, neededForI.stl.getCount(), neededForI.resultAmt);
                    if(!thisConversionSpecs.contains(spec)){
                        thisConversionSpecs.add(spec);
                    }
                }
                if(neededForS != null && StackUtil.stacksEqual(neededForS.stl, i)){
                    ConversionSpec spec = new ConversionSpec(i, s, neededForS.stl.getCount(), neededForS.resultAmt);
                    if(!thisConversionSpecs.contains(spec)){
                        thisConversionSpecs.add(spec);
                    }
                }
            }
            conversions.put(s, thisConversionSpecs);
        }
        // The thing should now contain several conversion specs.
    }

    public boolean containsAny(ItemStack[] stacks){
        for(ItemStack stack : stacks){
            if(contains(stack)){
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(ItemStack[] stacks){
        for(ItemStack stack : stacks){
            if(!contains(stack)){
                return false;
            }
        }
        return true;
    }

    public void printContents(){
        for(Map.Entry<ItemStack, Integer> ez : amountsByType.entrySet()){
            ThingsOfNaturalEnergies.logger.error(ez.getKey().getDisplayName() + " and amount " + ez.getValue());
        }
    }

    // This should be the primary means of interacting with
    // equivalence stacks. We remove the thing we want, and conversion
    // logic is handled internally
    //
    // Remove a specific amount of an item, converting if necessary.
    // Returns the removed itemstack, if possible, otherwise
    // return null.
    public ItemStack removeAmount(ItemStack requested, int amount){
        // Does this stack equate to the requested item
        if(!contains(requested)) return null;

        // Do we already have the perfect amount of this item?
        if(getAmountInInv(requested) >= amount){
            ItemStack toTakeFrom = null;
            for(Map.Entry<ItemStack, Integer> ez : amountsByType.entrySet()){
                if(StackUtil.stacksEqual(ez.getKey(), requested)){
                    toTakeFrom = ez.getKey();
                    break;
                }
            }
            amountsByType.put(toTakeFrom, getAmountInInv(toTakeFrom) - amount);
            ItemStack toReturn = requested.copy();
            toReturn.setCount(amount);
            return toReturn;
        }

        // Can we convert to it?
        for(Map.Entry<ItemStack, ArrayList<ConversionSpec>> e : conversions.entrySet()){
            if(StackUtil.stacksEqual(requested, e.getKey())) continue; // We would have caught this if we could.

            for(ConversionSpec cs : e.getValue()){
                if(StackUtil.stacksEqual(cs.convertingTo, requested)){
                    // This is a conversion spec to the target value.
                    int neededConversions = (int) Math.ceil((double) amount / (double) cs.amtToMake);
                    int neededMaterials = cs.amtToTake * neededConversions;
                    int amtHave = getAmountInInv(cs.from);

                    if(amtHave >= neededMaterials){
                        // We have enough to convert to make this item
                        ItemStack toTakeFrom = null;
                        for(Map.Entry<ItemStack, Integer> ez : amountsByType.entrySet()){
                            if(StackUtil.stacksEqual(ez.getKey(), e.getKey())){
                                toTakeFrom = ez.getKey();
                                break;
                            }
                        }
                        ItemStack toAddTo = null;
                        for(Map.Entry<ItemStack, Integer> ez : amountsByType.entrySet()){
                            if(StackUtil.stacksEqual(ez.getKey(), cs.convertingTo)){
                                toAddTo = ez.getKey();
                                break;
                            }
                        }
                        // A conversion has just happened
                        amountsByType.put(toTakeFrom, getAmountInInv(toTakeFrom) - neededMaterials);
                        amountsByType.put(toAddTo, getAmountInInv(toAddTo) + (neededConversions * cs.amtToMake));
                    }
                }
            }
        }

        // Go back through and try to do the thing once more.
        if(getAmountInInv(requested) >= amount){
            ItemStack toTakeFrom = null;
            for(Map.Entry<ItemStack, Integer> ez : amountsByType.entrySet()){
                if(StackUtil.stacksEqual(ez.getKey(), requested)){
                    toTakeFrom = ez.getKey();
                    break;
                }
            }
            amountsByType.put(toTakeFrom, getAmountInInv(toTakeFrom) - amount);
            ItemStack toReturn = requested.copy();
            toReturn.setCount(amount);
            return toReturn;
        }

        // We still couldn't do it. (Not enough to convert with, etc...)
        return null;
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        for(Map.Entry<ItemStack, ArrayList<ConversionSpec>> e : conversions.entrySet()){
            String s = "";
            for(ConversionSpec cs : e.getValue()){
                s += cs.toString();
                s += "\n";
            }
            String thisLine = e.getKey().getUnlocalizedName() + " - \n" + s;
            toReturn.append(thisLine);
        }
        return toReturn.toString();
    }

    private class ConversionSpec {
        int amtToTake = 0;
        int amtToMake = 0;
        ItemStack convertingTo;
        ItemStack from;

        public ConversionSpec(ItemStack from, ItemStack other, int amtNeeded, int amtToMake){
            this.from = from;
            this.convertingTo = other;
            this.amtToTake = amtNeeded;
            this.amtToMake = amtToMake;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof ConversionSpec)){
                return false;
            }
            ConversionSpec other = (ConversionSpec) obj;
            return StackUtil.stacksEqual(other.convertingTo, convertingTo) && StackUtil.stacksEqual(other.from, from);
        }

        @Override
        public String toString() {
            return from.getDisplayName() + "x" + amtToTake + " -> " + convertingTo.getDisplayName() + "x" + amtToMake;
        }
    }

}
