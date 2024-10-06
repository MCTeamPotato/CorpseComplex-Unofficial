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

package top.theillusivec4.corpsecomplex.common.capability;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.corpsecomplex.CorpseComplex;
import top.theillusivec4.corpsecomplex.common.DeathSettings;
import top.theillusivec4.corpsecomplex.common.util.DeathInfo;
import top.theillusivec4.corpsecomplex.common.util.manager.DeathSettingManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeathStorageCapability {

  @CapabilityInject(IDeathStorage.class)
  public static final Capability<IDeathStorage> DEATH_STORAGE_CAP;

  public static final ResourceLocation ID = new ResourceLocation(CorpseComplex.MODID,
      "death_storage");

  private static final String INVENTORIES = "Inventories";
  private static final String EFFECTS = "Effects";

  static {
    DEATH_STORAGE_CAP = null;
  }

  public static void register() {
    CapabilityManager.INSTANCE.register(IDeathStorage.class, new IStorage<IDeathStorage>() {

      @Override
      public Tag writeNbt(Capability<IDeathStorage> capability, IDeathStorage instance,
          Direction side) {
        CompoundTag compound = new CompoundTag();
        CompoundTag inventories = new CompoundTag();
        instance.getDeathInventory().forEach(inventories::put);
        compound.put(INVENTORIES, inventories);
        ListTag effects = new ListTag();
        instance.getEffects().forEach(effectInstance -> {
          CompoundTag effect = new CompoundTag();
          effectInstance.write(effect);
          effects.add(effect);
        });
        compound.put(EFFECTS, effects);
        DeathInfo info = instance.getDeathInfo();
        if (info != null) {
          info.write(compound);
        }
        return compound;
      }

      @Override
      public void readNBT(Capability<IDeathStorage> capability, IDeathStorage instance,
          Direction side, Tag nbt) {
        CompoundTag compound = (CompoundTag) nbt;
        CompoundTag inventories = compound.getCompound(INVENTORIES);
        inventories.getAllKeys().forEach(modid -> instance.addInventory(modid, inventories.get(modid)));
        ListTag effects = compound.getList(EFFECTS, Tag.TAG_COMPOUND);
        effects.forEach(effect -> {
          MobEffectInstance effectInstance = MobEffectInstance.load((CompoundTag) effect);
          instance.addEffectInstance(effectInstance);
        });
        DeathInfo deathDamageSource = new DeathInfo();
        deathDamageSource.read(compound);
        instance.setDeathDamageSource(deathDamageSource);
      }
    }, DeathStorage::new);
  }

  public static LazyOptional<IDeathStorage> getCapability(final Player playerEntity) {
    return playerEntity.getCapability(DEATH_STORAGE_CAP);
  }

  public interface IDeathStorage {

    Player getPlayer();

    void buildSettings();

    DeathSettings getSettings();

    void setDeathDamageSource(DeathInfo deathDamageSource);

    DeathInfo getDeathInfo();

    void addInventory(String modid, Tag nbt);

    Tag getInventory(String modid);

    Map<String, Tag> getDeathInventory();

    void clearDeathInventory();

    void addEffectInstance(MobEffectInstance effectInstance);

    void clearEffects();

    List<MobEffectInstance> getEffects();
  }

  public static class DeathStorage implements IDeathStorage {

    private final Map<String, Tag> storage = new HashMap<>();
    private final List<MobEffectInstance> effects = new ArrayList<>();
    private final Player player;

    private DeathInfo deathDamageSource;
    private DeathSettings deathSettings;

    public DeathStorage() {
      this(null);
    }

    public DeathStorage(@Nullable Player playerEntity) {
      this.player = playerEntity;
    }

    @Nullable
    @Override
    public Player getPlayer() {
      return this.player;
    }

    @Override
    public void buildSettings() {
      deathSettings = DeathSettingManager.buildSettings(this);
    }

    @Override
    public DeathSettings getSettings() {
      if (deathSettings == null) {
        this.buildSettings();
      }
      return deathSettings;
    }

    @Override
    public void setDeathDamageSource(DeathInfo deathDamageSource) {
      this.deathDamageSource = deathDamageSource;
    }

    @Override
    public DeathInfo getDeathInfo() {
      return this.deathDamageSource;
    }

    @Override
    public void addInventory(String modid, Tag nbt) {
      this.storage.put(modid, nbt);
    }

    @Override
    public Tag getInventory(String modid) {
      return this.storage.get(modid);
    }

    @Override
    public Map<String, Tag> getDeathInventory() {
      return ImmutableMap.copyOf(this.storage);
    }

    @Override
    public void clearDeathInventory() {
      this.storage.clear();
    }

    @Override
    public void addEffectInstance(MobEffectInstance effectInstance) {
      MobEffectInstance instance = new MobEffectInstance(effectInstance.getEffect(),
          effectInstance.getDuration(), effectInstance.getAmplifier());
      instance.setCurativeItems(effectInstance.getCurativeItems());
      this.effects.add(instance);
    }

    @Override
    public void clearEffects() {
      this.effects.clear();
    }

    @Override
    public List<MobEffectInstance> getEffects() {
      return ImmutableList.copyOf(this.effects);
    }
  }

  public static class Provider implements ICapabilitySerializable<Tag> {

    final LazyOptional<IDeathStorage> optional;
    final IDeathStorage data;

    public Provider(Player player) {
      this.data = new DeathStorage(player);
      this.optional = LazyOptional.of(() -> data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction side) {
      return DEATH_STORAGE_CAP.orEmpty(capability, optional);
    }

    @Override
    public Tag serializeNBT() {
      return DEATH_STORAGE_CAP.writeNBT(data, null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
      DEATH_STORAGE_CAP.readNBT(data, null, nbt);
    }
  }
}
