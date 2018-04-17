package com.thelostnomad.tone.entities.nature_sprite.recipe;

import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.entities.nature_sprite.conditions.ConditionBase;

import java.util.ArrayList;
import java.util.List;

public class SongRecipe {

    private String target;
    private List<String> singingSprites;
    private List<ConditionBase> conditions;

    public SongRecipe(String targetSpecies){
        this.target = targetSpecies;
        singingSprites = new ArrayList<>();
        this.conditions = new ArrayList<ConditionBase>();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void addSingingSprite(String name){
        this.singingSprites.add(name);
    }

    public List<String> getSingingSprites(){
        return this.singingSprites;
    }

    public List<ConditionBase> getConditions() {
        return conditions;
    }

    public void addCondition(ConditionBase condition) {
        this.conditions.add(condition);
    }
}
