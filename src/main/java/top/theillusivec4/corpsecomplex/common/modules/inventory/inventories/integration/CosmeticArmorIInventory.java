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

package top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.integration;

import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability.IDeathStorage;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.IInventory;
import top.theillusivec4.corpsecomplex.common.util.Enums.InventorySection;
import top.theillusivec4.corpsecomplex.common.util.InventoryHelper;

public class CosmeticArmorIInventory implements IInventory {

  @Override
  public void storeInventory(IDeathStorage deathStorage) {
    Player playerEntity = deathStorage.getPlayer();
    CAStacksBase stacks = CosArmorAPI.getCAStacks(playerEntity.getUUID());
    ListTag list = new ListTag();

    for (int i = 0; i < stacks.getSlots(); ++i) {
      ItemStack stack = stacks.getStackInSlot(i);
      if (!stack.isEmpty()) {
        InventoryHelper.process(playerEntity, stacks.getStackInSlot(i), i, list,
            InventorySection.COSMETIC_ARMOR, deathStorage.getSettings().getInventorySettings());
      }
    }
    deathStorage.addInventory("cosmeticarmorreworked", list);
  }

  @Override
  public void retrieveInventory(IDeathStorage newStorage, IDeathStorage oldStorage) {
    Player player = newStorage.getPlayer();
    Player oldPlayer = oldStorage.getPlayer();

    if (player != null && oldPlayer != null) {
      ListTag tagList = (ListTag) oldStorage.getInventory("cosmeticarmorreworked");

      if (tagList != null) {
        CAStacksBase stacks = CosArmorAPI.getCAStacks(player.getUUID());

        for (int i = 0; i < tagList.size(); ++i) {
          CompoundTag tag = tagList.getCompound(i);
          int slot = tag.getInt("Slot");
          ItemStack itemstack = ItemStack.of(tag);
          if (!itemstack.isEmpty()) {
            ItemStack existing = stacks.getStackInSlot(slot);

            if (existing.isEmpty()) {
              stacks.setStackInSlot(slot, itemstack);
            } else {
              ItemHandlerHelper.giveItemToPlayer(player, itemstack);
            }
          }
        }
      }
    }
  }
}
