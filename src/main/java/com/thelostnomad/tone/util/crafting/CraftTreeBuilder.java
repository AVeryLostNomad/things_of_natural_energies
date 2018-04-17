package com.thelostnomad.tone.util.crafting;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class CraftTreeBuilder {

    private static final int DEPTH_LIMIT = 10;
    private static List<IRecipe> recipesLoaded;
    public static List<EquivalenceStack> equivalenceStacks;

    public static void loadRecipes() {
        Set<ResourceLocation> iterator = CraftingManager.REGISTRY.getKeys();
        recipesLoaded = new ArrayList<IRecipe>();
        for (ResourceLocation r : iterator) {
            IRecipe thisRecipe = CraftingManager.REGISTRY.getObject(r);
            recipesLoaded.add(thisRecipe);
        }
        isolateEquivalenceStacks();
    }

    public static List<IRecipe> getRecipe(ItemStack o){
        List<IRecipe> possibleRecipes = new ArrayList<>();
        for (IRecipe rec : recipesLoaded) {
            if(rec.getRecipeOutput().isItemEqual(o)){
                possibleRecipes.add(rec);
            }
        }
        return possibleRecipes;
    }

    private static ItemStack getFirstNonempty(ItemStack[] array){
        for(ItemStack stack : array){
            if(stack == null) continue;
            if(stack.isEmpty()) continue;
            return stack;
        }
        return null;
    }

    private static int countIngredients(IRecipe recipe){
        Map<ItemStack, Integer> ingredientMap = new HashMap<ItemStack, Integer>();
        for(Ingredient i : recipe.getIngredients()){
            if(i == null) continue;
            ItemStack stack = getFirstNonempty(i.getMatchingStacks());
            if(stack == null) continue;
            if(ingredientMap.containsKey(stack)){
                ingredientMap.put(stack, ingredientMap.get(stack) + 1);
            }else{
                ingredientMap.put(stack, 1);
            }
        }
        return ingredientMap.keySet().size();
    }

    public static void isolateEquivalenceStacks(){
        equivalenceStacks = new ArrayList<EquivalenceStack>();

        // Go through every single recipe
        for(IRecipe recipe : recipesLoaded){
            ItemStack thisItem = recipe.getRecipeOutput();
            // We have a recipe for this item.
            // How many ingredients does it have?

            if(countIngredients(recipe) != 1){
                // We have more than one/less than one ingredient. Disclude. Not an equivalence stack.
                continue;
            }

            boolean gotThisRecipe = false;
            // We might have an equivalence stack, but we must first ensure that you can craft the ingredients using thisItems
            for(ItemStack ig : recipe.getIngredients().get(0).getMatchingStacks()){
                // Go through the first stack of matching ingredients, since it's the only stack
                for(IRecipe testRecipe : getRecipe(ig)){
                    // See if this item only has one ingredient!
                    if(countIngredients(testRecipe) != 1){
                        continue;
                    }

                    // It does! Is that ingredient equal to this?
                    for(ItemStack isCompare : testRecipe.getIngredients().get(0).getMatchingStacks()){
                        if(StackUtil.stacksEqual(thisItem, isCompare)){
                            // By jove, we've got a match! This is an equivalence stack!
                            // TODO EQUISTACK
                            addEquivalenceStackIfNotExists(thisItem, ig);
                            gotThisRecipe = true;
                            break;
                        }
                    }
                    if(gotThisRecipe){
                        break;
                    }
                }
                if(gotThisRecipe){
                    break;
                }
            }
        }
    }

    private static void addEquivalenceStackIfNotExists(ItemStack ... listOfEquivalencies){
        // First ensure that we do not already have this equistack in place.
        for(EquivalenceStack es : equivalenceStacks){
            if(es.containsAll(listOfEquivalencies)){
                // This stack already exists entirely. Skip
                return;
            }
            if(es.containsAny(listOfEquivalencies)){
                // This stack contains one of our items, let's go ahead and add the other.
                es.addTemplateComponent(listOfEquivalencies);
                es.retabulateConversionSpecs();
                return;
            }
        }
        EquivalenceStack es = new EquivalenceStack();
        es.addTemplateComponent(listOfEquivalencies);
        es.retabulateConversionSpecs();
        equivalenceStacks.add(es);
    }

    public static boolean isContainedInEquivalenceStack(Ingredient i){
        for(EquivalenceStack es : equivalenceStacks){
            for(ItemStack stk : i.getMatchingStacks()){
                if(es.contains(stk)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isContainedInEquivalenceStack(ItemStack i){
        for(EquivalenceStack es : equivalenceStacks){
            if(es.contains(i)){
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> findMissingItems(ItemStack target, List<ItemStack> inventory){
        InventoryWrapper wrap = new InventoryWrapper(inventory);

        RecipeBranch baseBranch = new RecipeBranch(null, target, 0, 1);

        printAllRecipeBranches(baseBranch);

        boolean canDoCraft = canCraft(wrap.copy(), baseBranch);
        if(!canDoCraft){
            return missingWhat(wrap.copy(), baseBranch);
        }
        return null;
    }

    // If it returns null, the craft was impossible.
    // If it returns any other value, the craft was possible and we were able to get that result.
    public static List<DirectionalItemStack> findProcessToMake(ItemStack target, List<ItemStack> inventory){
        InventoryWrapper wrap = new InventoryWrapper(inventory);

        RecipeBranch baseBranch = new RecipeBranch(null, target, 0, target.getCount());

        //printAllRecipeBranches(baseBranch);

        boolean canDoCraft = canCraft(wrap.copy(), baseBranch);
        if(canDoCraft){
            InventoryWrapper result = doCraft(wrap.copy(), baseBranch);
            //result.printDifferences(wrap);
            return result.getDifferencesInStackForm(wrap);
        }
        return null;
    }

    private static void printAllRecipeBranches(RecipeBranch branch){
        String spaces = "";
        for(int i = 0; i < branch.depth; i++){
            spaces += " ";
        }
        for(RecipeBranch rb : branch.getSubBranches()){
            printAllRecipeBranches(rb);
        }
    }

    private static List<ItemStack> getOverspill(RecipeBranch branch){
        List<ItemStack> toReturn = new ArrayList<>();
        toReturn.addAll(branch.overspill);
        return toReturn;
    }

    private static InventoryWrapper doCraft(InventoryWrapper inventory, RecipeBranch branch){
        inventory.addOverspill(getOverspill(branch)); // Handle extra items

        if(branch.getSubBranches().size() == 0 && branch.depth == 0){
            // Literally a single ingredient
            ItemStack targetGoal = branch.target.copy();
            targetGoal.setCount(branch.amtMade);
            if(inventory.hasItemstack(targetGoal)){
                inventory.getItemstack(targetGoal);
            }
        }

        for(RecipeBranch rb : branch.getSubBranches()){
            // For each one, do we have that item? If not, return
            ItemStack targetGoal = rb.target.copy();
            targetGoal.setCount(rb.amtMade);
            if(inventory.hasItemstack(targetGoal)){
                // We do have it, but we probably should remove one off the top
                inventory.getItemstack(targetGoal);
            }else{
                // We do not have the item.
                // Is there any way we might be able to make it from its parts?
                if(rb.getSubBranches().size() != 0){
                    // We have a chance, there are subbranches here
                    doCraft(inventory, rb);
                }
            }
        }
        return inventory;
    }

    private static List<ItemStack> missingWhat(InventoryWrapper inventory, RecipeBranch branch){
        List<ItemStack> totalMissing = new ArrayList<ItemStack>();

        if(branch.getSubBranches().size() == 0 && branch.depth == 0){
            // Literally a single ingredient
            ItemStack targetGoal = branch.target.copy();
            targetGoal.setCount(branch.amtMade);
            if(inventory.hasItemstack(targetGoal)){
                inventory.getItemstack(targetGoal);
            }else{
                totalMissing.add(targetGoal);
            }
        }

        for(RecipeBranch rb : branch.getSubBranches()){
            // For each one, do we have that item? If not, return
            ItemStack targetGoal = rb.target.copy();
            targetGoal.setCount(rb.amtMade);
            if(inventory.hasItemstack(targetGoal)){
                // We do have it, but we probably should remove one off the top
                inventory.getItemstack(targetGoal);
            }else{
                // We do not have the item.
                // Is there any way we might be able to make it from its parts?
                if(rb.getSubBranches().size() != 0){
                    // We have a chance, there are subbranches here
                    totalMissing.addAll(missingWhat(inventory, rb));
                }else{
                    // There is no way to get this item.
                    totalMissing.add(targetGoal);
                }
            }
        }
        return totalMissing;
    }

    private static boolean canCraft(InventoryWrapper inventory, RecipeBranch branch){
        boolean totalApprox = true;

        if(branch.getSubBranches().size() == 0 && branch.depth == 0){
            // Literally a single ingredient
            ItemStack targetGoal = branch.target.copy();
            targetGoal.setCount(branch.amtMade);
            if(inventory.hasItemstack(targetGoal)){
                inventory.getItemstack(targetGoal);
            }else{
                totalApprox = false;
            }
        }

        for(RecipeBranch rb : branch.getSubBranches()){
            // For each one, do we have that item? If not, return
            ItemStack targetGoal = rb.target.copy();
            targetGoal.setCount(rb.amtMade);
            if(inventory.hasItemstack(targetGoal)){
                // We do have it, but we probably should remove one off the top
                inventory.getItemstack(targetGoal);
            }else{
                // We do not have the item.
                // Is there any way we might be able to make it from its parts?
                if(rb.getSubBranches().size() != 0){
                    // We have a chance, there are subbranches here
                    if(!canCraft(inventory, rb)){
                        totalApprox = false;
                    }
                }else{
                    // There is no way to get this item.
                    totalApprox = false;
                    break;
                }
            }
        }
        return totalApprox;
    }

    public static EquivalenceStack getEquivalenceStack(ItemStack i){
        for(EquivalenceStack es : equivalenceStacks){
            if(es.contains(i)){
                return es;
            }
        }
        return null;
    }

    public static class DirectionalItemStack {

        private boolean add = false;
        private ItemStack stack;

        public DirectionalItemStack(boolean add, ItemStack stack){
            this.stack = stack;
            this.add = add;
        }

        public boolean isAdd() {
            return add;
        }

        public ItemStack getStack() {
            return stack;
        }
    }

    public static class InventoryWrapper {

        private List<ItemStack> standardStacks;
        private List<EquivalenceStack> equivalenceStacks;

        // Send in an player inventory, this wrapper will convert it into an interactable inventory
        // object.

        private InventoryWrapper() {}

        public InventoryWrapper(List<ItemStack> playerInventory){
            standardStacks = new ArrayList<>();
            equivalenceStacks = new ArrayList<>();

            for(ItemStack s : playerInventory){
                // Is this item something that is in "any" equivalence stack?
                if(isContainedInEquivalenceStack(s)){
                    // This is an equivalence stack item.
                    // First go through and see if we have one in our list
                    EquivalenceStack found = null;
                    for(EquivalenceStack es : equivalenceStacks){
                        if(es.contains(s)){
                            // This stack does indeed have that item type!
                            found = es;
                            break;
                        }
                    }
                    if(found != null){
                        found.setAmount(s, s.getCount() + found.getAmountInInv(s));
                    }else{
                        // We don't yet have an equivalence stack of this type.
                        EquivalenceStack template = getEquivalenceStack(s);
                        EquivalenceStack toAdd = template.copy();
                        toAdd.setAmount(s, s.getCount());

                        equivalenceStacks.add(toAdd);
                    }
                }

                // This is a normal item
                standardStacks.add(s);
            }
        }

        public void addOverspill(List<ItemStack> stack){
            for(ItemStack s : stack){
                if(isContainedInEquivalenceStack(s)){
                    // This is an equivalence stack item.
                    // First go through and see if we have one in our list
                    EquivalenceStack found = null;
                    for(EquivalenceStack es : equivalenceStacks){
                        if(es.contains(s)){
                            // This stack does indeed have that item type!
                            found = es;
                            break;
                        }
                    }
                    if(found != null){
                        found.setAmount(s, s.getCount() + found.getAmountInInv(s));
                    }else{
                        // We don't yet have an equivalence stack of this type.
                        EquivalenceStack template = getEquivalenceStack(s);
                        EquivalenceStack toAdd = template.copy();
                        toAdd.setAmount(s, s.getCount());

                        equivalenceStacks.add(toAdd);
                    }
                }

                // This is a normal item
                ItemStack toMod = null;
                for(ItemStack ourItems : standardStacks){
                    if(StackUtil.stacksEqual(ourItems, s)){
                        toMod = ourItems;
                    }
                }
                if(toMod != null){
                    toMod.setCount(toMod.getCount() + s.getCount());
                }else{
                    standardStacks.add(s);
                }
            }
        }

        public InventoryWrapper copy(){
            InventoryWrapper wrap = new InventoryWrapper();
            wrap.equivalenceStacks = new ArrayList<>();
            for(EquivalenceStack es : this.equivalenceStacks){
                wrap.equivalenceStacks.add(es.copy());
            }
            wrap.standardStacks = new ArrayList<>();
            for(ItemStack is : this.standardStacks){
                wrap.standardStacks.add(is.copy());
            }
            return wrap;
        }

        @Override
        public String toString() {
            String inv = "Inventory Contents:\n";
            for(ItemStack is : standardStacks){
                inv += is.getDisplayName() + "\n";
            }

            for(EquivalenceStack es : equivalenceStacks){
                inv += es.toString();
            }

            return inv;
        }

        public List<DirectionalItemStack> getDifferencesInStackForm(InventoryWrapper other){
            List<DirectionalItemStack> differences = new ArrayList<>();
            for(ItemStack is : standardStacks){
                boolean found = false;
                for(ItemStack os : other.standardStacks){
                    if(StackUtil.stacksEqual(is, os)){
                        found = true;
                        if(is.getCount() != os.getCount()){
                            int quantity = is.getCount() - os.getCount();
                            ItemStack copy = os.copy();
                            copy.setCount(Math.abs(quantity));
                            boolean add = true;
                            if(quantity < 0){
                                // Negative.
                                add = false;
                            }
                            differences.add(new DirectionalItemStack(add, copy));
                        }
                    }
                }
                if(!found){
                    // This item has something that one does not
                    if(is.isEmpty()) continue;
                    int quantity = is.getCount();
                    ItemStack copy = is.copy();
                    copy.setCount(quantity);
                    differences.add(new DirectionalItemStack(true, copy));
                }
            }

            for(ItemStack os : other.standardStacks){
                boolean found = false;
                for(ItemStack ts : standardStacks){
                    if(StackUtil.stacksEqual(os, ts)){
                        found = true;
                    }
                }
                if(!found){
                    if(os.isEmpty()) continue;
                    int quantity = os.getCount();
                    ItemStack copy = os.copy();
                    copy.setCount(quantity);
                    differences.add(new DirectionalItemStack(false, copy));
                }
            }

            for(EquivalenceStack es : equivalenceStacks){
                for(EquivalenceStack oes : other.equivalenceStacks){
                    if(es.similarType(oes)){
                        Map<ItemStack, Integer> amounts = es.getAmountsByType();
                        Map<ItemStack, Integer> otherAmounts = oes.getAmountsByType();
                        for(Map.Entry<ItemStack, Integer> e : amounts.entrySet()){
                            for(Map.Entry<ItemStack, Integer> oe : otherAmounts.entrySet()){
                                if(StackUtil.stacksEqual(e.getKey(), oe.getKey())){
                                    int diff = e.getValue() - oe.getValue();
                                    if(diff != 0){
                                        ItemStack copy = e.getKey().copy();
                                        copy.setCount(Math.abs(diff));
                                        boolean add = true;
                                        if(diff < 0){
                                            // Negative.
                                            add = false;
                                        }
                                        differences.add(new DirectionalItemStack(add, copy));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return differences;
        }

        public void printDifferences(InventoryWrapper other){
            List<String> stringDifferences = new ArrayList<>();
            for(ItemStack is : standardStacks){
                boolean found = false;
                for(ItemStack os : other.standardStacks){
                    if(StackUtil.stacksEqual(is, os)){
                        found = true;
                        if(is.getCount() != os.getCount()){
                            stringDifferences.add(os.getDisplayName() + " :- Difference " + (is.getCount() - os.getCount()));
                        }
                    }
                }
                if(!found){
                    // This item has something that one does not
                    if(is.isEmpty()) continue;
                    stringDifferences.add(is.getDisplayName() + " :- Difference " + (is.getCount()));
                }
            }

            for(ItemStack os : other.standardStacks){
                boolean found = false;
                for(ItemStack ts : standardStacks){
                    if(StackUtil.stacksEqual(os, ts)){
                        found = true;
                    }
                }
                if(!found){
                    if(os.isEmpty()) continue;
                    stringDifferences.add(os.getDisplayName() + " :- Difference -" + (os.getCount()));
                }
            }

            for(EquivalenceStack es : equivalenceStacks){
                for(EquivalenceStack oes : other.equivalenceStacks){
                    if(es.similarType(oes)){
                        Map<ItemStack, Integer> amounts = es.getAmountsByType();
                        Map<ItemStack, Integer> otherAmounts = oes.getAmountsByType();
                        for(Map.Entry<ItemStack, Integer> e : amounts.entrySet()){
                            for(Map.Entry<ItemStack, Integer> oe : otherAmounts.entrySet()){
                                if(StackUtil.stacksEqual(e.getKey(), oe.getKey())){
                                    int diff = e.getValue() - oe.getValue();
                                    if(diff != 0)
                                        stringDifferences.add(oe.getKey().getDisplayName() + " :- Difference " + (e.getValue() - oe.getValue()));
                                }
                            }
                        }
                    }
                }
            }

            for(String s : stringDifferences){
                ThingsOfNaturalEnergies.logger.error(s);
            }
        }

        public boolean hasItemstack(ItemStack stack){
            for(ItemStack is : standardStacks){
                if(StackUtil.stacksEqual(is, stack)){
                    if(is.getCount() >= stack.getCount()){
                        return true;
                    }
                }
            }

            for(EquivalenceStack es : equivalenceStacks){
                if(es.hasAmount(stack, stack.getCount())){
                    return true;
                }
            }

            return false;
        }

        public ItemStack getItemstack(ItemStack stack){
            for(ItemStack is : standardStacks){
                if(StackUtil.stacksEqual(is, stack)){
                    if(is.getCount() >= stack.getCount()){
                        // This is a valid item
                        return is.splitStack(stack.getCount());
                    }
                }
            }

            for(EquivalenceStack es : equivalenceStacks){
                if(es.hasAmount(stack, stack.getCount())){
                    return es.removeAmount(stack, stack.getCount());
                }
            }

            return null;
        }

    }

    private static class RecipeBranch {
        public List<RecipeBranch> subBranches = new ArrayList<>();
        public ItemStack target;
        public boolean terminal = false;
        public int depth;
        public int amtMade;
        public RecipeBranch parent = null;
        public EquivalenceStack equiStack = null;
        public List<ItemStack> overspill = new ArrayList<>();

        public RecipeBranch(RecipeBranch parent, ItemStack target, int depth, int amtMade){
            this.parent = parent;
            this.target = target;
            this.depth = depth;
            this.amtMade = amtMade;

            List<IRecipe> ingredients = getRecipe(target);

            // If there is no recipe for this and/or we're at our depth limit
            // mark this branch as a terminal
            if(ingredients.size() == 0 || depth > DEPTH_LIMIT){
                terminal = true;
                return;
            }

            IRecipe recipe = ingredients.get(0);

            if(isContainedInEquivalenceStack(target)){
                this.equiStack = getEquivalenceStack(target).copy();
                terminal = true;
                return;
            }

            List<Ingredient> stuff = recipe.getIngredients();
            Map<ItemStack[], Integer> amtPerThing = new HashMap<ItemStack[], Integer>();

            // We need to create a map of block type and how many we need of them
            for(Ingredient i : stuff){
                ItemStack[] matchingStackArray = i.getMatchingStacks();
                ItemStack[] entryInMap = null;
                for(ItemStack[] array : amtPerThing.keySet()){
                    for(ItemStack a : array){
                        for(ItemStack m : matchingStackArray){
                            if(StackUtil.stacksEqual(m, a)){
                                // Match found
                                entryInMap = array;
                                break;
                            }
                        }
                        if(entryInMap != null) break;
                    }
                    if(entryInMap != null) break;
                }
                if(entryInMap != null){
                    amtPerThing.put(entryInMap, amtPerThing.get(entryInMap) + 1);
                }else{
                    amtPerThing.put(matchingStackArray, 1);
                }
            }

            if(recipe.getRecipeOutput().getCount() > this.amtMade){
                // Add overspill here, too.

                ItemStack toAdd = recipe.getRecipeOutput().copy();
                toAdd.setCount(recipe.getRecipeOutput().getCount() - this.amtMade);
                overspill.add(toAdd);
            }

            // TODO again, respect recoberries here
            for(Map.Entry<ItemStack[], Integer> entry : amtPerThing.entrySet()){
                if(entry.getKey().length == 0) continue;
                // Key is ItemStack array, value is the size of map.
                int amt_per_make = 0;
                for(IRecipe rcp : getRecipe(entry.getKey()[0])){
                    amt_per_make = rcp.getRecipeOutput().getCount();
                }

                if(amt_per_make >= entry.getValue()){
                    subBranches.add(new RecipeBranch(this, entry.getKey()[0], depth + 1, entry.getValue()));
                    continue;
                }

                if(amt_per_make == 0){
                    // This item has no recipe. Oh well
                    // Base level resource
                    subBranches.add(new RecipeBranch(this, entry.getKey()[0], depth + 1, entry.getValue()));
                }else{
                    // This item has subbranches.
                    int timesNeeded = (int) Math.ceil((double) entry.getValue() / (double) amt_per_make);
                    int totalAmtMade = timesNeeded * amt_per_make;

                    if(totalAmtMade > entry.getValue()){
                        // We are making more than we need, which means we'll have overspill.
                        ItemStack toAdd = entry.getKey()[0].copy();
                        toAdd.setCount(totalAmtMade - entry.getValue());
                        overspill.add(toAdd);
                        ThingsOfNaturalEnergies.logger.error("Added overspill");
                    }

                    for(int i = 0; i < timesNeeded; i++){
                        subBranches.add(new RecipeBranch(this, entry.getKey()[0], depth + 1, amt_per_make));
                    }
                }
            }
        }

        public List<RecipeBranch> getSubBranches(){
            return subBranches;
        }
    }

}
