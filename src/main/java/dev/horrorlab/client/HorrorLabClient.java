package dev.horrorlab.client;

import dev.horrorlab.ModBlocks;
import dev.horrorlab.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class HorrorLabClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.MUTANT, MutantRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TOXIC_GAS, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ModBlocks.LAB_DOORS);
    }
}
