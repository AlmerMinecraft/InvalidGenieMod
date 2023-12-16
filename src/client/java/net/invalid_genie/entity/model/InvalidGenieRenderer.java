package net.invalid_genie.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.invalid_genie.InvalidGenieClient;
import net.invalid_genie.entity.InvalidGenieEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class InvalidGenieRenderer extends MobEntityRenderer<InvalidGenieEntity, InvalidGenieModel<InvalidGenieEntity>> {
    private static final Identifier TEXTURE = new Identifier("invalid_genie","textures/entity/invalid_genie.png");
    public InvalidGenieRenderer(EntityRendererFactory.Context context) {
        super(context, new InvalidGenieModel(context.getPart(InvalidGenieClient.INVALID_GENIE_LAYER)), 0.4F);
        this.addFeature(new EyesFeatureRenderer<InvalidGenieEntity, InvalidGenieModel<InvalidGenieEntity>>(this) {
            @Override
            public RenderLayer getEyesTexture() {
                return RenderLayer.getEyes(new Identifier("invalid_genie", "textures/entity/invalid_genie_e.png"));
            }
        });
    }
    @Override
    public Identifier getTexture(InvalidGenieEntity entity) {
        return TEXTURE;
    }
}
