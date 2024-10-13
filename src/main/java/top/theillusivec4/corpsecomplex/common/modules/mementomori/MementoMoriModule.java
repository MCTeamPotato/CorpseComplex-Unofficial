/*
 * Copyright (c) 2017-2020 C4
 *
 * This file is part of Corpse Complex, a mod made for Minecraft.
 *
 * Corpse Complex is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Corpse Complex is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Corpse Complex.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.corpsecomplex.common.modules.mementomori;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.corpsecomplex.CorpseComplex;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.config.CorpseComplexConfig;
import top.theillusivec4.corpsecomplex.common.modules.mementomori.MementoMoriEffect.AttributeInfo;
import top.theillusivec4.corpsecomplex.common.registry.CorpseComplexRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CorpseComplex.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MementoMoriModule {

  private static final List<ItemStack> CURES = new ArrayList<>();

  @SubscribeEvent
  public static void serverStart(final FMLCommonSetupEvent evt) {

    if (CorpseComplexConfig.SERVER.healthMod.get() != 0) {
      MementoMoriEffect.ATTRIBUTES.put(Attributes.MAX_HEALTH,
          new AttributeInfo(CorpseComplexConfig.SERVER.healthMod.get(),
              UUID.fromString("ca572ca7-d11e-4054-b225-f4c797cdf69b"), Operation.ADDITION));
    }

    if (CorpseComplexConfig.SERVER.armorMod.get() != 0) {
      MementoMoriEffect.ATTRIBUTES.put(Attributes.ARMOR,
          new AttributeInfo(CorpseComplexConfig.SERVER.armorMod.get(),
              UUID.fromString("b3bd0150-1953-4971-a822-8445953c4195"), Operation.ADDITION));
    }

    if (CorpseComplexConfig.SERVER.toughnessMod.get() != 0) {
      MementoMoriEffect.ATTRIBUTES.put(Attributes.ARMOR_TOUGHNESS,
          new AttributeInfo(CorpseComplexConfig.SERVER.toughnessMod.get(),
              UUID.fromString("5113ef1e-5200-4d6a-a898-946f0e4b5d26"), Operation.ADDITION));
    }

    if (CorpseComplexConfig.SERVER.movementMod.get() != 0) {
      MementoMoriEffect.ATTRIBUTES.put(Attributes.MOVEMENT_SPEED,
          new AttributeInfo(CorpseComplexConfig.SERVER.movementMod.get(),
              UUID.fromString("f9a9495d-89b5-4676-8345-bc2e92936821"), Operation.MULTIPLY_TOTAL));
    }

    if (CorpseComplexConfig.SERVER.attackSpeedMod.get() != 0) {
      MementoMoriEffect.ATTRIBUTES.put(Attributes.ATTACK_SPEED,
          new AttributeInfo(CorpseComplexConfig.SERVER.attackSpeedMod.get(),
              UUID.fromString("9fe627b8-3477-4ccf-9587-87776259172f"), Operation.MULTIPLY_TOTAL));
    }

    if (CorpseComplexConfig.SERVER.damageMod.get() != 0) {
      MementoMoriEffect.ATTRIBUTES.put(Attributes.ATTACK_DAMAGE,
          new AttributeInfo(CorpseComplexConfig.SERVER.damageMod.get(),
              UUID.fromString("ae5b003a-65f3-41f2-b104-4c71a2261d5b"), Operation.ADDITION));
    }
    CorpseComplexConfig.SERVER.mementoCures.get().forEach(cure -> {
      Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(cure));

      if (item != null) {
        CURES.add(new ItemStack(item));
      }
    });
  }

  @SubscribeEvent
  public static void playerRespawn(final PlayerRespawnEvent evt) {
    Player playerEntity = (Player) evt.getEntity();

    if (!MementoMoriEffect.ATTRIBUTES.isEmpty() || CorpseComplexConfig.SERVER.noFood.get()
        || CorpseComplexConfig.SERVER.percentXp.get() != 0) {
      MobEffectInstance instance = new MobEffectInstance(CorpseComplexRegistry.MEMENTO_MORI.get(),
          CorpseComplexConfig.SERVER.duration.get() * 20);
      instance.setCurativeItems(CURES);
      playerEntity.addEffect(instance);

      if (playerEntity.getHealth() < playerEntity.getMaxHealth()) {
        playerEntity.setHealth(playerEntity.getMaxHealth());
      }
    }
  }

  @SubscribeEvent
  public static void eatingFood(final PlayerInteractEvent.RightClickItem evt) {
    DeathStorageCapability.getCapability((Player) evt.getEntity()).ifPresent(deathStorage -> {
      if (((Player) evt.getEntity()).hasEffect(CorpseComplexRegistry.MEMENTO_MORI.get()) && deathStorage
          .getSettings().getMementoMoriSettings().isNoFood()
          && evt.getItemStack().getUseAnimation() == UseAnim.EAT) {
        evt.setCanceled(true);
      }
    });
  }

  @SubscribeEvent
  public static void eatingCake(final PlayerInteractEvent.RightClickBlock evt) {
    DeathStorageCapability.getCapability(evt.getEntity()).ifPresent(deathStorage -> {
      if (evt.getEntity().hasEffect(CorpseComplexRegistry.MEMENTO_MORI.get()) && deathStorage
          .getSettings().getMementoMoriSettings().isNoFood() && evt.getLevel()
          .getBlockState(evt.getPos()).getBlock() instanceof CakeBlock) {
        evt.setCanceled(true);
      }
    });
  }

  @SubscribeEvent
  public static void finishItemUse(LivingEntityUseItemEvent.Finish evt) {
    LivingEntity entity = evt.getEntity();

    if (!entity.getCommandSenderWorld().isClientSide && entity instanceof Player) {
      DeathStorageCapability.getCapability((Player) entity).ifPresent(
          deathStorage -> deathStorage.getSettings().getMementoMoriSettings().getMementoCures()
              .forEach(itemStack -> {
                if (ItemStack.isSameItemSameTags(evt.getItem(), itemStack)) {
                  entity.curePotionEffects(evt.getItem());
                }
              }));
    }
  }

  @SubscribeEvent
  public static void playerChangeXp(final PlayerXpEvent.XpChange evt) {
    DeathStorageCapability.getCapability(evt.getEntity()).ifPresent(deathStorage -> {
      Player playerEntity = evt.getEntity();
      MobEffectInstance effectInstance = playerEntity
          .getEffect(CorpseComplexRegistry.MEMENTO_MORI.get());

      if (effectInstance != null) {
        double percentXp = deathStorage.getSettings().getMementoMoriSettings().getPercentXp();

        if (percentXp != 0) {
          double modifier =
              CorpseComplexConfig.gradualRecovery ? (((float) effectInstance.getDuration())
                  / CorpseComplexConfig.duration) : 1.0D;
          percentXp *= modifier;
          evt.setAmount(Math.max(1, (int) (evt.getAmount() * (1 + percentXp))));
        }
      }
    });
  }
}
