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

package top.theillusivec4.corpsecomplex.common.modules.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.corpsecomplex.CorpseComplex;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.util.Enums.PermissionMode;

import java.util.List;

@Mod.EventBusSubscriber(modid = CorpseComplex.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectsModule {

  @SubscribeEvent
  public static void finishItemUse(LivingEntityUseItemEvent.Finish evt) {
    Entity entity = evt.getEntity();

    if (!entity.getCommandSenderWorld().isClientSide && entity instanceof Player player) {
      DeathStorageCapability.getCapability(player).ifPresent(
          deathStorage -> deathStorage.getSettings().getEffectsSettings().getCures()
              .forEach(itemStack -> {
                if (ItemStack.isSameItemSameTags(evt.getItem(), itemStack)) {
                    player.curePotionEffects(evt.getItem());
                }
              }));
    }
  }

  @SubscribeEvent
  public static void playerDeath(final LivingDeathEvent evt) {

    if (!(evt.getEntity() instanceof Player)) {
      return;
    }
    Player playerEntity = (Player) evt.getEntity();
    Level world = playerEntity.getCommandSenderWorld();

    if (!world.isClientSide) {
      DeathStorageCapability.getCapability(playerEntity).ifPresent(
          deathStorage -> playerEntity.getActiveEffects().forEach(effectInstance -> {
            boolean flag;
            EffectsSetting setting = deathStorage.getSettings().getEffectsSettings();
            List<MobEffect> keepEffects = setting.getKeepEffects();

            if (setting.getKeepEffectsMode() == PermissionMode.BLACKLIST) {
              flag = !keepEffects.contains(effectInstance.getEffect());
            } else {
              flag = keepEffects.contains(effectInstance.getEffect());
            }

            if (flag) {
              deathStorage.addEffectInstance(effectInstance);
            }
          }));
    }
  }

  @SubscribeEvent
  public static void playerClone(final PlayerEvent.Clone evt) {

    if (evt.isWasDeath()) {
      DeathStorageCapability.getCapability(evt.getOriginal()).ifPresent(
          deathStorage -> DeathStorageCapability.getCapability(evt.getOriginal()).ifPresent(
              oldDeathStorage -> oldDeathStorage.getEffects()
                  .forEach(deathStorage::addEffectInstance)));
    }
  }

  @SubscribeEvent
  public static void playerRespawn(final PlayerRespawnEvent evt) {

    if (!evt.isEndConquered()) {
      Player player = (Player) evt.getEntity();
      DeathStorageCapability.getCapability(player).ifPresent(deathStorage -> {
        deathStorage.getEffects().forEach(player::addEffect);
        deathStorage.clearEffects();
        deathStorage.getSettings().getEffectsSettings().getEffects().forEach(effectInstance -> {
            MobEffectInstance newEffect = new MobEffectInstance(effectInstance.getEffect(),
              effectInstance.getDuration(), effectInstance.getAmplifier());
          newEffect.setCurativeItems(effectInstance.getCurativeItems());
          player.addEffect(newEffect);
        });
      });
    }
  }
}
