package com.thelostnomad.tone.entities.nature_sprite;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.network.SpritePlayEffectPacket;
import com.thelostnomad.tone.network.SpriteSingPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.MobUtil;
import com.thelostnomad.tone.util.sound.LocalSoundCrafting;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import scala.tools.cmd.Spec;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class NatureSpriteEntity extends EntityFlying {

    // We reuse the zombie model which has arms that need to be raised when the zombie is attacking:
    // private static final DataParameter<Boolean> ARMS_RAISED = EntityDataManager.createKey(NatureSpriteEntity.class, DataSerializers.BOOLEAN);

    public static final ResourceLocation LOOT = new ResourceLocation(ThingsOfNaturalEnergies.MODID, "entities/nature_sprite");
    private boolean navigatingDown = false;

    private SpeciesHelper speciesHelper;

    private Map<Integer, Long> timeLastActionMap = new HashMap<Integer, Long>();
    private double stamina; // How much physical "energy" this sprite has.

    private boolean resting = false;

    private ItemStack[] heldItemstacks;

    public Map<String, Object> flags = new HashMap<String, Object>();

    public NatureSpriteEntity(World worldIn) {
        this(worldIn, SpeciesHelper.NATURE_SPRITE);
    }

    public NatureSpriteEntity(World worldIn, SpeciesHelper species){
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.isImmuneToFire = true;
        this.moveHelper = new NatureSpriteEntity.SpriteMoveHelper(this);
        this.speciesHelper = species;
        this.stamina = speciesHelper.getStamina();
        initEntityAI();
        heldItemstacks = new ItemStack[27];
        applyEntityAttributes();
    }

    public boolean getAlwaysRenderNameTag(){
        return false;
    }

    // This method called when the creature's species has just been set
    public void reload(World world, SpeciesHelper newSpecies){
        ThingsOfNaturalEnergies.logger.error("I was a " + speciesHelper.getInternalName() + " but am updating to " + newSpecies.getInternalName());
        this.updateBlocked = true;
        this.speciesHelper = newSpecies;
        // Clear tasks
        List<EntityAIBase> toRemove = new ArrayList<EntityAIBase>();
        ThingsOfNaturalEnergies.logger.error("Task size before: " + tasks.taskEntries.size());
        for(EntityAITasks.EntityAITaskEntry task : tasks.taskEntries){
            toRemove.add(task.action);
        }
        for(EntityAIBase eai : toRemove){
            tasks.removeTask(eai);
        }
        ThingsOfNaturalEnergies.logger.error("Task size after: " + tasks.taskEntries.size());
        toRemove.clear();
        for(EntityAITasks.EntityAITaskEntry task : targetTasks.taskEntries){
            toRemove.add(task.action);
        }
        for(EntityAIBase eai : toRemove){
            targetTasks.removeTask(eai);
        }
        ThingsOfNaturalEnergies.logger.error("Going on in");
        initEntityAI();
        heldItemstacks = new ItemStack[27];

        /**
         *
         this.dataManager = new EntityDataManager(this);
         this.dataManager.register(FLAGS, Byte.valueOf((byte)0));
         this.dataManager.register(AIR, Integer.valueOf(300));
         this.dataManager.register(CUSTOM_NAME_VISIBLE, Boolean.valueOf(false));
         this.dataManager.register(CUSTOM_NAME, "");
         this.dataManager.register(SILENT, Boolean.valueOf(false));
         this.dataManager.register(NO_GRAVITY, Boolean.valueOf(false));
         */

//        this.dataManager = new EntityDataManager(this);
//        this.dataManager.register(FLAGS, Byte.valueOf((byte)0));
//
//        try {
//            Field f = this.getClass().getDeclaredField("attributeMap");
//            f.setAccessible(true);
//            f.set(this, null);
//
//            registerDataManagerField("AIR", Integer.valueOf(300));
//            registerDataManagerField("CUSTOM_NAME_VISIBLE", Boolean.valueOf(false));
//            registerDataManagerField("CUSTOM_NAME", "");
//            registerDataManagerField("SILENT", false);
//            registerDataManagerField("NO_GRAVITY", Boolean.valueOf(false));
//        }catch(Exception e){
//            // Oops?
//        }
        this.stamina = speciesHelper.getStamina();
        this.firstUpdate = true;

        applyEntityAttributes();
        this.entityInit();
        this.updateBlocked = false;
    }

    private void registerDataManagerField(String field, Object value){
        try {
            Field cnameVis = this.getClass().getDeclaredField(field);
            cnameVis.setAccessible(true);
            DataParameter param = (DataParameter) cnameVis.get(null);
            if(param == null){
                ThingsOfNaturalEnergies.logger.error("Is param null? " + (param == null));
            }
            this.dataManager.register((DataParameter) cnameVis.get(null), value);
        }catch(Exception e){
            // Double oops?
            e.printStackTrace();
        }
    }

    public boolean getTargetPlayerIsOnline(){
        if(flags.containsKey("targetPlayer")){
            if(getTargetPlayer() != null){
                if(getTargetPlayer().getPosition() != null){
                    return true;
                }
            }
        }
        return false;
    }

    public EntityPlayer getTargetPlayer(){
        List<EntityPlayer> target = world.getEntities(EntityPlayer.class, new Predicate<EntityPlayer>() {
            @Override
            public boolean apply(@Nullable EntityPlayer input) {
                return input.getDisplayName().equals(flags.get("targetPlayer"));
            }
        });
        return target.get(0);
    }

    public ItemStack[] getHeldItemstacks() {
        return heldItemstacks;
    }

    public void setHeld(int slot, ItemStack to){
        this.heldItemstacks[slot] = to;
    }

    public void setHeldItemstacks(ItemStack[] heldItemstacks) {
        this.heldItemstacks = heldItemstacks;
    }

    public SpeciesHelper getSpeciesHelper() {
        return speciesHelper;
    }

    public void setSpeciesHelper(SpeciesHelper speciesHelper) {
        this.speciesHelper = speciesHelper;
    }

    public double getStamina() {
        return stamina;
    }

    public void setStamina(double stamina) {
        this.stamina = stamina;
    }

    @Override
    protected void entityInit() {
        try{
            super.entityInit();
        }catch(IllegalArgumentException e){
            return;
        }
        //this.getDataManager().register(ARMS_RAISED, Boolean.valueOf(false));
    }

    public void onUpdate() {
        super.onUpdate();

        if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL)
        {
            this.setDead();
        }
    }

    @Override
    protected void applyEntityAttributes() {
        if(speciesHelper == null){
            super.applyEntityAttributes();
            return;
        }
        speciesHelper.applyEntityAttributes(this);
//        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
//        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
//        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
//        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    @Override
    protected void initEntityAI() {
        if(speciesHelper == null){
            return;
        }

        this.tasks.addTask(5, new NatureSpriteEntity.AISleepWhenTired(this));
//        this.tasks.addTask(2, new EntityAIWeirdZombieAttack(this, 1.0D, false));
//        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
//        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
//        this.tasks.addTask(8, new EntityAILookIdle(this));
        ThingsOfNaturalEnergies.logger.error("Applying species AI of " + speciesHelper.getInternalName());
        speciesHelper.applyAI(this, this.tasks);
        ThingsOfNaturalEnergies.logger.error("Afterwards, size of tasks is " + this.tasks.taskEntries.size());
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
//        if (super.attackEntityAsMob(entityIn)) {
//            if (entityIn instanceof EntityLivingBase) {
//                // This zombie gives health boost and regeneration when it attacks
//                ((EntityLivingBase)entityIn).addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 200));
//                ((EntityLivingBase)entityIn).addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200));
//            }
//            return true;
//        } else {
//            return false;
//        }
        return false;
    }

    public static void registerFixesBat(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityBat.class);
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            return super.attackEntityFrom(source, amount);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        //this.dataManager.set(HANGING, Byte.valueOf(compound.getByte("BatFlags")));
        navigatingDown = compound.getBoolean("navigating_down");
        speciesHelper = SpeciesHelper.fromInternalName(compound.getString("species"));
        speciesHelper.setItemField(compound.getString("item_field"));

        NBTTagCompound cpd = compound.getCompoundTag("action_map");
        for(String s : cpd.getKeySet()){
            timeLastActionMap.put(Integer.parseInt(s), cpd.getLong(s));
        }

        stamina = compound.getDouble("stamina");
        resting = compound.getBoolean("resting");
    }

    public boolean getCanSpawnHere() {
        BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);

        return true; // for now, it can spawn anywhere.
    }

    public float getEyeHeight() {
        return this.height / 2.0F;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     * private boolean navigatingDown = false;

     private SpeciesHelper speciesHelper;

     private Map<Integer, Long> timeLastActionMap = new HashMap<Integer, Long>();
     private double stamina; // How much physical "energy" this sprite has.

     private boolean resting = false;
     */
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        //compound.setByte("BatFlags", ((Byte)this.dataManager.get(HANGING)).byteValue());
        compound.setBoolean("navigating_down", navigatingDown);
        compound.setString("species", speciesHelper.getInternalName());
        compound.setString("item_field", speciesHelper.getItemField());

        NBTTagCompound cpd = new NBTTagCompound();
        for(Map.Entry<Integer, Long> lastActionMap : timeLastActionMap.entrySet()){
            cpd.setLong(lastActionMap.getKey().toString(), lastActionMap.getValue());
        }
        compound.setTag("action_map", cpd);

        compound.setDouble("stamina", stamina);
        compound.setBoolean("resting", resting);
    }

    public Map<Integer, Long> getTimeLastActionMap(){
        return this.timeLastActionMap;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 5;
    }

    public void fall(float distance, float damageMultiplier) {
    }

    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean isNavigatingDown() {
        return navigatingDown;
    }

    public void setNavigatingDown(boolean navigatingDown) {
        this.navigatingDown = navigatingDown;
    }

    static class AISleepWhenTired extends EntityAIBase
    {
        private final NatureSpriteEntity parentEntity;
        private long lastStaminaIncrease = 0L;

        public AISleepWhenTired(NatureSpriteEntity ghast)
        {
            this.parentEntity = ghast;
            this.setMutexBits(1);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute()
        {
            if(parentEntity.stamina < (this.parentEntity.speciesHelper.getStamina() * 0.25)){
                ThingsOfNaturalEnergies.logger.error("I want to start resting");
                parentEntity.resting = true;
                return true;
            }
            return false;
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean shouldContinueExecuting()
        {
            if(parentEntity.stamina < parentEntity.speciesHelper.getStamina()){
                return true;
            }
            parentEntity.clearActivePotions();
            parentEntity.resting = false;
            return false;
        }

        private int getGroundHeight(){
            int i = parentEntity.getPosition().getY();
            int index = 0;
            for(; i >= 0; i--, index++){
                if(parentEntity.world.getBlockState(parentEntity.getPosition().down(index)) != Blocks.AIR){
                    return i;
                }
            }
            return -1;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting()
        {
            updateTask();
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void updateTask()
        {
            Potion potion = MobEffects.SLOWNESS;
            if(parentEntity.getActivePotionEffects().size() == 0){
                parentEntity.addPotionEffect((new PotionEffect(potion, 10000, 1)));
            }

            if((this.parentEntity.getPosition().getY() - getGroundHeight()) > 1){
                double d0 = 0;
                double d1 = this.parentEntity.posY - 0.2;
                double d2 = 0;
                this.parentEntity.setPosition(this.parentEntity.getPosition().getX(), d1, this.parentEntity.getPosition().getZ());
            }

            long nowTick = parentEntity.world.getTotalWorldTime();
            if((nowTick - lastStaminaIncrease) > 20L){
                double limit = parentEntity.speciesHelper.getStamina();
                double amt = limit - this.parentEntity.stamina;
                if(amt > 1D){
                    this.parentEntity.stamina += 1;
                }else{
                    this.parentEntity.stamina += amt;
                }
                lastStaminaIncrease = nowTick;
            }
        }

    }

    public boolean isResting() {
        return resting;
    }

    public void setResting(boolean resting) {
        this.resting = resting;
    }

    public static class SpriteMoveHelper extends EntityMoveHelper
    {
        private final NatureSpriteEntity parentEntity;
        private int courseChangeCooldown;

        public SpriteMoveHelper(NatureSpriteEntity ghast)
        {
            super(ghast);
            this.parentEntity = ghast;
        }

        public void setWait(){
            this.action = Action.WAIT;
        }

        public void onUpdateMoveHelper()
        {
            if (this.action == EntityMoveHelper.Action.MOVE_TO)
            {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.courseChangeCooldown-- <= 0)
                {
                    this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
                    d3 = (double)MathHelper.sqrt(d3);

                    if (this.isNotColliding(this.posX, this.posY, this.posZ, d3))
                    {
                        this.parentEntity.motionX += d0 / d3 * 0.1D;
                        this.parentEntity.motionY += d1 / d3 * 0.1D;
                        this.parentEntity.motionZ += d2 / d3 * 0.1D;
                    }
                    else
                    {
                        this.action = EntityMoveHelper.Action.WAIT;
                    }
                }
            }
        }

        /**
         * Checks if entity bounding box is not colliding with terrain
         */
        private boolean isNotColliding(double x, double y, double z, double p_179926_7_)
        {
            double d0 = (x - this.parentEntity.posX) / p_179926_7_;
            double d1 = (y - this.parentEntity.posY) / p_179926_7_;
            double d2 = (z - this.parentEntity.posZ) / p_179926_7_;
            AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

            for (int i = 1; (double)i < p_179926_7_; ++i)
            {
                axisalignedbb = axisalignedbb.offset(d0, d1, d2);

                if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty())
                {
                    return false;
                }
            }

            return true;
        }
    }

}
