package dev.minesla.djinn;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.block.Blocks;

import java.util.*;

public final class Djinns implements ModInitializer {
    public static final String ID = "djinns";
    public static final String[] COLORS = {"blue", "red", "white", "black"};
    public static final String[] ROLES = {"common", "merchant", "guard", "mage"};
    public static final Map<String, EntityType<DjinnEntity>> TYPES = new LinkedHashMap<>();
    public static final List<Item> ITEMS = new ArrayList<>();
    public static EntityType<DjinnEntity> KING;
    public static Item SPEAR, FLAME_SWORD, ROYAL_SWORD, DESERT_SHIELD;
    public static Item SCROLL_FLAME, SCROLL_WIND, SCROLL_SHADOW, SCROLL_WARD;
    public static Item DESERT_HELMET, DESERT_CHESTPLATE, DESERT_LEGGINGS, DESERT_BOOTS;
    public static final RegistryEntry<StatusEffect> POSSESSION = Registry.registerReference(
            Registries.STATUS_EFFECT, id("possession"), new StatusEffect(StatusEffectCategory.HARMFUL, 0x7051b3) {});

    public static Identifier id(String path) { return Identifier.of(ID, path); }
    private static Item item(String name, Item item) {
        Registry.register(Registries.ITEM, id(name), item);
        ITEMS.add(item);
        return item;
    }

    @Override public void onInitialize() {
        SPEAR = item("desert_spear", new SwordItem(ToolMaterials.IRON, new Item.Settings()
                .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 4, -2.8f))));
        FLAME_SWORD = item("djinn_flame_sword", new FlameSword(new Item.Settings()
                .fireproof().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 3, -2.4f))));
        ROYAL_SWORD = item("royal_sword", new FlameSword(new Item.Settings()
                .fireproof().rarity(net.minecraft.util.Rarity.EPIC)
                .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 6, -2.6f))));
        DESERT_SHIELD = item("desert_shield", new ShieldItem(new Item.Settings().maxDamage(480)));
        SCROLL_FLAME = item("occult_scroll_flame", new OccultScroll(OccultScroll.Kind.FLAME));
        SCROLL_WIND = item("occult_scroll_wind", new OccultScroll(OccultScroll.Kind.WIND));
        SCROLL_SHADOW = item("occult_scroll_shadow", new OccultScroll(OccultScroll.Kind.SHADOW));
        SCROLL_WARD = item("occult_scroll_ward", new OccultScroll(OccultScroll.Kind.WARD));
        RegistryEntry<ArmorMaterial> material = Registry.registerReference(Registries.ARMOR_MATERIAL, id("desert"),
                new ArmorMaterial(Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 6,
                        ArmorItem.Type.LEGGINGS, 5, ArmorItem.Type.BOOTS, 2, ArmorItem.Type.BODY, 6),
                        20, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, () -> Ingredient.ofItems(Items.GOLD_INGOT),
                        List.of(new ArmorMaterial.Layer(id("desert"))), 1.0f, 0.0f));
        DESERT_HELMET = item("desert_helmet", new ArmorItem(material, ArmorItem.Type.HELMET, new Item.Settings().maxDamage(220)));
        DESERT_CHESTPLATE = item("desert_chestplate", new ArmorItem(material, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(320)));
        DESERT_LEGGINGS = item("desert_leggings", new ArmorItem(material, ArmorItem.Type.LEGGINGS, new Item.Settings().maxDamage(300)));
        DESERT_BOOTS = item("desert_boots", new ArmorItem(material, ArmorItem.Type.BOOTS, new Item.Settings().maxDamage(260)));
        for (int color = 0; color < 4; color++) {
            for (int role = 0; role < 4; role++) {
                String name = COLORS[color] + "_" + ROLES[role] + "_djinn";
                EntityType<DjinnEntity> type = registerEntity(name, color, role, false);
                TYPES.put(name, type);
                int base = new int[]{0x344c83, 0x9c302c, 0xdde8ed, 0x272338}[color];
                item(name + "_spawn_egg", new SpawnEggItem(type, base, 0xd9ad52, new Item.Settings()));
                final int c = color, r = role;
                SpawnRestriction.register(type, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                        (entityType, world, reason, pos, random) -> world.getBlockState(pos.down()).isOf(Blocks.SAND)
                                && world.getLightLevel(pos) > 8 && MobEntity.canMobSpawn(entityType, world, reason, pos, random));
                BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.DESERT), SpawnGroup.CREATURE,
                        type, (c == 0 ? 6 : 2) * (r == 0 ? 2 : 1), 1, r == 2 ? 3 : 2);
            }
        }
        KING = registerEntity("djinn_king", 0, 2, true);
        TYPES.put("djinn_king", KING);
        item("djinn_king_spawn_egg", new SpawnEggItem(KING, 0x193946, 0x19dfd6, new Item.Settings()));
        Registry.register(Registries.ITEM_GROUP, id("djinns"), FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.djinns"))
                .icon(() -> new ItemStack(ROYAL_SWORD))
                .entries((context, entries) -> ITEMS.forEach(entries::add)).build());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) tickPossession(player);
        });
    }

    private static EntityType<DjinnEntity> registerEntity(String name, int color, int role, boolean king) {
        EntityType<DjinnEntity> type = Registry.register(Registries.ENTITY_TYPE, id(name),
                EntityType.Builder.<DjinnEntity>create((t, w) -> new DjinnEntity(t, w, color, role, king), SpawnGroup.CREATURE)
                        .dimensions(king ? 1.4f : 0.7f, king ? 5.0f : 2.25f)
                        .maxTrackingRange(10).build(ID + ":" + name));
        DefaultAttributeContainer.Builder attributes = DjinnEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, king ? 300 : role == 2 ? 48 : role == 3 ? 36 : 28)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, king ? 0.32 : 0.28)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, king ? 0.36 : 0.30)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, king ? 1 : role == 2 ? 6 : 4)
                .add(EntityAttributes.GENERIC_ARMOR, king ? 12 : role == 2 ? 7 : 2)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, king ? 0.8 : 0.25);
        FabricDefaultAttributeRegistry.register(type, attributes);
        return type;
    }

    public static void possess(ServerPlayerEntity player) {
        if (player.isCreative() || player.isSpectator() || player.hasStatusEffect(POSSESSION)) return;
        player.addStatusEffect(new StatusEffectInstance(POSSESSION, 100, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
        player.sendMessage(Text.translatable("message.djinns.possessed"), true);
    }

    private static void tickPossession(ServerPlayerEntity player) {
        if (!player.hasStatusEffect(POSSESSION) || !player.isAlive() || player.isCreative() || player.isSpectator()) return;
        // Client input is overridden too. The server remains authoritative for involuntary attacks.
        if (player.age % 12 == 0) {
            List<LivingEntity> victims = player.getWorld().getEntitiesByClass(LivingEntity.class,
                    player.getBoundingBox().expand(2.8), e -> e != player && e.isAlive()
                            && player.squaredDistanceTo(e) <= 2.8 * 2.8
                            && !(e instanceof net.minecraft.entity.player.PlayerEntity) && player.canSee(e));
            if (!victims.isEmpty()) {
                LivingEntity victim = victims.get(player.getRandom().nextInt(victims.size()));
                player.attack(victim);
                player.swingHand(net.minecraft.util.Hand.MAIN_HAND, true);
            }
        }
    }
}
