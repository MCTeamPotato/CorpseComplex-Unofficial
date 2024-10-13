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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability.IDeathStorage;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.IInventory;
import top.theillusivec4.corpsecomplex.common.util.Enums.InventorySection;
import top.theillusivec4.corpsecomplex.common.util.InventoryHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.UUID;
public class CuriosIInventory implements IInventory {

  @Override
  public void storeInventory(IDeathStorage deathStorage) {
    Player player = deathStorage.getPlayer();
    ListTag list = new ListTag();
    CuriosApi.getCuriosHelper().getCuriosHandler(player)
        .ifPresent(curioHandler -> curioHandler.getCurios().forEach((id, stackHandler) -> {
          ListTag list1 = new ListTag();
          ListTag list2 = new ListTag();

          for (int i = 0; i < stackHandler.getSlots(); i++) {
            InventoryHelper.process((Player) curioHandler.getWearer(),
                stackHandler.getStacks().getStackInSlot(i), i, list1, InventorySection.CURIOS,
                deathStorage.getSettings().getInventorySettings());
            InventoryHelper.process((Player) curioHandler.getWearer(),
                stackHandler.getCosmeticStacks().getStackInSlot(i), i, list2,
                InventorySection.CURIOS, deathStorage.getSettings().getInventorySettings());
          }
          CompoundTag tag = new CompoundTag();
          tag.putString("Identifier", id);
          tag.put("Stacks", list1);
          tag.put("CosmeticStacks", list2);
          ListTag modifiers = new ListTag();

          for (Map.Entry<UUID, AttributeModifier> entry : stackHandler.getModifiers().entrySet()) {
            CompoundTag mod = new CompoundTag();
            mod.put("Modifier", entry.getValue().save());
            modifiers.add(mod);
          }
          tag.put("Modifiers", modifiers);
          list.add(tag);
        }));
    deathStorage.addInventory("curios", list);
  }

  @Override
  public void retrieveInventory(IDeathStorage newStorage, IDeathStorage oldStorage) {
    Player player = newStorage.getPlayer();
    Player oldPlayer = oldStorage.getPlayer();

    if (player != null && oldPlayer != null) {
      ListTag list = (ListTag) oldStorage.getInventory("curios");

      if (list != null) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(newHandler -> {

          for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            String id = tag.getString("Identifier");
            newHandler.getStacksHandler(id).ifPresent(stacksHandler -> {
              ListTag modifiers = tag.getList("Modifiers", Tag.TAG_COMPOUND);

              for (int i1 = 0; i1 < modifiers.size(); i1++) {
                CompoundTag mod = modifiers.getCompound(i1);
                AttributeModifier attributeModifier =
                    AttributeModifier.load(mod.getCompound("Modifier"));
                stacksHandler.getCachedModifiers().add(attributeModifier);
                stacksHandler.addTransientModifier(attributeModifier);
              }
              stacksHandler.update();
              ListTag stacks = tag.getList("Stacks", Tag.TAG_COMPOUND);

              for (int j = 0; j < stacks.size(); j++) {
                CompoundTag CompoundTag = stacks.getCompound(j);
                int slot = CompoundTag.getInt("Slot");
                ItemStack itemstack = ItemStack.of(CompoundTag);

                if (!itemstack.isEmpty()) {
                  IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                  if (stackHandler.getSlots() > slot &&
                      stackHandler.getStackInSlot(slot).isEmpty()) {
                    stacksHandler.getStacks().setStackInSlot(slot, itemstack);
                    CuriosApi.getCuriosHelper().getCurio(itemstack).ifPresent((curio) -> {
                      SlotContext slotContext = new SlotContext(id, player, slot, false, true);
                      player.getAttributes()
                          .addTransientAttributeModifiers(curio.getAttributeModifiers(slotContext, player.getUUID()));
                      curio.onEquip(slotContext, curio.getStack());
                    });
                  } else {
                    ItemHandlerHelper.giveItemToPlayer(player, itemstack);
                  }
                }
              }
              ListTag cosmeticStacks = tag.getList("CosmeticStacks", Tag.TAG_COMPOUND);

              for (int j = 0; j < cosmeticStacks.size(); j++) {
                CompoundTag CompoundTag = stacks.getCompound(j);
                int slot = CompoundTag.getInt("Slot");
                ItemStack itemstack = ItemStack.of(CompoundTag);

                if (!itemstack.isEmpty()) {
                  IDynamicStackHandler stackHandler = stacksHandler.getCosmeticStacks();

                  if (stackHandler.getSlots() > slot &&
                      stackHandler.getStackInSlot(slot).isEmpty()) {
                    stacksHandler.getCosmeticStacks().setStackInSlot(slot, itemstack);
                  } else {
                    ItemHandlerHelper.giveItemToPlayer(player, itemstack);
                  }
                }
              }
            });
          }
        });
      }
    }
  }
}
