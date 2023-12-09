package net.invalid_genie;

import net.fabricmc.api.ModInitializer;

import net.invalid_genie.entity.ModEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidGenieMod implements ModInitializer {
	public static final String MOD_ID = "invalid_genie";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		ModEntities.register();
	}
}