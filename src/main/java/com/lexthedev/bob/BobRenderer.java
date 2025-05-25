package com.lexthedev.bob;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BobRenderer extends MobRenderer<BobEntity, PlayerModel<BobEntity>> {
    private static final ResourceLocation BOB_TEXTURE = new ResourceLocation("bob", "textures/entity/bob.png");

    public BobRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(BobEntity entity) {
        return BOB_TEXTURE;
    }
}