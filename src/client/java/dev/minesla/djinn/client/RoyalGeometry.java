package dev.minesla.djinn.client;
import net.minecraft.client.model.*;
/** Generated from assets/djinns/geometry/royal.json. */
public final class RoyalGeometry {
    private RoyalGeometry() {}
    public static void add(ModelPartData spirit, ModelPartData head, ModelPartData tail) {
        head.addChild("royal_crown", ModelPartBuilder.create()
            .uv(64,32).cuboid(-5.7f,-10.9f,-5.7f,11.4f,1.2f,11.4f)
            .uv(64,32).cuboid(-4.9f,-12.0f,-4.9f,9.8f,1.2f,9.8f)
            .uv(64,32).cuboid(-4.55f,-13.3f,-5.8f,1.1f,2.5f,1.0f)
            .uv(64,32).cuboid(-2.55f,-13.3f,-5.8f,1.1f,2.5f,1.0f)
            .uv(64,32).cuboid(-0.55f,-13.3f,-5.8f,1.1f,2.5f,1.0f)
            .uv(64,32).cuboid(1.45f,-13.3f,-5.8f,1.1f,2.5f,1.0f)
            .uv(64,32).cuboid(3.45f,-13.3f,-5.8f,1.1f,2.5f,1.0f)
            .uv(32,96).cuboid(-1.4f,-13.7f,-6.1f,2.8f,4.3f,0.8f)
            .uv(64,32).cuboid(-1.9f,-11.8f,-5.9f,3.8f,1.0f,1.0f)
            .uv(64,32).cuboid(-3.4f,-4.4f,-4.4f,6.8f,0.55f,0.65f)
            .uv(0,0).cuboid(-0.55f,-2.8f,-4.55f,1.1f,2.3f,0.6f)
            .uv(64,32).cuboid(-3.6f,1.0f,-4.1f,7.2f,0.55f,0.65f)
            .uv(32,32).cuboid(-4.6f,-6.0f,3.4f,1.5f,14.0f,1.2f)
            .uv(64,32).cuboid(-4.699999999999999f,7.0f,3.3f,1.7f,1.5f,1.4f)
            .uv(32,32).cuboid(3.1f,-6.0f,3.4f,1.5f,14.0f,1.2f)
            .uv(64,32).cuboid(3.0f,7.0f,3.3f,1.7f,1.5f,1.4f), ModelTransform.pivot(0.0f,0.0f,0.0f));
        spirit.addChild("royal_cape", ModelPartBuilder.create()
            .uv(32,32).cuboid(-5.5f,0.0f,0.0f,11.0f,14.0f,1.0f)
            .uv(64,32).cuboid(-5.7f,12.0f,-0.1f,11.4f,1.5f,1.2f)
            .uv(64,32).cuboid(-4.6000000000000005f,1.0f,-0.15f,0.8f,11.0f,1.4f)
            .uv(64,32).cuboid(-0.4f,1.0f,-0.15f,0.8f,11.0f,1.4f)
            .uv(64,32).cuboid(3.8000000000000003f,1.0f,-0.15f,0.8f,11.0f,1.4f), ModelTransform.pivot(0.0f,-22.0f,3.6f));
        spirit.addChild("royal_chain", ModelPartBuilder.create()
            .uv(64,32).cuboid(-5.7f,-23.0f,-3.8f,11.4f,2.0f,1.5f)
            .uv(32,96).cuboid(-1.4f,-23.2f,-4.1f,2.8f,3.8f,1.0f)
            .uv(64,32).cuboid(-5.7f,-18.5f,-3.8f,11.4f,1.2f,1.5f)
            .uv(64,32).cuboid(-3.8f,-20.8f,-3.85f,1.0f,2.3f,1.2f)
            .uv(64,32).cuboid(2.8f,-20.8f,-3.85f,1.0f,2.3f,1.2f)
            .uv(64,32).cuboid(-4.95f,-21.5f,-4.25f,0.9f,0.9f,0.9f)
            .uv(32,32).cuboid(-4.72f,-21.35f,-4.4f,0.44f,0.44f,0.2f)
            .uv(64,32).cuboid(-3.6500000000000004f,-20.5f,-4.25f,0.9f,0.9f,0.9f)
            .uv(32,32).cuboid(-3.4200000000000004f,-20.35f,-4.4f,0.44f,0.44f,0.2f)
            .uv(64,32).cuboid(-2.25f,-19.9f,-4.25f,0.9f,0.9f,0.9f)
            .uv(32,32).cuboid(-2.02f,-19.75f,-4.4f,0.44f,0.44f,0.2f)
            .uv(64,32).cuboid(1.35f,-19.9f,-4.25f,0.9f,0.9f,0.9f)
            .uv(32,32).cuboid(1.58f,-19.75f,-4.4f,0.44f,0.44f,0.2f)
            .uv(64,32).cuboid(2.75f,-20.5f,-4.25f,0.9f,0.9f,0.9f)
            .uv(32,32).cuboid(2.98f,-20.35f,-4.4f,0.44f,0.44f,0.2f)
            .uv(64,32).cuboid(4.05f,-21.5f,-4.25f,0.9f,0.9f,0.9f)
            .uv(32,32).cuboid(4.28f,-21.35f,-4.4f,0.44f,0.44f,0.2f), ModelTransform.pivot(0.0f,0.0f,0.0f));
        spirit.getChild("right_arm").addChild("forearm", ModelPartBuilder.create()
            .uv(0,0).cuboid(-4.0f,0.0f,-2.0f,4.0f,6.0f,4.0f)
            .uv(64,32).cuboid(-4.35f,3.0f,-2.35f,4.7f,2.0f,4.7f)
            .uv(0,0).cuboid(-4.0f,5.0f,-2.1f,4.0f,2.0f,4.2f), ModelTransform.pivot(0.0f,6.0f,0.0f));
        spirit.getChild("right_arm").addChild("royal_pauldron", ModelPartBuilder.create()
            .uv(64,32).cuboid(-5.0f,-2.6f,-3.3f,5.5f,2.4f,6.6f)
            .uv(64,32).cuboid(-5.4f,-1.0f,-3.5f,6.3f,1.5f,7.0f)
            .uv(32,96).cuboid(-3.4f,-0.5f,-3.8f,1.6f,2.2f,0.8f)
            .uv(64,32).cuboid(-4.9f,0.4f,-2.9f,1.5f,0.65f,5.8f)
            .uv(64,32).cuboid(-4.9f,1.2000000000000002f,-2.9f,1.5f,0.65f,5.8f)
            .uv(64,32).cuboid(-4.9f,2.0f,-2.9f,1.5f,0.65f,5.8f), ModelTransform.pivot(0.0f,0.0f,0.0f));
        spirit.getChild("right_arm").getChild("forearm").addChild("royal_gauntlet", ModelPartBuilder.create()
            .uv(64,32).cuboid(-4.5f,0.5f,-2.6f,5.0f,4.3f,5.2f)
            .uv(32,96).cuboid(-2.7f,1.4f,-2.9f,1.4f,2.6f,0.7f), ModelTransform.pivot(0.0f,0.0f,0.0f));
        spirit.getChild("left_arm").addChild("forearm", ModelPartBuilder.create()
            .uv(0,0).cuboid(0.0f,0.0f,-2.0f,4.0f,6.0f,4.0f)
            .uv(64,32).cuboid(-0.35f,3.0f,-2.35f,4.7f,2.0f,4.7f)
            .uv(0,0).cuboid(0.0f,5.0f,-2.1f,4.0f,2.0f,4.2f), ModelTransform.pivot(0.0f,6.0f,0.0f));
        spirit.getChild("left_arm").addChild("royal_pauldron", ModelPartBuilder.create()
            .uv(64,32).cuboid(-0.5f,-2.6f,-3.3f,5.5f,2.4f,6.6f)
            .uv(64,32).cuboid(-0.9f,-1.0f,-3.5f,6.3f,1.5f,7.0f)
            .uv(32,96).cuboid(1.8f,-0.5f,-3.8f,1.6f,2.2f,0.8f)
            .uv(64,32).cuboid(3.4f,0.4f,-2.9f,1.5f,0.65f,5.8f)
            .uv(64,32).cuboid(3.4f,1.2000000000000002f,-2.9f,1.5f,0.65f,5.8f)
            .uv(64,32).cuboid(3.4f,2.0f,-2.9f,1.5f,0.65f,5.8f), ModelTransform.pivot(0.0f,0.0f,0.0f));
        spirit.getChild("left_arm").getChild("forearm").addChild("royal_gauntlet", ModelPartBuilder.create()
            .uv(64,32).cuboid(-0.5f,0.5f,-2.6f,5.0f,4.3f,5.2f)
            .uv(32,96).cuboid(1.3f,1.4f,-2.9f,1.4f,2.6f,0.7f), ModelTransform.pivot(0.0f,0.0f,0.0f));
        spirit.addChild("royal_tabard", ModelPartBuilder.create()
            .uv(32,32).cuboid(-3.0f,0.0f,0.0f,6.0f,8.0f,1.0f)
            .uv(64,32).cuboid(-3.1f,6.8f,-0.1f,6.2f,1.2f,1.2f)
            .uv(64,32).cuboid(-0.45f,0.2f,-0.2f,0.9f,6.7f,1.4f), ModelTransform.pivot(0.0f,-13.0f,-3.7f));
        tail.addChild("royal_mist", ModelPartBuilder.create()
            .uv(64,0).cuboid(-4.0f,0.0f,-3.0f,8.0f,3.0f,6.0f)
            .uv(32,96).cuboid(-0.9f,1.0f,-3.2f,1.8f,3.0f,0.7f), ModelTransform.pivot(0.0f,0.0f,0.0f));
        tail.getChild("tail2").getChild("tail3").addChild("tail4", ModelPartBuilder.create()
            .uv(64,0).cuboid(-0.9f,0.0f,-0.9f,1.8f,3.0f,1.8f), ModelTransform.pivot(0.0f,5.0f,0.0f));
    }
}
