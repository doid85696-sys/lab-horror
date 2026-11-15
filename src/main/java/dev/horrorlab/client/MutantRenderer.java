package dev.horrorlab.client;

import dev.horrorlab.HorrorLabMod;
import dev.horrorlab.entity.MutantEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MutantRenderer extends BipedEntityRenderer<MutantEntity, BipedEntityModel<MutantEntity>> {
    private static final Identifier TEXTURE = HorrorLabMod.id("textures/entity/mutant.png");

    public MutantRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.6f);
    }

    @Override
    protected void scale(MutantEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.15f, 1.15f, 1.15f);
    }

    @Override
    public Identifier getTexture(MutantEntity entity) {
        return TEXTURE;
    }
}
