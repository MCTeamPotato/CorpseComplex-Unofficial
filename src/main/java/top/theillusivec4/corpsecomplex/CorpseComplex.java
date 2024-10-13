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

package top.theillusivec4.corpsecomplex;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.config.CorpseComplexConfig;
import top.theillusivec4.corpsecomplex.common.registry.CorpseComplexRegistry;
import top.theillusivec4.corpsecomplex.common.util.integration.IntegrationManager;
import top.theillusivec4.corpsecomplex.common.util.manager.DeathConditionManager;
import top.theillusivec4.corpsecomplex.common.util.manager.DeathOverrideManager;
import top.theillusivec4.corpsecomplex.common.util.manager.ItemOverrideManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Mod(CorpseComplex.MODID)
public class CorpseComplex {

  public static final String MODID = "corpsecomplex";
  public static final Logger LOGGER = LogManager.getLogger();

  public CorpseComplex() {
    ModLoadingContext.get().registerConfig(Type.SERVER, CorpseComplexConfig.SERVER_SPEC);
    createServerConfig(CorpseComplexConfig.OVERRIDES_SPEC, "overrides");
    createServerConfig(CorpseComplexConfig.ITEM_OVERRIDES_SPEC, "itemoverrides");
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

    eventBus.addListener(this::setup);
    eventBus.addListener(this::config);
    eventBus.addListener(this::registerCapability);

    CorpseComplexRegistry.register(eventBus);
  }

  public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MODID + "." + MODID) {
    @Override
    public ItemStack makeIcon() {
      return new ItemStack(CorpseComplexRegistry.SCROLL.get());
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> pItems) {
      pItems.add(new ItemStack(CorpseComplexRegistry.SCROLL.get()));
    }
  };

  public void registerCapability(RegisterCapabilitiesEvent event) {
    event.register(DeathStorageCapability.class);
  }

  private void setup(final FMLCommonSetupEvent evt) {
    IntegrationManager.init();
  }

  private void config(final ModConfigEvent evt) {
    ModConfig modConfig = evt.getConfig();

    if (modConfig.getModId().equals(MODID)) {
      ForgeConfigSpec spec = (ForgeConfigSpec) modConfig.getSpec();

      if (modConfig.getType() == Type.SERVER) {
        CorpseComplexConfig.bakeConfigs();

        if (spec == CorpseComplexConfig.OVERRIDES_SPEC) {
          CorpseComplexConfig.transformOverrides(modConfig.getConfigData());
          DeathConditionManager.importConfig();
          DeathOverrideManager.importConfig();
        } else if (spec == CorpseComplexConfig.ITEM_OVERRIDES_SPEC) {
          CorpseComplexConfig.transformItemOverrides(modConfig.getConfigData());
          ItemOverrideManager.importConfig();
        }
      }
    }
  }

  private static void createServerConfig(ForgeConfigSpec spec, String suffix) {
    String fileName = "corpsecomplex-" + suffix + ".toml";
    ModLoadingContext.get().registerConfig(Type.SERVER, spec, fileName);
    File defaults = new File(FMLPaths.GAMEDIR.get() + "/defaultconfigs/" + fileName);

    if (!defaults.exists()) {
      try {
        FileUtils.copyInputStreamToFile(Objects
                .requireNonNull(CorpseComplex.class.getClassLoader().getResourceAsStream(fileName)),
            defaults);
      } catch (IOException e) {
        LOGGER.error("Error creating default config for " + fileName);
      }
    }
  }
}
