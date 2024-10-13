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

package top.theillusivec4.corpsecomplex.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.corpsecomplex.CorpseComplex;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability.Provider;
import top.theillusivec4.corpsecomplex.common.util.DeathInfo;

@Mod.EventBusSubscriber(modid = CorpseComplex.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {
  @SubscribeEvent
  public static void attachCapability(final AttachCapabilitiesEvent<Entity> evt) {
    if (evt.getObject() instanceof Player) {
      evt.addCapability(DeathStorageCapability.ID, new Provider((Player) evt.getObject()));
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void playerDeath(final LivingDeathEvent evt) {

    if (!(evt.getEntity() instanceof Player)) {
      return;
    }
    Player playerEntity = (Player) evt.getEntity();
    Level world = playerEntity.level;

    if (!world.isClientSide() && evt.getEntity() instanceof ServerPlayer serverPlayer) {
      BlockPos lastDeathLocation = serverPlayer.blockPosition();
      List<Integer> list = List.of(lastDeathLocation.getX(), lastDeathLocation.getY(), lastDeathLocation.getZ());
      serverPlayer.getPersistentData().putIntArray("lastDeathLocation", list);

      DeathStorageCapability.getCapability(serverPlayer).ifPresent(deathStorage -> {
        deathStorage
            .setDeathDamageSource(new DeathInfo(evt.getSource(), world, new ArrayList<>()));
        deathStorage.buildSettings();
      });
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void playerClone(final PlayerEvent.Clone evt) {

    if (evt.isWasDeath()) {
      DeathStorageCapability.getCapability(evt.getOriginal()).ifPresent(
          deathStorage -> DeathStorageCapability.getCapability(evt.getOriginal()).ifPresent(
              oldDeathStorage -> deathStorage
                  .setDeathDamageSource(oldDeathStorage.getDeathInfo())));
      if (evt.getEntity() instanceof ServerPlayer serverPlayer){
        int[] list = evt.getOriginal().getPersistentData().getIntArray("lastDeathLocation").clone();
        serverPlayer.getPersistentData().putIntArray("lastDeathLocation", list);
      }
    }
  }
}
