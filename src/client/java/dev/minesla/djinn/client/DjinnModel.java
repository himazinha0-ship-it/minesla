package dev.minesla.djinn.client;

import dev.minesla.djinn.DjinnEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

public final class DjinnModel extends SinglePartEntityModel<DjinnEntity> implements ModelWithArms, ModelWithHead {
    private final ModelPart root, spirit, head, leftArm, rightArm, tail, tail2, tail3, guardArmor, royalArmor, sash, turban;
    public DjinnModel(ModelPart root) {
        this.root = root; spirit = root.getChild("spirit"); head = spirit.getChild("head");
        leftArm = spirit.getChild("left_arm"); rightArm = spirit.getChild("right_arm");
        tail = spirit.getChild("tail"); tail2 = tail.getChild("tail2"); tail3 = tail2.getChild("tail3");
        guardArmor = spirit.getChild("guard_armor"); royalArmor = spirit.getChild("royal_armor");
        sash = spirit.getChild("sash"); turban = head.getChild("turban");
    }
    public static TexturedModelData createModel() {
        ModelData data = new ModelData();
        ModelPartData spirit = data.getRoot().addChild("spirit", ModelPartBuilder.create(), ModelTransform.pivot(0, 24, 0));
        spirit.addChild("torso", ModelPartBuilder.create().uv(0, 0).cuboid(-5, -24, -3, 10, 12, 6)
                .uv(64, 32).cuboid(-5.5f, -25, -3.5f, 11, 2, 7), ModelTransform.NONE);
        ModelPartData head = spirit.addChild("head", ModelPartBuilder.create().uv(0, 32).cuboid(-4, -6, -4, 8, 8, 8)
                .uv(0, 96).cuboid(-2.8f, -3, -4.12f, 1.6f, 1.3f, .2f)
                .uv(0, 96).cuboid(1.2f, -3, -4.12f, 1.6f, 1.3f, .2f)
                .uv(64, 32).cuboid(-4.6f, -1.5f, -.5f, 1, 3, 1)
                .uv(64, 32).cuboid(3.6f, -1.5f, -.5f, 1, 3, 1), ModelTransform.pivot(0, -27, 0));
        head.addChild("turban", ModelPartBuilder.create().uv(32, 32).cuboid(-5, -9, -5, 10, 4, 10)
                .uv(32, 32).cuboid(-4.5f, -10.5f, -4.5f, 9, 2, 9)
                .uv(64, 32).cuboid(-1.5f, -10, -5.4f, 3, 5, 1)
                .uv(32, 96).cuboid(-.8f, -9.4f, -5.7f, 1.6f, 3, .5f)
                .uv(32, 32).cuboid(2, -5, 3.5f, 3, 11, 1), ModelTransform.NONE);
        spirit.addChild("left_arm", ModelPartBuilder.create().uv(0, 0).cuboid(0, -1, -2, 4, 6, 4)
                .uv(64, 32).cuboid(-.3f, 6, -2.3f, 4.6f, 2.5f, 4.6f), ModelTransform.pivot(5, -23, 0));
        spirit.addChild("right_arm", ModelPartBuilder.create().uv(0, 0).cuboid(-4, -1, -2, 4, 6, 4)
                .uv(64, 32).cuboid(-4.3f, 6, -2.3f, 4.6f, 2.5f, 4.6f), ModelTransform.pivot(-5, -23, 0));
        spirit.addChild("sash", ModelPartBuilder.create().uv(64, 64).cuboid(-5.3f, -14, -3.3f, 10.6f, 5, 6.6f)
                .uv(64, 32).cuboid(-5.6f, -15, -3.6f, 11.2f, 2, 7.2f)
                .uv(32, 96).cuboid(-1, -15, -4, 2, 3, .8f)
                .uv(64, 64).cuboid(3.2f, -10, -3.5f, 2.4f, 6, 1), ModelTransform.NONE);
        ModelPartData tail = spirit.addChild("tail", ModelPartBuilder.create().uv(64, 0).cuboid(-3.5f, 0, -2.5f, 7, 4, 5), ModelTransform.pivot(0, -10, 0));
        ModelPartData tail2 = tail.addChild("tail2", ModelPartBuilder.create().uv(64, 0).cuboid(-2.5f, 0, -2, 5, 4, 4), ModelTransform.pivot(.5f, 3, 0));
        tail2.addChild("tail3", ModelPartBuilder.create().uv(64, 0).cuboid(-1.5f, 0, -1.5f, 3, 5, 3)
                .uv(64, 0).cuboid(-.5f, 5, -.5f, 1, 2, 1), ModelTransform.pivot(0, 3, 0));
        spirit.addChild("guard_armor", ModelPartBuilder.create().uv(64, 32).cuboid(-7, -25, -4, 5, 3, 8)
                .uv(64, 32).cuboid(2, -25, -4, 5, 3, 8)
                .uv(96, 32).cuboid(-5.4f, -22, -3.5f, 10.8f, 6, 1), ModelTransform.NONE);
        spirit.addChild("royal_armor", ModelPartBuilder.create().uv(64, 32).cuboid(-8, -26, -4.5f, 6, 2, 9)
                .uv(64, 32).cuboid(2, -26, -4.5f, 6, 2, 9)
                .uv(32, 32).cuboid(-5, -13, 3, 10, 12, 1)
                .uv(32, 96).cuboid(-1, -22, -3.8f, 2, 6, 1), ModelTransform.NONE);
        RoyalGeometry.add(spirit, head, tail);
        return TexturedModelData.of(data, 128, 128);
    }
    @Override public ModelPart getPart() { return root; }
    @Override public ModelPart getHead() { return head; }
    @Override public void setArmAngle(Arm arm, MatrixStack matrices) {
        spirit.rotate(matrices);
        ModelPart armPart = arm == Arm.LEFT ? leftArm : rightArm;
        armPart.rotate(matrices);
        armPart.getChild("forearm").rotate(matrices);
        matrices.translate(0, -.375, 0);
    }
    @Override public void setAngles(DjinnEntity entity, float limbAngle, float limbDistance, float age, float yaw, float pitch) {
        root.traverse().forEach(ModelPart::resetTransform);
        float[] p = RoyalPose.sample(age, handSwingProgress, entity.castingTicks(), entity.role(), entity.isKing(), limbDistance);
        spirit.pivotY += p[0]; spirit.yaw = p[1]; spirit.pitch = p[2]; spirit.roll = p[3];
        head.yaw = yaw * MathHelper.RADIANS_PER_DEGREE - p[1];
        head.pitch = pitch * MathHelper.RADIANS_PER_DEGREE * .6f + p[18];
        rightArm.pitch = p[4]; rightArm.yaw = p[5]; rightArm.roll = p[6];
        leftArm.pitch = p[7]; leftArm.yaw = p[8]; leftArm.roll = p[9];
        tail.roll = p[10]; tail2.roll = p[11]; tail3.roll = p[12]; tail.yaw = p[13]; tail.pivotY = -13;
        rightArm.getChild("forearm").pitch = p[14]; leftArm.getChild("forearm").pitch = p[15];
        spirit.getChild("royal_cape").pitch = p[16]; spirit.getChild("royal_cape").roll = p[17];
        tail3.getChild("tail4").roll = p[19];
        guardArmor.visible = entity.role() == 2; royalArmor.visible = entity.isKing();
        head.getChild("royal_crown").visible = entity.isKing();
        for (String name : new String[]{"royal_cape", "royal_chain", "royal_tabard"}) spirit.getChild(name).visible = entity.isKing();
        for (ModelPart arm : new ModelPart[]{rightArm, leftArm}) {
            arm.getChild("royal_pauldron").visible = entity.isKing();
            arm.getChild("forearm").getChild("royal_gauntlet").visible = entity.isKing();
        }
        tail.getChild("royal_mist").visible = entity.isKing();
    }
}
