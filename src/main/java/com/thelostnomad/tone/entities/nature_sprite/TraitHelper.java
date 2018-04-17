package com.thelostnomad.tone.entities.nature_sprite;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.conditions.ConditionBase;
import com.thelostnomad.tone.util.annotation.RecipeCondition;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import org.objectweb.asm.Type;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TraitHelper {

    private static Map<String, Pair<String, Type>> TRAIT_AI_MAP = new HashMap<String, Pair<String, Type>>();
    private static Map<String, Pair<String, Type>> CONDITION_MAP = new HashMap<String, Pair<String, Type>>();

    public static String deserializeItem(String line){
        if(line.contains("{")){
            return line.substring(1, line.indexOf("}"));
        }
        return null;
    }

    public static ConditionBase deserializeCondition(String line){
        if(line.contains("=") || line.contains("{")) return null;

        try {
            boolean hasParam = false;
            String param = "";
            if(line.contains("(")){
                // We do have a parameter
                param = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
                line = line.substring(0, line.indexOf("("));
                hasParam = true;
            }
            Pair<String, Type> thisPair = TraitHelper.CONDITION_MAP.get(line);
            Class c = Class.forName(thisPair.getLeft());
            if(hasParam){
                if(thisPair.getRight() == Type.INT_TYPE){
                    return (ConditionBase) c.getConstructor(int.class).newInstance(Integer.parseInt(param));
                }
                if(thisPair.getRight() == Type.LONG_TYPE){
                    return (ConditionBase) c.getConstructor(long.class).newInstance(Long.parseLong(param));
                }
                if(thisPair.getRight() == Type.DOUBLE_TYPE){
                    return (ConditionBase) c.getConstructor(double.class).newInstance(Double.parseDouble(param));
                }
                if(thisPair.getRight() == Type.BOOLEAN_TYPE){
                    return (ConditionBase) c.getConstructor(boolean.class).newInstance(Boolean.parseBoolean(param));
                }
            }else{
                return (ConditionBase) c.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void findAllRecipeConditions(@Nonnull ASMDataTable table){
        String annotationClassName = RecipeCondition.class.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = new HashSet<>(table.getAll(annotationClassName));

        asmLoop:
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Map<String, Object> data = asmData.getAnnotationInfo();
                String name = WordUtils.uncapitalize(asmData.getClassName().substring(asmData.getClassName().lastIndexOf('.') + 1).trim());

                String c = asmData.getClassName();
                String AIName = c.substring(c.lastIndexOf(".") + 1, c.length());

                Type optional = (Type) data.get("optionalArg");

                CONDITION_MAP.put(AIName, new Pair<String, Type>() {
                    @Override
                    public Type setValue(Type value) {
                        return null;
                    }

                    @Override
                    public String getLeft() {
                        return c;
                    }

                    @Override
                    public Type getRight() {
                        return optional;
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public static Pair<IAttribute, Double> deserializeAtribute(String line){
        if(line.contains("=")){
            String[] parts = line.split("=");
            
            if(parts[0].equals("health")){
                return new Pair<IAttribute, Double>() {
                    @Override
                    public IAttribute getLeft() {
                        return SharedMonsterAttributes.MAX_HEALTH;
                    }

                    @Override
                    public Double getRight() {
                        return Double.parseDouble(parts[1]);
                    }

                    @Override
                    public Double setValue(Double value) {
                        return null;
                    }
                };
            }
            if(parts[0].equals("speed")){
                return new Pair<IAttribute, Double>(){
                    @Override
                    public IAttribute getLeft() {
                        return SharedMonsterAttributes.FLYING_SPEED;
                    }

                    @Override
                    public Double getRight() {
                        return Double.parseDouble(parts[1]);
                    }

                    @Override
                    public Double setValue(Double value) {
                        return null;
                    }
                };
            }
            if(parts[0].equals("armor")){
                return new Pair<IAttribute, Double>(){
                    @Override
                    public IAttribute getLeft() {
                        return SharedMonsterAttributes.ARMOR;
                    }

                    @Override
                    public Double getRight() {
                        return Double.parseDouble(parts[1]);
                    }

                    @Override
                    public Double setValue(Double value) {
                        return null;
                    }
                };
            }
            if(parts[0].equals("stamina")) {
                return new Pair<IAttribute, Double>(){
                    @Override
                    public Double setValue(Double value) {
                        return null;
                    }

                    @Override
                    public IAttribute getLeft() {
                        return null;
                    }

                    @Override
                    public Double getRight() {
                        return Double.parseDouble(parts[1]);
                    }
                };
            }

            return null;
        }else{
            return null;
        }
    }

    public static void findAllSpriteAI(@Nonnull ASMDataTable table){
        String annotationClassName = SpriteAI.class.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = new HashSet<>(table.getAll(annotationClassName));

        asmLoop:
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Map<String, Object> data = asmData.getAnnotationInfo();
                String name = WordUtils.uncapitalize(asmData.getClassName().substring(asmData.getClassName().lastIndexOf('.') + 1).trim());

                String c = asmData.getClassName();
                String AIName = c.substring(c.lastIndexOf(".") + 1, c.length());

                Type optional = (Type) data.get("optionalArg");

                TRAIT_AI_MAP.put(AIName, new Pair<String, Type>() {
                    @Override
                    public Type setValue(Type value) {
                        return null;
                    }

                    @Override
                    public String getLeft() {
                        return c;
                    }

                    @Override
                    public Type getRight() {
                        return optional;
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public static EntityAIBase deserializeAI(NatureSpriteEntity parentEntity, String line){
        if(line.contains("=") || line.contains("{")) return null;

        try {
            boolean hasParam = false;
            String param = "";
            if(line.contains("(")){
                // We do have a parameter
                param = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
                line = line.substring(0, line.indexOf("("));
                hasParam = true;
            }
            Pair<String, Type> thisPair = TRAIT_AI_MAP.get(line);
            Class c = Class.forName(thisPair.getLeft());
            if(hasParam){
                if(thisPair.getRight() == Type.INT_TYPE){
                    return (EntityAIBase) c.getConstructor(NatureSpriteEntity.class, int.class).newInstance(parentEntity, Integer.parseInt(param));
                }
                if(thisPair.getRight() == Type.LONG_TYPE){
                    return (EntityAIBase) c.getConstructor(NatureSpriteEntity.class, long.class).newInstance(parentEntity, Long.parseLong(param));
                }
                if(thisPair.getRight() == Type.DOUBLE_TYPE){
                    return (EntityAIBase) c.getConstructor(NatureSpriteEntity.class, double.class).newInstance(parentEntity, Double.parseDouble(param));
                }
                if(thisPair.getRight() == Type.BOOLEAN_TYPE){
                    return (EntityAIBase) c.getConstructor(NatureSpriteEntity.class, boolean.class).newInstance(parentEntity, Boolean.parseBoolean(param));
                }
            }else{
                return (EntityAIBase) c.getConstructor(NatureSpriteEntity.class).newInstance(parentEntity);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
