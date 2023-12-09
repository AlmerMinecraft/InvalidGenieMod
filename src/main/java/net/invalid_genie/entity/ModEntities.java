package net.invalid_genie.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.invalid_genie.InvalidGenieMod;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<InvalidGenieEntity> INVALID_GENIE = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(InvalidGenieMod.MOD_ID, "invalid_genie"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, InvalidGenieEntity::new)
                    .dimensions(EntityDimensions.fixed(0.35F, 0.6F)).build());
    public static void register(){
        InvalidGenieMod.LOGGER.info("Registering entities for: " + InvalidGenieMod.MOD_ID);
    }
}
