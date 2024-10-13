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

package top.theillusivec4.corpsecomplex.common.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.corpsecomplex.common.util.Enums;
import top.theillusivec4.corpsecomplex.common.util.Enums.DropMode;

public class ConfigParser {

  public static List<EntityType<?>> parseMobs(@Nonnull List<? extends String> configList) {
    List<EntityType<?>> list = new ArrayList<>();
    configList.forEach(mob -> {
      EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mob));

      if (entity != null) {
        list.add(entity);
      }
    });
    return list;
  }

  public static Map<Item, Integer> parseItems(@Nonnull List<? extends String> configList) {
    Map<Item, Integer> map = new HashMap<>();
    configList.forEach(item -> {
      String[] parsed = item.split(";");
      Item item1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parsed[0]));

      if (item1 != null) {
        int amount = parsed.length > 1 ? Integer.parseInt(parsed[1]) : 1;
        map.put(item1, amount);
      }
    });
    return map;
  }

  public static Map<Item, DropMode> parseDrops(@Nonnull List<? extends String> configList) {
    Map<Item, DropMode> map = new HashMap<>();
    configList.forEach(string -> {
      String[] parsed = string.split(";");
      Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parsed[0]));

      if (item != null) {
        Enums.DropMode dropMode = Enums.DropMode.DROP;

        if (parsed.length > 1) {
          String setting = parsed[1];

          if (!setting.equals("drop")) {

            if (setting.equals("keep")) {
              dropMode = Enums.DropMode.KEEP;
            } else if (setting.equals("destroy")) {
              dropMode = Enums.DropMode.DESTROY;
            }
          }
        }
        map.put(item, dropMode);
      }
    });
    return map;
  }

  public static List<MobEffect> parseEffects(@Nonnull List<? extends String> configList) {
    List<MobEffect> list = new ArrayList<>();
    configList.forEach(effect -> {
      MobEffect effect1 = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect));

      if (effect1 != null) {
        list.add(effect1);
      }
    });
    return list;
  }

  public static List<MobEffectInstance> parseMobEffectInstances(
      @Nonnull List<? extends String> configList, @Nonnull List<ItemStack> cures) {
    List<MobEffectInstance> list = new ArrayList<>();
    configList.forEach(instance -> {
      String[] parse = instance.split(";");

      if (parse.length >= 2) {
        MobEffect effect1 = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(parse[0]));

        if (effect1 != null) {
          int amplifier = parse.length >= 3 ? Integer.parseInt(parse[2]) : 0;
          MobEffectInstance instance1 = new MobEffectInstance(effect1, Integer.parseInt(parse[1]) * 20,
              amplifier);

          if (parse.length >= 4) {
            instance1.setCurativeItems(new ArrayList<>());
          } else {
            instance1.setCurativeItems(cures);
          }
          list.add(instance1);
        }
      }
    });
    return list;
  }
}
