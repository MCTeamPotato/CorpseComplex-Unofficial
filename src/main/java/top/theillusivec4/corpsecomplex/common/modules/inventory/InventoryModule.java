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

package top.theillusivec4.corpsecomplex.common.modules.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.corpsecomplex.CorpseComplex;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.IInventory;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.VanillaInventory;

@Mod.EventBusSubscriber(modid = CorpseComplex.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InventoryModule {

  public static final List<IInventory> STORAGE = new ArrayList<>();

  static {
    STORAGE.add(new VanillaInventory());
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void playerDrops(final LivingDropsEvent evt) {

    if (!(evt.getEntity() instanceof Player)) {
      return;
    }
    Player playerEntity = (Player) evt.getEntity();
    DeathStorageCapability.getCapability(playerEntity).ifPresent(deathStorage -> {
      int despawnTime = deathStorage.getSettings().getInventorySettings().getDropDespawnTime();

      if (despawnTime < 1) {
        evt.getDrops().forEach(ItemEntity::setExtendedLifetime);
      } else {
        evt.getDrops().forEach(itemEntity -> itemEntity.lifespan = despawnTime * 20);
      }
    });
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public static void playerDeath(final LivingDeathEvent evt) {

    if (!(evt.getEntity() instanceof Player)) {
      return;
    }
    Player playerEntity = (Player) evt.getEntity();
    Level world = playerEntity.getCommandSenderWorld();

    if (!world.isClientSide && !world.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
      DeathStorageCapability.getCapability(playerEntity).ifPresent(
          deathStorage -> STORAGE.forEach(storage -> storage.storeInventory(deathStorage)));
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
  public static void canceledDeath(final LivingDeathEvent evt) {

    if (!(evt.getEntity() instanceof Player) || !evt.isCanceled()) {
      return;
    }
    Player playerEntity = (Player) evt.getEntity();
    Level world = playerEntity.getCommandSenderWorld();

    if (!world.isClientSide && !world.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
      DeathStorageCapability.getCapability(playerEntity).ifPresent(deathStorage -> {
        STORAGE.forEach(storage -> storage.retrieveInventory(deathStorage, deathStorage));
        deathStorage.clearDeathInventory();
      });
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void playerRespawn(final PlayerEvent.Clone evt) {

    if (evt.isWasDeath()) {
      Player original = evt.getOriginal();
      original.revive();
      DeathStorageCapability.getCapability((Player) evt.getEntity()).ifPresent(
          newStorage -> DeathStorageCapability.getCapability(evt.getOriginal()).ifPresent(
              oldStorage -> STORAGE
                  .forEach(storage -> storage.retrieveInventory(newStorage, oldStorage))));
      original.remove(Entity.RemovalReason.KILLED);
    }
  }
}
