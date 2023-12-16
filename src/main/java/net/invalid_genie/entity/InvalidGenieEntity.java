package net.invalid_genie.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import net.invalid_genie.InvalidGenieMod;
import net.minecraft.block.*;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.custom.DebugBeeCustomPayload;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.tools.obfuscation.struct.Message;

import java.util.*;
import java.util.function.Predicate;

public class InvalidGenieEntity extends FlyingEntity implements Flutterer {
    public final AnimationState idlingAnimationState = new AnimationState();
    public final AnimationState magicAnimationState = new AnimationState();
    private int idleAnimationCooldown = 0;
    private int teleportCooldown = 6000;
    private boolean magic = false;
    public static PlayerEntity playerTarget;
    public InvalidGenieEntity(EntityType<? extends InvalidGenieEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new GenieMoveControl(this);
    }
    protected void initGoals(){
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new GoToPlayerGoal());
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 100));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal(this, PlayerEntity.class, false));
    }
    private void updateAnimations() {
        if (this.idleAnimationCooldown <= 0) {
            this.idleAnimationCooldown = this.random.nextInt(40) + 80;
            this.idlingAnimationState.start(this.age);
        } else {
            --this.idleAnimationCooldown;
        }
        if(isMagic()){
            this.magicAnimate();
        }
    }
    private void magicAnimate(){
        this.magicAnimationState.start(this.age);
        this.getWorld().addParticle(ParticleTypes.EFFECT, this.getX(), this.getY() + 0.5, this.getZ(), 0, 1, 0);
    }
    public boolean isMagic(){
        return this.magic;
    }
    public void setMagic(boolean magic){
        this.magic = magic;
    }
    public static DefaultAttributeContainer.Builder createAttributes(){
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0f)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.10000000149011612)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.10000000149011612)
                .add(EntityAttributes.GENERIC_ARMOR, 1000)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0);
    }
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.6F;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        this.magicAnimate();
        return super.interactAt(player, hitPos, hand);
    }

    @Override
    public void tick() {
        if (this.getWorld().isClient()) {
            this.updateAnimations();
        }
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);
        if(this.teleportCooldown <= 0){
            this.teleportCooldown = 6000;
            Box box = new Box(this.getBlockPos()).expand(40, 40, 40);
            List list = this.getWorld().getEntitiesByClass(LivingEntity.class, box, e->e.isAlive());
            if(!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    LivingEntity entity = (LivingEntity) list.get(i);
                    entity.setPos(this.getX(), this.getY(), this.getZ());
                }
            }
        }
        else{
            --this.teleportCooldown;
        }
        if(playerTarget != null) {
            Box box = new Box(playerTarget.getBlockPos()).expand(100);
            List<InvalidGenieEntity> list = playerTarget.getWorld().getEntitiesByClass(InvalidGenieEntity.class, box, e -> e.isAlive());
            if (list.isEmpty()) {
                InvalidGenieEntity genie = new InvalidGenieEntity(ModEntities.INVALID_GENIE, playerTarget.getWorld());
                playerTarget.getWorld().spawnEntity(genie);
                genie.setPos(playerTarget.getX(), playerTarget.getY(), playerTarget.getZ());
            }
            if(list.size() > 1){
                for(int i = 0; i < list.size() - 1; i++){
                    InvalidGenieEntity genie = list.get(i);
                    genie.discard();
                }
            }
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if(this.getTarget() != null) {
            PlayerEntity player = (PlayerEntity) this.getTarget();
            playerTarget = (PlayerEntity) this.getTarget();
            if (this.squaredDistanceTo(playerTarget) >= 250) {
                this.setPos(playerTarget.getX(), playerTarget.getY(), playerTarget.getZ());
            }
            for(int x = -10; x < 10; x++) {
                for(int z = -10; z < 10; z++){
                    for(int y = -5; y < 5; y++){
                        BlockState state = this.getWorld().getBlockState(BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z));
                        if(state == Blocks.FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.NORTH) ||
                                state == Blocks.FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.WEST) ||
                                state == Blocks.FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.EAST) ||
                                state == Blocks.FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.SOUTH)){
                            magicBlock(0, BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), state);
                        }
                        if(state == Blocks.BLAST_FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.NORTH) ||
                                state == Blocks.BLAST_FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.WEST) ||
                                state == Blocks.BLAST_FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.EAST) ||
                                state == Blocks.BLAST_FURNACE.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.SOUTH)){
                            magicBlock(0, BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), state);
                        }
                        if(state == Blocks.SMOKER.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.NORTH) ||
                                state == Blocks.SMOKER.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.WEST) ||
                                state == Blocks.SMOKER.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.EAST) ||
                                state == Blocks.SMOKER.getDefaultState().with(AbstractFurnaceBlock.LIT, true).with(AbstractFurnaceBlock.FACING, Direction.SOUTH)){
                            magicBlock(1, BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), state);
                        }
                        if(state == Blocks.LAVA.getDefaultState()){
                            magicBlock(2, BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), state);
                        }
                        if(state == Blocks.WATER.getDefaultState()){
                            magicBlock(3, BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), state);
                        }
                        if(state == Blocks.COPPER_ORE.getDefaultState() ||
                                state == Blocks.IRON_ORE.getDefaultState() ||
                                state == Blocks.GOLD_ORE.getDefaultState() ||
                                state == Blocks.DIAMOND_ORE.getDefaultState() ||
                                state == Blocks.EMERALD_ORE.getDefaultState() ||
                                state == Blocks.LAPIS_ORE.getDefaultState()){
                            magicBlock(4, BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), state);
                        }
                        if(state == Blocks.NETHER_GOLD_ORE.getDefaultState()){
                            PiglinBruteEntity piglin = new PiglinBruteEntity(EntityType.PIGLIN_BRUTE, this.getWorld());
                            this.getWorld().spawnEntity(piglin);
                            piglin.setPos(player.getX() + x, player.getY() + y + 1, player.getZ() + z);
                            this.getWorld().setBlockState(BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), Blocks.NETHERRACK.getDefaultState());
                        }
                        if(state == Blocks.BEE_NEST.getDefaultState().with(BeehiveBlock.HONEY_LEVEL, 5).with(BeehiveBlock.FACING, Direction.NORTH) ||
                                state == Blocks.BEE_NEST.getDefaultState().with(BeehiveBlock.HONEY_LEVEL, 5).with(BeehiveBlock.FACING, Direction.SOUTH) ||
                                state == Blocks.BEE_NEST.getDefaultState().with(BeehiveBlock.HONEY_LEVEL, 5).with(BeehiveBlock.FACING, Direction.WEST) ||
                                state == Blocks.BEE_NEST.getDefaultState().with(BeehiveBlock.HONEY_LEVEL, 5).with(BeehiveBlock.FACING, Direction.EAST)){
                            magicBlock(5, BlockPos.ofFloored(player.getX() + x, player.getY() + y, player.getZ() + z), state);
                        }
                    }
                }
            }
            if(player.getMainHandStack().getItem() instanceof SwordItem ||
                    player.getMainHandStack().getItem() instanceof PickaxeItem ||
                    player.getMainHandStack().getItem() instanceof AxeItem ||
                    player.getMainHandStack().getItem() instanceof ShovelItem ||
                    player.getMainHandStack().getItem() instanceof HoeItem ||
                    player.getMainHandStack().getItem() instanceof BowItem ||
                    player.getMainHandStack().getItem() instanceof FishingRodItem){
                if(!player.getMainHandStack().hasEnchantments()) {
                    ItemStack item = player.getMainHandStack();
                    item.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                    this.setMagic(true);
                }
            }
            if(player.getArmor() > 0){
                if(player.getEquippedStack(EquipmentSlot.HEAD) != null && !player.getEquippedStack(EquipmentSlot.HEAD).hasEnchantments()){
                    player.getEquippedStack(EquipmentSlot.HEAD).addEnchantment(Enchantments.BINDING_CURSE, 1);
                    this.setMagic(true);
                }
                if(player.getEquippedStack(EquipmentSlot.CHEST) != null && !player.getEquippedStack(EquipmentSlot.CHEST).hasEnchantments()){
                    player.getEquippedStack(EquipmentSlot.CHEST).addEnchantment(Enchantments.BINDING_CURSE, 1);
                    this.setMagic(true);
                }
                if(player.getEquippedStack(EquipmentSlot.LEGS) != null && !player.getEquippedStack(EquipmentSlot.LEGS).hasEnchantments()){
                    player.getEquippedStack(EquipmentSlot.LEGS).addEnchantment(Enchantments.BINDING_CURSE, 1);
                    this.setMagic(true);
                }
                if(player.getEquippedStack(EquipmentSlot.FEET) != null && !player.getEquippedStack(EquipmentSlot.FEET).hasEnchantments()){
                    player.getEquippedStack(EquipmentSlot.FEET).addEnchantment(Enchantments.BINDING_CURSE, 1);
                    this.setMagic(true);
                }
            }
            Box box = new Box(this.getBlockPos()).expand(20);
            List<LivingEntity> list = this.getWorld().getEntitiesByClass(LivingEntity.class, box, e->e.isOnFire());
            if(!list.isEmpty()){
                for(int i = 0; i < list.size(); i++){
                    LivingEntity entity = list.get(i);
                    entity.setFireTicks(0);
                    entity.setOnFire(false);
                    this.setMagic(true);
                    this.getWorld().addParticle(ParticleTypes.CLOUD, entity.getX(),  entity.getY() + 2, entity.getZ(), 0, 1, 0);
                    this.getWorld().addParticle(ParticleTypes.DRIPPING_WATER, entity.getX(),  entity.getY() + 2, entity.getZ(), 0, -1, 0);
                }
            }
            if(player.getMainHandStack().getItem().isFood()){
                int count = player.getMainHandStack().getCount();
                player.setStackInHand(player.getActiveHand(), Items.CHORUS_FRUIT.getDefaultStack());
                player.getStackInHand(player.getActiveHand()).setCount(count);
                this.setMagic(true);
            }
        }
    }
    @Override
    public boolean isInvulnerable() {
        return true;
    }
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM;
    }
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ALLAY_HURT;
    }
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ALLAY_DEATH;
    }
    protected float getSoundVolume() {
        return 0.4F;
    }
    @Override
    public boolean isInAir() {
        return true;
    }
    public void magicBlock(int index, BlockPos pos, BlockState state){
        this.setMagic(true);
        if(index == 0){
            this.getWorld().setBlockState(pos, Blocks.SMOKER.getDefaultState().with(AbstractFurnaceBlock.FACING, state.get(AbstractFurnaceBlock.FACING)));
        }
        if(index == 1){
            this.getWorld().setBlockState(pos, Blocks.BLAST_FURNACE.getDefaultState().with(AbstractFurnaceBlock.FACING, state.get(AbstractFurnaceBlock.FACING)));
        }
        if(index == 2){
            this.getWorld().setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState());
        }
        if(index == 3){
            this.getWorld().setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState());
        }
        if(index == 4){
            this.getWorld().setBlockState(pos, Blocks.COAL_ORE.getDefaultState());
        }
        if(index == 5){
            this.getWorld().setBlockState(pos, Blocks.BEE_NEST.getStateWithProperties(state).with(BeehiveBlock.HONEY_LEVEL, 0));
            PotionEntity item = new PotionEntity(this.getWorld(), pos.getX(), pos.getY() - 1, pos.getZ());
            this.getWorld().spawnEntity(item);
            item.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
            PotionUtil.setPotion(item.getStack(), Potions.STRONG_POISON);
        }
    }
    private class GenieMoveControl extends MoveControl {
        public GenieMoveControl(InvalidGenieEntity owner) {
            super(owner);
        }
        public void tick() {
            if (this.state == State.MOVE_TO) {
                Vec3d vec3d = new Vec3d(this.targetX - InvalidGenieEntity.this.getX(), this.targetY - InvalidGenieEntity.this.getY(), this.targetZ - InvalidGenieEntity.this.getZ());
                double d = vec3d.length();
                if (d < InvalidGenieEntity.this.getBoundingBox().getAverageSideLength()) {
                    this.state = State.WAIT;
                    InvalidGenieEntity.this.setVelocity(InvalidGenieEntity.this.getVelocity().multiply(0.5));
                } else {
                    InvalidGenieEntity.this.setVelocity(InvalidGenieEntity.this.getVelocity().add(vec3d.multiply(this.speed * 0.05 / d)));
                    if (InvalidGenieEntity.this.getTarget() == null) {
                        Vec3d vec3d2 = InvalidGenieEntity.this.getVelocity();
                        InvalidGenieEntity.this.setYaw(-((float) MathHelper.atan2(vec3d2.x, vec3d2.z)) * 57.295776F);
                        InvalidGenieEntity.this.bodyYaw = InvalidGenieEntity.this.getYaw();
                    } else {
                        double e = InvalidGenieEntity.this.getTarget().getX() - InvalidGenieEntity.this.getX();
                        double f = InvalidGenieEntity.this.getTarget().getZ() - InvalidGenieEntity.this.getZ();
                        InvalidGenieEntity.this.setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776F);
                        InvalidGenieEntity.this.bodyYaw = InvalidGenieEntity.this.getYaw();
                    }
                }

            }
        }
    }
    private class GoToPlayerGoal extends Goal {
        public GoToPlayerGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }
        public boolean canStart() {
            LivingEntity livingEntity = InvalidGenieEntity.this.getTarget();
            if (livingEntity != null && livingEntity.isAlive() && !InvalidGenieEntity.this.getMoveControl().isMoving() && InvalidGenieEntity.this.random.nextInt(toGoalTicks(7)) == 0) {
                return InvalidGenieEntity.this.squaredDistanceTo(livingEntity) > 4.0;
            } else {
                return false;
            }
        }
        public boolean shouldContinue() {
            LivingEntity livingEntity = InvalidGenieEntity.this.getTarget();
            if(livingEntity != null) {
                double d = InvalidGenieEntity.this.squaredDistanceTo(livingEntity);
                return InvalidGenieEntity.this.getMoveControl().isMoving() && InvalidGenieEntity.this.getTarget() != null && InvalidGenieEntity.this.getTarget().isAlive() && d > 1.0;
            }
            else{
                return false;
            }
        }
        public void start() {
            LivingEntity livingEntity = InvalidGenieEntity.this.getTarget();
            if (livingEntity != null) {
                Vec3d vec3d = livingEntity.getEyePos();
                InvalidGenieEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 0.50000000149011612);
            }
        }
        public boolean shouldRunEveryTick() {
            return true;
        }
        public void tick() {
            LivingEntity livingEntity = InvalidGenieEntity.this.getTarget();
            if (livingEntity != null) {
                double d = InvalidGenieEntity.this.squaredDistanceTo(livingEntity);
                if (d < 9.0) {
                    Vec3d vec3d = livingEntity.getEyePos();
                    InvalidGenieEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
                }
            }
        }
    }
}
