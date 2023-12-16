package net.invalid_genie.mixin;

import com.mojang.authlib.GameProfile;
import net.invalid_genie.entity.InvalidGenieEntity;
import net.invalid_genie.entity.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "tick()V", at = @At("HEAD"))
    public void tick(CallbackInfo ci){
        Box box = new Box(this.getBlockPos()).expand(100);
        List<InvalidGenieEntity> list = this.getWorld().getEntitiesByClass(InvalidGenieEntity.class, box, e->e.isAlive());
        if(list.isEmpty()){
            InvalidGenieEntity genie = new InvalidGenieEntity(ModEntities.INVALID_GENIE, this.getWorld());
            this.getWorld().spawnEntity(genie);
            genie.setPos(this.getX(), this.getY(), this.getZ());
        }
    }
}
