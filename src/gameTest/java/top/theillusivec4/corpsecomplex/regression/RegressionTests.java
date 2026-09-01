package top.theillusivec4.corpsecomplex.regression;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.corpsecomplex.common.CommonEventHandler;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.modules.effects.EffectsModule;
import top.theillusivec4.corpsecomplex.common.modules.experience.ExperienceModule;
import top.theillusivec4.corpsecomplex.common.modules.hunger.HungerModule;
import top.theillusivec4.corpsecomplex.common.modules.inventory.InventoryModule;
import top.theillusivec4.corpsecomplex.common.modules.inventory.InventorySetting;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.VanillaInventory;
import top.theillusivec4.corpsecomplex.common.registry.CorpseComplexRegistry;
import top.theillusivec4.corpsecomplex.common.util.DeathInfo;
import top.theillusivec4.corpsecomplex.common.util.Enums.DropMode;
import top.theillusivec4.corpsecomplex.common.util.InventoryHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mod("corpsecomplex_tests")
@GameTestHolder("corpsecomplex")
@PrefixGameTestTemplate(false)
public class RegressionTests {
    private static FakePlayer player(ServerLevel level) {
        return new FakePlayer(level, new GameProfile(UUID.randomUUID(), "Regression"));
    }

    private static DeathStorageCapability.IDeathStorage storage(FakePlayer player) {
        return DeathStorageCapability.getCapability(player).orElseThrow(() -> new AssertionError("Missing death storage"));
    }

    @GameTest(template = "empty")
    public static void ordinaryEnchantmentDoesNotKeepItem(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 3);
        helper.assertTrue(InventoryHelper.getDropModeOverride(sword, new InventorySetting()) != DropMode.KEEP,
                "Sharpness alone must not protect an item from death drops");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void soulbindingKeepsItem(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(CorpseComplexRegistry.SOULBINDING.get(), 1);
        helper.assertTrue(InventoryHelper.getDropModeOverride(sword, new InventorySetting()) == DropMode.KEEP,
                "Soulbinding must protect its own item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void scrollReturnsToDeathDimension(GameTestHelper helper) {
        FakePlayer player = player(helper.getLevel());
        BlockPos deathPos = new BlockPos(10, 80, 10);
        player.setLastDeathLocation(Optional.of(GlobalPos.of(Level.NETHER, deathPos)));
        helper.assertTrue(helper.getLevel().getServer().getLevel(Level.NETHER) != null, "Nether fixture is missing");
        ItemStack scroll = new ItemStack(CorpseComplexRegistry.SCROLL.get(), 2);
        scroll.getItem().finishUsingItem(scroll, helper.getLevel(), player);
        helper.assertTrue(player.level().dimension() == Level.NETHER, "Scroll must change to the death dimension");
        helper.assertTrue(player.blockPosition().equals(deathPos), "Scroll must use death coordinates");
        helper.assertTrue(scroll.getCount() == 1, "Successful survival teleport must consume exactly one scroll");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unavailableDimensionDoesNotConsumeScroll(GameTestHelper helper) {
        FakePlayer player = player(helper.getLevel());
        ResourceKey<Level> missing = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("corpsecomplex_tests", "missing"));
        player.setLastDeathLocation(Optional.of(GlobalPos.of(missing, new BlockPos(10, 80, 10))));
        ItemStack scroll = new ItemStack(CorpseComplexRegistry.SCROLL.get(), 2);
        scroll.getItem().finishUsingItem(scroll, helper.getLevel(), player);
        helper.assertTrue(scroll.getCount() == 2, "An unavailable dimension must not consume a scroll");
        helper.assertFalse(player.getCooldowns().isOnCooldown(scroll.getItem()), "A failed teleport must not start cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cloneCopiesDeathInfoToNewPlayer(GameTestHelper helper) {
        FakePlayer oldPlayer = player(helper.getLevel());
        FakePlayer newPlayer = player(helper.getLevel());
        DeathInfo info = new DeathInfo(oldPlayer.damageSources().fall(), helper.getLevel(), List.of());
        storage(oldPlayer).setDeathDamageSource(info);
        CommonEventHandler.playerClone(new PlayerEvent.Clone(newPlayer, oldPlayer, true));
        helper.assertTrue(storage(newPlayer).getDeathInfo() == info, "Death context must reach the respawned player");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cloneRestoresExperienceToNewPlayer(GameTestHelper helper) {
        FakePlayer oldPlayer = player(helper.getLevel());
        FakePlayer newPlayer = player(helper.getLevel());
        storage(oldPlayer).getSettings().getExperienceSettings().setLostXp(0);
        oldPlayer.experienceLevel = 12;
        oldPlayer.experienceProgress = 0.25F;
        oldPlayer.totalExperience = 230;
        ExperienceModule.playerRespawn(new PlayerEvent.Clone(newPlayer, oldPlayer, true));
        helper.assertTrue(newPlayer.experienceLevel == 12 && newPlayer.experienceProgress == 0.25F
                && newPlayer.totalExperience == 230, "Kept experience must reach the respawned player");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cloneRestoresHungerToNewPlayer(GameTestHelper helper) {
        FakePlayer oldPlayer = player(helper.getLevel());
        FakePlayer newPlayer = player(helper.getLevel());
        var setting = storage(oldPlayer).getSettings().getHungerSettings();
        setting.setKeepFood(true);
        setting.setMinFood(0);
        setting.setMaxFood(20);
        oldPlayer.getFoodData().setFoodLevel(7);
        HungerModule.playerRespawn(new PlayerEvent.Clone(newPlayer, oldPlayer, true));
        helper.assertTrue(newPlayer.getFoodData().getFoodLevel() == 7, "Kept hunger must reach the respawned player");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cloneCopiesEffectsToNewPlayer(GameTestHelper helper) {
        FakePlayer oldPlayer = player(helper.getLevel());
        FakePlayer newPlayer = player(helper.getLevel());
        storage(oldPlayer).addEffectInstance(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        EffectsModule.playerClone(new PlayerEvent.Clone(newPlayer, oldPlayer, true));
        helper.assertTrue(storage(newPlayer).getEffects().size() == 1, "Kept effects must reach the respawned player");
        helper.assertTrue(storage(oldPlayer).getEffects().size() == 1, "Cloning must not append effects to the old player");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void keepProbabilityPreservesTotalItemCount(GameTestHelper helper) {
        for (double chance : new double[] {0, 0.9, 1}) {
            FakePlayer oldPlayer = player(helper.getLevel());
            FakePlayer newPlayer = player(helper.getLevel());
            var oldStorage = storage(oldPlayer);
            oldStorage.getSettings().getInventorySettings().getInventorySettings().values().forEach(section -> {
                section.keepChance = chance;
                section.destroyChance = 0;
                section.keepDurabilityLoss = 0;
                section.dropDurabilityLoss = 0;
            });
            oldPlayer.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 64));
            InventoryHelper.RAND.setSeed(0);
            new VanillaInventory().storeInventory(oldStorage);
            int dropped = oldPlayer.getInventory().getItem(0).getCount();
            InventoryModule.playerRespawn(new PlayerEvent.Clone(newPlayer, oldPlayer, true));
            int kept = newPlayer.getInventory().getItem(0).getCount();
            helper.assertTrue(kept + dropped == 64, "Keep chance " + chance + " must not destroy items");
            if (chance == 1) helper.assertTrue(kept == 64, "Keep chance 1 must restore all items");
            if (chance == 0) helper.assertTrue(kept == 0, "Keep chance 0 must leave all items to drop");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void removedPlayerRestoresStateThroughCloneEvent(GameTestHelper helper) {
        FakePlayer oldPlayer = player(helper.getLevel());
        FakePlayer newPlayer = player(helper.getLevel());
        var oldStorage = storage(oldPlayer);
        DeathInfo info = new DeathInfo(oldPlayer.damageSources().fall(), helper.getLevel(), List.of());
        oldStorage.setDeathDamageSource(info);
        oldStorage.getSettings().getExperienceSettings().setLostXp(0);
        oldPlayer.experienceLevel = 12;
        oldPlayer.experienceProgress = 0.25F;
        oldPlayer.totalExperience = 230;
        oldStorage.getSettings().getHungerSettings().setKeepFood(true);
        oldStorage.getSettings().getHungerSettings().setMinFood(0);
        oldStorage.getSettings().getHungerSettings().setMaxFood(20);
        oldPlayer.getFoodData().setFoodLevel(7);
        oldStorage.addEffectInstance(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        oldStorage.getSettings().getInventorySettings().getInventorySettings().values().forEach(section -> {
            section.keepChance = 1;
            section.destroyChance = 0;
        });
        oldPlayer.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 64));
        new VanillaInventory().storeInventory(oldStorage);
        oldPlayer.remove(Entity.RemovalReason.KILLED);
        PlayerEvent.Clone event = new PlayerEvent.Clone(newPlayer, oldPlayer, true);
        CommonEventHandler.playerClone(event);
        helper.assertTrue(oldPlayer.isRemoved(), "Corpse Complex must not revive the dead player entity");
        helper.assertFalse(DeathStorageCapability.getCapability(oldPlayer).isPresent(), "Temporary capabilities must be invalidated again");
        // Curios also handles this event and revives the original entity in version 5.7.0.
        // Check our lifecycle handling above, then verify data restoration with every listener active.
        MinecraftForge.EVENT_BUS.post(event);
        helper.assertTrue(storage(newPlayer).getDeathInfo() == info, "Clone event must preserve death context");
        helper.assertTrue(newPlayer.experienceLevel == 12, "Clone event must restore experience");
        helper.assertTrue(newPlayer.getFoodData().getFoodLevel() == 7, "Clone event must restore hunger");
        helper.assertTrue(storage(newPlayer).getEffects().size() == 1, "Clone event must copy effects once");
        helper.assertTrue(newPlayer.getInventory().getItem(0).getCount() == 64, "Clone event must restore inventory");
        helper.succeed();
    }
}
