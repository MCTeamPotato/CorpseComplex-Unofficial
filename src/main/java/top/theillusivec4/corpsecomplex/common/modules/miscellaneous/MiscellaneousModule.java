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

package top.theillusivec4.corpsecomplex.common.modules.miscellaneous;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import top.theillusivec4.corpsecomplex.CorpseComplex;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.config.CorpseComplexConfig;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = CorpseComplex.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MiscellaneousModule {

  @SubscribeEvent
  public static void setSpawn(final PlayerSetSpawnEvent evt) {

    if (!evt.getEntity().getCommandSenderWorld().isClientSide) {
      DeathStorageCapability.getCapability(evt.getEntity()).ifPresent(deathStorage -> {
        if (deathStorage.getSettings().getMiscellaneousSettings().isRestrictRespawning()) {
          evt.setCanceled(true);
        }
      });
    }
  }

  @SubscribeEvent
  public static void playerRespawn(final PlayerRespawnEvent evt) {
    Player player = evt.getEntity();

    DeathStorageCapability.getCapability(player).ifPresent(
        deathStorage -> deathStorage.getSettings().getMiscellaneousSettings().getRespawnItems()
            .forEach(item -> ItemHandlerHelper.giveItemToPlayer(evt.getEntity(), item.copy())));

    if (CorpseComplexConfig.respawnHealth > 0) {
      player.setHealth((float) CorpseComplexConfig.respawnHealth);
    }
  }

  @SubscribeEvent
  public static void playerDeath(final LivingDeathEvent evt) {

    if (!(evt.getEntity() instanceof Player)) {
      return;
    }
    Player playerEntity = (Player) evt.getEntity();
    Level world = playerEntity.level;

    if (!world.isClientSide) {
      DeathStorageCapability.getCapability(playerEntity).ifPresent(
          deathStorage -> deathStorage.getSettings().getMiscellaneousSettings()
              .getMobSpawnsOnDeath()
              .forEach(mob -> spawnMob(mob, BlockPos.containing(playerEntity.getPosition(1f)), world)));
    }
  }

  private static void spawnMob(EntityType<?> type, BlockPos blockPos, Level world) {
    double d0 =
        (double) blockPos.getX() + (world.random.nextDouble() - world.random.nextDouble()) * 4 + 0.5D;
    double d1 = (blockPos.getY() + world.random.nextInt(3) - 1);
    double d2 =
        (double) blockPos.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * 4 + 0.5D;

    if (world.noCollision(type.getAABB(d0, d1, d2))) {
      CompoundTag compoundnbt = new CompoundTag();
      compoundnbt.putString("id", Objects.requireNonNull(type.getCategory().getName()));
        Entity entity = EntityType.loadEntityRecursive(compoundnbt, world, (entity1) -> {
        entity1.moveTo(d0, d1, d2, entity1.yRotO, entity1.xRotO);
        return entity1;
      });

      if (entity != null) {
        entity.moveTo(entity.getX(), entity.getY(), entity.getZ(),
            world.random.nextFloat() * 360.0F, 0.0F);

        if (entity instanceof Mob && world instanceof ServerLevel) {
          ((Mob) entity).finalizeSpawn((ServerLevel) world,
              world.getCurrentDifficultyAt(BlockPos.containing(entity.getPosition(1))), MobSpawnType.TRIGGERED, null,
              null);
        }
        addEntity(entity, world);
        world.levelEvent(2004, blockPos, 0);

        if (entity instanceof Mob) {
          ((Mob) entity).spawnAnim();
          ((Mob) entity).setPersistenceRequired();
        }
      }
    }
  }

  private static void addEntity(Entity entity, Level world) {

    if (world.addFreshEntity(entity)) {

      for (Entity entity1 : entity.getPassengers()) {
        addEntity(entity1, world);
      }
    }
  }
}
