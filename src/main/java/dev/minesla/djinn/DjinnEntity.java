package dev.minesla.djinn;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.boss.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.village.*;
import net.minecraft.world.*;

import java.util.*;

public class DjinnEntity extends MerchantEntity {
    private static final TrackedData<Integer> JOB = DataTracker.registerData(DjinnEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> CAST = DataTracker.registerData(DjinnEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final String[] JOBS = {"farmer", "fisherman", "shepherd", "fletcher", "librarian", "cartographer",
            "cleric", "armorer", "weaponsmith", "toolsmith", "butcher", "leatherworker", "mason"};
    private final int color, role;
    private final boolean king;
    private final ServerBossBar bossBar = new ServerBossBar(Text.translatable("entity.djinns.djinn_king"),
            BossBar.Color.BLUE, BossBar.Style.PROGRESS);
    private UUID enemy, summoner;
    private int angerTicks, summonLife, experience, lastLevel = 1, riftCooldown = 160, riftAge = -1;
    private long restockDay = -1;
    private final List<Vec3d> rifts = new ArrayList<>();

    public DjinnEntity(EntityType<? extends MerchantEntity> type, World world, int color, int role, boolean king) {
        super(type, world);
        this.color = color; this.role = role; this.king = king;
        this.moveControl = new FlightMoveControl(this, 18, true);
        setNoGravity(true);
        this.experiencePoints = king ? 100 : role == 2 ? 10 : 5;
        setPathfindingPenalty(PathNodeType.WATER, -1);
        setPathfindingPenalty(PathNodeType.DANGER_FIRE, 0);
        setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, 0);
    }

    public int color() { return color; }
    public int role() { return role; }
    public boolean isKing() { return king; }
    public int castingTicks() { return dataTracker.get(CAST); }
    public int job() { return MathHelper.clamp(dataTracker.get(JOB), 0, JOBS.length - 1); }
    public int level() { return experience >= 250 ? 5 : experience >= 150 ? 4 : experience >= 70 ? 3 : experience >= 20 ? 2 : 1; }

    @Override protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(JOB, 0); builder.add(CAST, 0);
    }
    @Override protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanPathThroughDoors(true);
        navigation.setCanEnterOpenDoors(true);
        navigation.setCanSwim(false);
        return navigation;
    }
    @Override protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new DjinnCombatGoal(this));
        goalSelector.add(5, new FlyGoal(this, 0.7));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason,
                                           EntityData entityData) {
        EntityData result = super.initialize(world, difficulty, reason, entityData);
        dataTracker.set(JOB, role == 3 ? 4 : role == 2 ? 7 + random.nextInt(3) : random.nextInt(JOBS.length));
        if (king) {
            equipStack(EquipmentSlot.MAINHAND, new ItemStack(Djinns.ROYAL_SWORD));
            setPersistent();
        } else if (role == 2) {
            equipStack(EquipmentSlot.MAINHAND, new ItemStack(random.nextBoolean() ? Djinns.SPEAR : Djinns.FLAME_SWORD));
            equipStack(EquipmentSlot.OFFHAND, new ItemStack(Djinns.DESERT_SHIELD));
            equipStack(EquipmentSlot.CHEST, new ItemStack(Djinns.DESERT_CHESTPLATE));
        } else if (role == 3) equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BLAZE_ROD));
        else if (random.nextInt(4) == 0) equipStack(EquipmentSlot.MAINHAND, new ItemStack(Djinns.SPEAR));
        return result;
    }

    @Override public void tickHandSwing() {
        if (!isKing()) { super.tickHandSwing(); return; }
        if (handSwinging) {
            if (++handSwingTicks >= RoyalBalance.SWING_TICKS) { handSwingTicks = 0; handSwinging = false; }
        } else handSwingTicks = 0;
        handSwingProgress = (float)handSwingTicks / RoyalBalance.SWING_TICKS;
    }

    @Override public void tick() {
        super.tick();
        setNoGravity(true);
        fallDistance = 0;
        if (getWorld().isClient) {
            if (age % 3 == 0) getWorld().addParticle(particle(), getX() + (random.nextDouble() - .5) * .6,
                    getY() + .25, getZ() + (random.nextDouble() - .5) * .6, 0, -.025, 0);
            return;
        }
        if (hasCustomer()) getNavigation().stop();
        if (castingTicks() > 0) dataTracker.set(CAST, castingTicks() - 1);
        if (angerTicks > 0) angerTicks--;
        if (angerTicks == 0 && summoner == null) { enemy = null; setTarget(null); }
        if (enemy != null && getTarget() == null && ((ServerWorld)getWorld()).getEntity(enemy) instanceof LivingEntity living
                && living.isAlive() && !(living instanceof PlayerEntity player && (player.isCreative() || player.isSpectator()))) setTarget(living);
        if (getTarget() != null && (!getTarget().isAlive() || getTarget().squaredDistanceTo(this) > 1600
                || getTarget() instanceof PlayerEntity p && (p.isCreative() || p.isSpectator()))) {
            setTarget(null); enemy = null; angerTicks = 0;
        }
        if (summoner != null && --summonLife <= 0) { discard(); return; }
        if (age % 200 == 0 && !hasCustomer()) {
            long day = getWorld().getTimeOfDay() / 24000;
            if (day != restockDay) { getOffers().forEach(TradeOffer::resetUses); restockDay = day; }
        }
        if (king) tickKing();
    }

    private ParticleEffect particle() {
        return switch (color) { case 1 -> ParticleTypes.FLAME; case 2 -> ParticleTypes.CLOUD;
            case 3 -> ParticleTypes.SMOKE; default -> ParticleTypes.SOUL_FIRE_FLAME; };
    }

    private void becomeAngry(LivingEntity attacker) {
        if (attacker instanceof PlayerEntity p && (p.isCreative() || p.isSpectator())) return;
        enemy = attacker.getUuid(); angerTicks = 600; setTarget(attacker); setCustomer(null);
    }

    @Override public boolean damage(DamageSource source, float amount) {
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.FALL)) return false;
        if (color == 1 && source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE)) return false;
        if (role == 2 && getOffHandStack().isOf(Djinns.DESERT_SHIELD) && source.getAttacker() != null
                && random.nextFloat() < .25f && source.getAttacker().getPos().subtract(getPos()).normalize().dotProduct(getRotationVec(1)) > 0) {
            playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1, 1); return false;
        }
        boolean hit = super.damage(source, amount);
        if (hit && source.getAttacker() instanceof LivingEntity attacker && attacker != this
                && !(attacker instanceof DjinnEntity)) {
            becomeAngry(attacker);
            for (DjinnEntity ally : getWorld().getEntitiesByClass(DjinnEntity.class, getBoundingBox().expand(32), Entity::isAlive))
                if (squaredDistanceTo(ally) <= 1024) ally.becomeAngry(attacker);
            if (source.getSource() == attacker && source.getAttacker() instanceof ServerPlayerEntity player
                    && random.nextFloat() < .15f) Djinns.possess(player);
        }
        return hit;
    }

    @Override public boolean tryAttack(Entity target) {
        swingHand(Hand.MAIN_HAND);
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            applyElement(living);
            if (getMainHandStack().getItem() instanceof FlameSword) living.setOnFireFor(5);
        }
        return hit;
    }

    private void applyElement(LivingEntity victim) {
        switch (color) {
            case 1 -> victim.setOnFireFor(4);
            case 2 -> victim.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60));
            case 3 -> {
                victim.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
                victim.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100));
            }
            default -> { if (role == 3) victim.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80)); }
        }
    }

    public void castAt(LivingEntity target) {
        dataTracker.set(CAST, 20);
        Vec3d start = getEyePos();
        Vec3d direction = target.getEyePos().subtract(start).normalize();
        if (color == 1) {
            // Same projectile as a blaze. Owner and mobGriefing retain vanilla fireball behavior.
            SmallFireballEntity fireball = new SmallFireballEntity(getWorld(), this, direction.multiply(.65));
            fireball.setPosition(start.add(direction));
            getWorld().spawnEntity(fireball);
            playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
        } else {
            if (target.damage(getDamageSources().indirectMagic(this, this), role == 3 ? 5 : 3)) applyElement(target);
            ServerWorld world = (ServerWorld)getWorld();
            for (int i = 0; i <= 20; i++) {
                Vec3d point = start.lerp(target.getEyePos(), i / 20.0);
                world.spawnParticles(particle(), point.x, point.y, point.z, 1, .04, .04, .04, .005);
            }
            playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, .7f, 1.3f);
        }
    }

    private void tickKing() {
        bossBar.setPercent(MathHelper.clamp(getHealth() / getMaxHealth(), 0, 1));
        bossBar.setName(getDisplayName());
        bossBar.setVisible(getTarget() != null);
        if (getTarget() != null && --riftCooldown <= 0 && riftAge < 0) {
            riftCooldown = getHealth() < getMaxHealth() / 2 ? 180 : 280;
            riftAge = 0; rifts.clear(); dataTracker.set(CAST, 60);
            for (int i = 0; i < 3; i++) {
                double angle = i * Math.PI * 2 / 3 + random.nextDouble();
                rifts.add(getPos().add(Math.cos(angle) * 4, .2, Math.sin(angle) * 4));
            }
            playSound(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, .6f);
        }
        if (riftAge >= 0) {
            ServerWorld world = (ServerWorld)getWorld();
            for (Vec3d center : rifts) {
                for (int i = 0; i < 12; i++) {
                    double angle = i * Math.PI / 6 + riftAge * .14;
                    world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x + Math.cos(angle) * .65,
                            center.y + 1.2 + Math.sin(angle) * 1.2, center.z, 1, .02, .02, .02, 0);
                }
                if (riftAge == 30 && world.getEntitiesByClass(DjinnEntity.class, getBoundingBox().expand(40),
                        e -> getUuid().equals(e.summoner)).size() < 6) {
                    DjinnEntity guard = Djinns.TYPES.get("blue_guard_djinn").create(world);
                    if (guard != null) {
                        guard.refreshPositionAndAngles(center.x, center.y, center.z, getYaw(), 0);
                        guard.initialize(world, world.getLocalDifficulty(guard.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
                        if (world.isSpaceEmpty(guard)) {
                            guard.summoner = getUuid(); guard.summonLife = 1200;
                            guard.experiencePoints = 0;
                            if (getTarget() != null) guard.becomeAngry(getTarget());
                            world.spawnEntity(guard);
                        }
                    }
                }
            }
            if (++riftAge > 60) { riftAge = -1; rifts.clear(); }
        }
    }

    @Override public void onStartedTrackingBy(ServerPlayerEntity player) { super.onStartedTrackingBy(player); if (king) bossBar.addPlayer(player); }
    @Override public void onStoppedTrackingBy(ServerPlayerEntity player) { super.onStoppedTrackingBy(player); if (king) bossBar.removePlayer(player); }
    @Override public PassiveEntity createChild(ServerWorld world, PassiveEntity other) { return null; }
    @Override public boolean canImmediatelyDespawn(double distanceSquared) { return false; }
    @Override protected boolean shouldDropLoot() { return summoner == null && super.shouldDropLoot(); }
    @Override public float getDropChance(EquipmentSlot slot) {
        if (summoner != null) return 0;
        if (slot == EquipmentSlot.MAINHAND) return king ? 1 : .12f;
        return super.getDropChance(slot);
    }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_ALLAY_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_ALLAY_DEATH; }
    @Override public SoundEvent getYesSound() { return SoundEvents.ENTITY_VILLAGER_YES; }

    @Override protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.getStackInHand(hand).getItem() instanceof SpawnEggItem) return super.interactMob(player, hand);
        if (king || role == 2 || getTarget() != null || !isAlive()) return ActionResult.PASS;
        if (!getWorld().isClient && !hasCustomer()) {
            int profession = workstation(player.getStackInHand(hand).getItem());
            if (profession >= 0 && experience == 0 && role != 3) {
                dataTracker.set(JOB, profession); offers = null;
                player.sendMessage(Text.translatable("message.djinns.profession", Text.translatable("profession.djinns." + JOBS[job()])), true);
                return ActionResult.SUCCESS;
            }
            setCustomer(player); setPersistent();
            sendOffers(player, getDisplayName().copy().append(Text.literal(" · "))
                    .append(Text.translatable("profession.djinns." + JOBS[job()])), level());
        }
        return ActionResult.success(getWorld().isClient);
    }

    private int workstation(Item item) {
        if (!(item instanceof BlockItem blockItem)) return -1;
        String block = net.minecraft.registry.Registries.BLOCK.getId(blockItem.getBlock()).getPath();
        return switch (block) {
            case "composter" -> 0; case "barrel" -> 1; case "loom" -> 2; case "fletching_table" -> 3;
            case "lectern" -> 4; case "cartography_table" -> 5; case "brewing_stand" -> 6;
            case "blast_furnace" -> 7; case "grindstone" -> 8; case "smithing_table" -> 9;
            case "smoker" -> 10; case "cauldron" -> 11; case "stonecutter" -> 12; default -> -1;
        };
    }

    private void sell(Item item, int count, int lapis) {
        offers.add(new TradeOffer(new TradedItem(Items.LAPIS_LAZULI, lapis), new ItemStack(item, count), 12, 5, .02f));
    }
    @Override protected void fillRecipes() {
        if (king || role == 2) return;
        if (role == 3) {
            sell(Djinns.SCROLL_FLAME, 1, 12); sell(Djinns.SCROLL_WIND, 1, 10);
            sell(Djinns.SCROLL_SHADOW, 1, 14); sell(Djinns.SCROLL_WARD, 1, 16);
            if (level() >= 3) sell(Items.ENDER_PEARL, 2, 10);
            return;
        }
        switch (job()) {
            case 0 -> { sell(Items.BREAD, 8, 3); sell(Items.GOLDEN_CARROT, 3, 7); }
            case 1 -> { sell(Items.COOKED_COD, 8, 3); sell(Items.FISHING_ROD, 1, 6); }
            case 2 -> { sell(Items.WHITE_WOOL, 12, 4); sell(Items.WHITE_BED, 1, 6); }
            case 3 -> { sell(Items.ARROW, 24, 4); sell(Items.BOW, 1, 8); }
            case 4 -> { sell(Items.BOOK, 4, 3); sell(Items.BOOKSHELF, 2, 8); }
            case 5 -> { sell(Items.MAP, 2, 5); sell(Items.COMPASS, 1, 7); }
            case 6 -> { sell(Items.REDSTONE, 12, 3); sell(Items.GLOWSTONE, 4, 6); }
            case 7 -> { sell(Djinns.DESERT_HELMET, 1, 12); sell(Djinns.DESERT_BOOTS, 1, 10); }
            case 8 -> { sell(Djinns.SPEAR, 1, 12); sell(Items.IRON_SWORD, 1, 8); }
            case 9 -> { sell(Items.IRON_PICKAXE, 1, 8); sell(Items.IRON_AXE, 1, 8); }
            case 10 -> { sell(Items.COOKED_BEEF, 8, 4); sell(Items.COOKED_CHICKEN, 8, 3); }
            case 11 -> { sell(Items.LEATHER, 8, 4); sell(Items.SADDLE, 1, 14); }
            case 12 -> { sell(Items.CUT_SANDSTONE, 32, 3); sell(Items.TERRACOTTA, 24, 4); }
        }
        if (role == 1) { sell(Djinns.DESERT_SHIELD, 1, 12); sell(Djinns.SPEAR, 1, 14); }
        if (level() >= 2) sell(Djinns.DESERT_LEGGINGS, 1, 20);
        if (level() >= 3) sell(Djinns.DESERT_CHESTPLATE, 1, 24);
        if (level() >= 4) sell(Djinns.FLAME_SWORD, 1, 32);
        if (level() >= 5) sell(Djinns.SCROLL_WARD, 2, 24);
    }
    @Override protected void afterUsing(TradeOffer offer) {
        experience += offer.getMerchantExperience();
        if (level() > lastLevel) {
            lastLevel = level();
            // Refresh after closing the screen, never mutate the active offer indices.
            pendingPromotion = true;
        }
        addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60));
    }
    private boolean pendingPromotion;
    @Override public void setCustomer(PlayerEntity player) {
        super.setCustomer(player);
        if (player == null && pendingPromotion) { offers = null; pendingPromotion = false; }
    }
    @Override public int getExperience() { return experience; }
    @Override public boolean isLeveledMerchant() { return true; }
    @Override public boolean canRefreshTrades() { return true; }

    @Override public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("DjinnJob", job()); nbt.putInt("DjinnXp", experience);
        nbt.putInt("AngerTicks", angerTicks); nbt.putLong("RestockDay", restockDay);
        if (enemy != null) nbt.putUuid("DjinnEnemy", enemy);
        if (summoner != null) { nbt.putUuid("Summoner", summoner); nbt.putInt("SummonLife", summonLife); }
    }
    @Override public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        dataTracker.set(JOB, MathHelper.clamp(nbt.getInt("DjinnJob"), 0, JOBS.length - 1));
        experience = nbt.getInt("DjinnXp"); lastLevel = level();
        angerTicks = nbt.getInt("AngerTicks"); restockDay = nbt.getLong("RestockDay");
        enemy = nbt.containsUuid("DjinnEnemy") ? nbt.getUuid("DjinnEnemy") : null;
        summoner = nbt.containsUuid("Summoner") ? nbt.getUuid("Summoner") : null;
        summonLife = nbt.getInt("SummonLife");
        var attack = getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
        attack.setBaseValue(RoyalBalance.attackBase(attack.getBaseValue(), isKing()));
    }
}
