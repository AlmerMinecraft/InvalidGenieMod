package net.invalid_genie.gamerules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.invalid_genie.InvalidGenieMod;
import net.minecraft.world.GameRules;

public class RuleRegister {
    public static final GameRules.Key<GameRules.BooleanRule> JAX_MODE = GameRuleRegistry.register("jaxMode", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
    public static void register(){
        InvalidGenieMod.LOGGER.info("Registering rules for: " + InvalidGenieMod.MOD_ID);
    }
}
