package net.invalid_genie.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.invalid_genie.InvalidGenieClient;
import net.invalid_genie.entity.InvalidGenieEntity;
import net.invalid_genie.gamerules.RuleRegister;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class InvalidGenieRenderer extends MobEntityRenderer<InvalidGenieEntity, InvalidGenieModel<InvalidGenieEntity>> {
    private static final Identifier TEXTURE = new Identifier("invalid_genie","textures/entity/invalid_genie.png");
    private static final Identifier JAX_TEXTURE = new Identifier("invalid_genie", "textures/entity/jax_genie.png");
    public InvalidGenieRenderer(EntityRendererFactory.Context context) {
        super(context, new InvalidGenieModel(context.getPart(InvalidGenieClient.INVALID_GENIE_LAYER)), 0.4F);
        this.addFeature(new EyesFeatureRenderer<InvalidGenieEntity, InvalidGenieModel<InvalidGenieEntity>>(this) {
            @Override
            public RenderLayer getEyesTexture() {
                if(InvalidGenieEntity.playerTarget != null) {
                    if (!InvalidGenieEntity.playerTarget.getWorld().getGameRules().getBoolean(RuleRegister.JAX_MODE)) {
                        return RenderLayer.getEyes(new Identifier("invalid_genie", "textures/entity/invalid_genie_e.png"));
                    } else {
                        return RenderLayer.getEyes(new Identifier("invalid_genie", "textures/entity/jax_genie_e.png"));
                    }
                }
                else{
                    return RenderLayer.getEyes(new Identifier("invalid_genie", "textures/entity/invalid_genie_e.png"));
                }
            }
        });
    }
    @Override
    public Identifier getTexture(InvalidGenieEntity entity) {
        if(InvalidGenieEntity.playerTarget != null) {
            if (!InvalidGenieEntity.playerTarget.getWorld().getGameRules().getBoolean(RuleRegister.JAX_MODE)) {
                return TEXTURE;
            } else {
                return JAX_TEXTURE;
            }
        }
        else{
            return TEXTURE;
        }
    }
}
