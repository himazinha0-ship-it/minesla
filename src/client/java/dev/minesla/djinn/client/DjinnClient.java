package dev.minesla.djinn.client;

import dev.minesla.djinn.Djinns;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.ShieldEntityModel;

public final class DjinnClient implements ClientModInitializer {
    public static final EntityModelLayer MODEL = new EntityModelLayer(Djinns.id("djinn"), "main");
    @Override public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(MODEL, DjinnModel::createModel);
        Djinns.TYPES.values().forEach(type -> EntityRendererRegistry.register(type, DjinnRenderer::new));
        BuiltinItemRendererRegistry.INSTANCE.register(Djinns.DESERT_SHIELD,
                (stack, mode, matrices, consumers, light, overlay) -> {
                    ShieldEntityModel shield = new ShieldEntityModel(ShieldEntityModel.getTexturedModelData().createModel());
                    matrices.push();
                    matrices.scale(1, -1, -1);
                    var vertices = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(Djinns.id("textures/entity/desert_shield.png")));
                    shield.getHandle().render(matrices, vertices, light, overlay);
                    shield.getPlate().render(matrices, vertices, light, overlay);
                    matrices.pop();
                });
        net.minecraft.client.item.ModelPredicateProviderRegistry.register(Djinns.DESERT_SHIELD,
                net.minecraft.util.Identifier.ofVanilla("blocking"), (stack, world, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1 : 0);
    }
}
