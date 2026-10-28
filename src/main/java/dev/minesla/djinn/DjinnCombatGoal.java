package dev.minesla.djinn;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

final class DjinnCombatGoal extends Goal {
    private final DjinnEntity mob;
    private int attackCooldown, rangedCooldown;
    DjinnCombatGoal(DjinnEntity mob) { this.mob = mob; setControls(EnumSet.of(Control.MOVE, Control.LOOK)); }
    @Override public boolean canStart() { return mob.getTarget() != null && mob.getTarget().isAlive(); }
    @Override public boolean shouldContinue() { return canStart(); }
    @Override public boolean shouldRunEveryTick() { return true; }
    @Override public void stop() { mob.getNavigation().stop(); }
    @Override public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        mob.getLookControl().lookAt(target, 30, 30);
        double distance = mob.squaredDistanceTo(target);
        if (--attackCooldown < 0) attackCooldown = 0;
        if (--rangedCooldown < 0) rangedCooldown = 0;
        boolean ranged = !mob.isKing() && (mob.color() == 1 || mob.role() == 3);
        if (ranged && distance < 196 && distance > 9 && mob.canSee(target)) {
            mob.getNavigation().stop();
            if (rangedCooldown == 0) { mob.castAt(target); rangedCooldown = mob.color() == 1 ? 30 : 55; }
        } else mob.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), mob.isKing() ? 1.25 : 1.1);
        double reach = mob.isKing() ? 4.4 : mob.getMainHandStack().isOf(Djinns.SPEAR) ? 3.0 : 2.2;
        if (distance <= reach * reach && mob.canSee(target) && attackCooldown == 0) {
            mob.tryAttack(target);
            attackCooldown = mob.isKing() ? 28 : 28;
            if (mob.isKing()) {
                Vec3d forward = target.getPos().subtract(mob.getPos()).normalize();
                for (LivingEntity other : mob.getWorld().getEntitiesByClass(LivingEntity.class,
                        mob.getBoundingBox().expand(3), e -> e != target && e != mob && !(e instanceof DjinnEntity) && e.isAlive())) {
                    Vec3d offset = other.getPos().subtract(mob.getPos()).normalize();
                    if (offset.dotProduct(forward) > .35 && mob.canSee(other)) {
                        other.damage(mob.getDamageSources().mobAttack(mob), 10);
                        other.takeKnockback(.65, mob.getX() - other.getX(), mob.getZ() - other.getZ());
                    }
                }
            }
        }
    }
}
