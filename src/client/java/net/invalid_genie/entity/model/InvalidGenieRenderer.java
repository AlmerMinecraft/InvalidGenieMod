package net.invalid_genie.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.invalid_genie.entity.InvalidGenieEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class InvalidGenieRenderer extends MobEntityRenderer<InvalidGenieEntity, InvalidGenieModel<InvalidGenieEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/invalid_genie.png");
    public InvalidGenieRenderer(EntityRendererFactory.Context context, InvalidGenieModel entityModel, float f) {
        super(context, new InvalidGenieModel(context.getPart(EntityModelLayers.ALLAY)), 0.4F);
    }
    @Override
    public Identifier getTexture(InvalidGenieEntity entity) {
        return TEXTURE;
    }
}
