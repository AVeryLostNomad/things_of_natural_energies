package com.thelostnomad.tone.entities.nature_sprite;

import com.google.common.io.Resources;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.TraitHelper;
import com.thelostnomad.tone.entities.nature_sprite.ai.GrowNearbyPlants;
import com.thelostnomad.tone.entities.nature_sprite.conditions.ConditionBase;
import com.thelostnomad.tone.entities.nature_sprite.recipe.SongRecipe;
import com.thelostnomad.tone.util.annotation.RecipeCondition;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import com.thelostnomad.tone.util.sound.LocalSoundCrafting;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;
import scala.tools.cmd.Spec;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SpeciesHelper {

    public static final SpeciesHelper NATURE_SPRITE = new SpeciesHelper("normal");
    public static final SpeciesHelper GREEDY_SPRITE = new SpeciesHelper("greedy");
    public static final SpeciesHelper PACKER_SPRITE = new SpeciesHelper("packer");
    public static final SpeciesHelper VOID_SPRITE = new SpeciesHelper("void");

    private String thisSpeciesFile;
    private boolean isValid = true;

    private Map<IAttribute, Double> attributes = new HashMap<IAttribute, Double>();
    private List<String> traitList = new ArrayList<String>();
    private double stamina;
    private String itemField = "";

    public SpeciesHelper(String json_species_file){
        thisSpeciesFile = json_species_file;
        load(new ResourceLocation(ThingsOfNaturalEnergies.MODID, json_species_file));
    }

    public void reload(){
        load(new ResourceLocation(ThingsOfNaturalEnergies.MODID, getInternalName()));
    }

    public static SpeciesHelper fromInternalName(String s){
        Field[] declaredFields = SpeciesHelper.class.getDeclaredFields();
        List<Field> staticFields = new ArrayList<Field>();
        for(Field f : declaredFields){
            if(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())){
                staticFields.add(f);
            }
        }
        for(Field f : staticFields){
            try {
                SpeciesHelper sh = (SpeciesHelper) f.get(null);
                if(sh.getInternalName().equals(s)){
                    return sh;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getInternalName(){
        return thisSpeciesFile;
    }

    public boolean isValid(){
        return this.isValid;
    }

    private void load(ResourceLocation resource){
        if(resource.getResourcePath().equals("packer")){
            ThingsOfNaturalEnergies.logger.error("Loading this thing");
        }
        String fileString = "/assets/" + resource.getResourceDomain() + "/species/"
                + resource.getResourcePath() + ".txt";
        InputStream url = ThingsOfNaturalEnergies.class.getResourceAsStream(fileString);
        ThingsOfNaturalEnergies.logger.error("Resource is null? " + (url == null));
        if (url != null)
        {
            try
            {
                boolean inCreation = false;
                SongRecipe workInProgress = null;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url));
                String line;
                while((line = bufferedReader.readLine()) != null){
                    // Do something with this line that indicates the traits
                    // TODO do something with this here...
                    if(line.startsWith(">")){
                        // We are making a creation condition (a way to make this sprite species via sprite breeding)
                        if(!inCreation){
                            // Start making one
                            workInProgress = new SongRecipe(resource.getResourcePath());
                            inCreation = true;
                        }else{
                            // We've just finished one, add it
                            LocalSoundCrafting.addSongRecipe(workInProgress);
                            workInProgress = null;
                            inCreation = false;
                        }
                        continue;
                    }

                    if(inCreation){
                        // Anything we read is going to go into that song recipe, period.

                        if(line.startsWith("&")){
                            // This line is a condition of recipe completion
                            // Conditions are for things like "it must be raining"
                            // "it must be this time", etc...
                            workInProgress.addCondition(TraitHelper.deserializeCondition(line.substring(1, line.length())));
                            continue;
                        }



                        // There are no symbols on this line. It's just the internal name of a sprite species
                        // That sprite must be singing to trigger this.
                        workInProgress.addSingingSprite(line);
                        continue;
                    }
                    Pair<IAttribute, Double> attribute = TraitHelper.deserializeAtribute(line);
                    if(attribute == null){
                        String itemTexture = TraitHelper.deserializeItem(line);
                        if(itemTexture != null){
                            this.itemField = itemTexture;
                            continue;
                        }
                        ThingsOfNaturalEnergies.logger.error("Adding the line: " + line);
                        traitList.add(line);
                    }else{
                        if(attribute.getLeft() == null){
                            this.stamina = attribute.getRight();
                            continue;
                        }
                        attributes.put(attribute.getLeft(), attribute.getRight());
                    }
                }
            } catch (FileNotFoundException e) {
                ThingsOfNaturalEnergies.logger.error("Couldn't load species file {} from {}", resource, url, e);
                isValid = false;
            } catch (IOException e) {
                ThingsOfNaturalEnergies.logger.error("Couldn't load species file {} from {}", resource, url, e);
                isValid = false;
            }
        }
    }

    public String getItemField() {
        return itemField;
    }

    public void setItemField(String itemField) {
        this.itemField = itemField;
    }

    public double getStamina(){
        return stamina;
    }

    public void applyEntityAttributes(NatureSpriteEntity entity){
        for(Map.Entry<IAttribute, Double> e : attributes.entrySet()){
            entity.getEntityAttribute(e.getKey()).setBaseValue(e.getValue());
        }
    }

    public void applyAI(NatureSpriteEntity entity, EntityAITasks tasks){
        for(String s : traitList){
            EntityAIBase base = TraitHelper.deserializeAI(entity, s);

            //ThingsOfNaturalEnergies.logger.error("Loading " + base.getClass().getCanonicalName());

            if(base == null) continue;

            try {
                Method m = base.getClass().getMethod("getImportance");
                int value = (int) m.invoke(base);
                tasks.addTask(value, base);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

}
