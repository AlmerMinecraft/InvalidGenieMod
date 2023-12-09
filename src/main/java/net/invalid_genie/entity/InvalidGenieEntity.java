package net.invalid_genie.entity;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.world.World;

public class InvalidGenieEntity extends FlyingEntity {
    public final AnimationState idlingAnimationState = new AnimationState();
    public final AnimationState magicAnimationState = new AnimationState();
    private int idleAnimationCooldown = 0;
    protected InvalidGenieEntity(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
    }
    private void updateAnimations() {
        if (this.idleAnimationCooldown <= 0) {
            this.idleAnimationCooldown = this.random.nextInt(40) + 80;
            this.idlingAnimationState.start(this.age);
        } else {
            --this.idleAnimationCooldown;
        }
        if(isMagic()){
            this.magicAnimationState.start(this.age);
        }
    }
    public boolean isMagic(){
        return false;
    }
}
