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

package top.theillusivec4.corpsecomplex.common.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.corpsecomplex.CorpseComplex;
import top.theillusivec4.corpsecomplex.common.modules.enchantment.SoulbindingEnchantment;
import top.theillusivec4.corpsecomplex.common.modules.item.ScrollItem;
import top.theillusivec4.corpsecomplex.common.modules.mementomori.MementoMoriEffect;
@SuppressWarnings("unused")
public class CorpseComplexRegistry {

  public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CorpseComplex.MODID);

  public static final RegistryObject<MobEffect> MEMENTO_MORI = MOB_EFFECTS.register("memento_mori", MementoMoriEffect::new);

  public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, CorpseComplex.MODID);

  public static final RegistryObject<Enchantment> SOULBINDING = ENCHANTMENTS.register("soulbinding", SoulbindingEnchantment::new);

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CorpseComplex.MODID);

  public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", ScrollItem::new);

  public static void register(IEventBus eventBus){
    ITEMS.register(eventBus);
    ENCHANTMENTS.register(eventBus);
    MOB_EFFECTS.register(eventBus);
  }
}
