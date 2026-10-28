package dev.minesla.djinn;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;

public final class FlameSword extends SwordItem {
    public FlameSword(Settings settings) { super(ToolMaterials.DIAMOND, settings); }
    @Override public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setOnFireFor(5);
        return super.postHit(stack, target, attacker);
    }
}
