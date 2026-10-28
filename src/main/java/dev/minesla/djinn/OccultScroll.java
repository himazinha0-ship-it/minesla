package dev.minesla.djinn;

import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class OccultScroll extends Item {
    public enum Kind { FLAME, WIND, SHADOW, WARD }
    private final Kind kind;
    public OccultScroll(Kind kind) { super(new Settings().maxCount(16).rarity(Rarity.RARE)); this.kind = kind; }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            switch (kind) {
                case FLAME -> {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200));
                    world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, user.getBoundingBox().expand(5),
                            e -> e != user && user.squaredDistanceTo(e) <= 25
                                    && !(e instanceof PlayerEntity) && user.canSee(e)).forEach(e -> e.setOnFireFor(6));
                }
                case WIND -> {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 0));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 400));
                }
                case SHADOW -> {
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 600));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 600));
                }
                case WARD -> {
                    user.removeStatusEffect(Djinns.POSSESSION);
                    user.removeStatusEffect(StatusEffects.BLINDNESS);
                    user.removeStatusEffect(StatusEffects.NAUSEA);
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100));
                }
            }
            ((ServerWorld)world).spawnParticles(ParticleTypes.ENCHANT, user.getX(), user.getY() + 1, user.getZ(), 45, 0.7, 1, 0.7, 0.15);
            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1, 0.8f);
            user.getItemCooldownManager().set(this, 100);
            if (!user.isCreative()) stack.decrement(1);
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
