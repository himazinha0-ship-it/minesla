package dev.minesla.djinn.client;

import dev.minesla.djinn.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class DjinnRenderer extends MobEntityRenderer<DjinnEntity, DjinnModel> {
    public DjinnRenderer(EntityRendererFactory.Context context) {
        super(context, new DjinnModel(context.getPart(DjinnClient.MODEL)), .45f);
        addFeature(new HeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
        addFeature(new FeatureRenderer<DjinnEntity, DjinnModel>(this) {
            @Override public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light,
                                         DjinnEntity entity, float limbAngle, float limbDistance, float tickDelta,
                                         float animationProgress, float headYaw, float headPitch) {
                var layer = RenderLayer.getEyes(Djinns.id("textures/entity/" + (entity.isKing() ? "king_eyes" : "eyes") + ".png"));
                getContextModel().render(matrices, vertices.getBuffer(layer), 15728640, OverlayTexture.DEFAULT_UV);
            }
        });
    }
    @Override public Identifier getTexture(DjinnEntity entity) {
        return Djinns.id("textures/entity/" + (entity.isKing() ? "king" : Djinns.COLORS[entity.color()] + "_" + Djinns.ROLES[entity.role()]) + ".png");
    }
    @Override protected void scale(DjinnEntity entity, MatrixStack matrices, float tickDelta) {
        if (entity.isKing()) matrices.scale(2.05f, 2.05f, 2.05f);
    }
}
