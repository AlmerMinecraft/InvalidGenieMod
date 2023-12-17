package net.invalid_genie;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.invalid_genie.entity.InvalidGenieEntity;
import net.invalid_genie.entity.ModEntities;
import net.invalid_genie.gamerules.RuleRegister;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidGenieMod implements ModInitializer {
	public static final String MOD_ID = "invalid_genie";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		ModEntities.register();
		FabricDefaultAttributeRegistry.register(ModEntities.INVALID_GENIE, InvalidGenieEntity.createAttributes());
		RuleRegister.register();
	}
}