package net.invalid_genie;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.invalid_genie.entity.ModEntities;
import net.invalid_genie.entity.model.InvalidGenieModel;
import net.invalid_genie.entity.model.InvalidGenieRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class InvalidGenieClient implements ClientModInitializer {
	public static final EntityModelLayer INVALID_GENIE_LAYER = new EntityModelLayer(new Identifier(InvalidGenieMod.MOD_ID, "invalid_genie"), "main");
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.INVALID_GENIE, (context) -> {
			return new InvalidGenieRenderer(context);
		});
		EntityModelLayerRegistry.registerModelLayer(INVALID_GENIE_LAYER, InvalidGenieModel::getTexturedModelData);
	}
}