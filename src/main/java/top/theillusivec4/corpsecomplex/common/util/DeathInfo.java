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

package top.theillusivec4.corpsecomplex.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DeathInfo {

  private String damageType;
  private boolean isFireDamage;
  private boolean isMagicDamage;
  private boolean isExplosion;
  private boolean isProjectile;
  @Nullable
  private EntityType<?> immediateSource;
  @Nullable
  private EntityType<?> trueSource;
  private ResourceLocation dimension;
  private List<String> gameStages;

  public DeathInfo() {
  }

  public DeathInfo(DamageSource source, Level world, @Nonnull List<String> gameStages) {
    this.damageType = source.getMsgId();
    this.isFireDamage = source.isFire();
    this.isMagicDamage = source.isMagic();
    this.isExplosion = source.isExplosion();
    this.isProjectile = source.isProjectile();
    this.immediateSource =
        source.getDirectEntity() != null ? source.getDirectEntity().getType() : null;
    this.trueSource = source.getEntity() != null ? source.getEntity().getType() : null;
    this.dimension = world.dimension().registry();
    this.gameStages = gameStages;
  }

  public String getDamageType() {
    return damageType;
  }

  public boolean isFireDamage() {
    return isFireDamage;
  }

  public boolean isMagicDamage() {
    return isMagicDamage;
  }

  public boolean isExplosion() {
    return isExplosion;
  }

  public boolean isProjectile() {
    return isProjectile;
  }

  @Nullable
  public EntityType<?> getImmediateSource() {
    return immediateSource;
  }

  @Nullable
  public EntityType<?> getTrueSource() {
    return trueSource;
  }

  public ResourceLocation getDimension() {
    return dimension;
  }

  public List<String> getGameStages() {
    return gameStages;
  }

  public CompoundTag write(CompoundTag compoundNBT) {
    CompoundTag tag = new CompoundTag();
    tag.putString("DamageType", this.damageType);
    tag.putBoolean("FireDamage", this.isFireDamage);
    tag.putBoolean("MagicDamage", this.isMagicDamage);
    tag.putBoolean("Explosion", this.isExplosion);
    tag.putBoolean("Projectile", this.isProjectile);
    if (this.immediateSource != null) {
        tag.putString("ImmediateSource", this.immediateSource.getCategory().getName());
    }
    if (this.trueSource != null) {
        tag.putString("TrueSource", this.trueSource.getCategory().getName());
    }
    tag.putString("Dimension", this.dimension.toString());
    ListTag list = new ListTag();
    this.gameStages.forEach(stage -> list.add(StringTag.valueOf(stage)));
    tag.put("GameStages", list);
    compoundNBT.put("DeathDamageSource", tag);
    return compoundNBT;
  }

  public void read(CompoundTag compoundNBT) {
    CompoundTag tag = compoundNBT.getCompound("DeathDamageSource");
    this.damageType = tag.getString("DamageType");
    this.isFireDamage = tag.getBoolean("FireDamage");
    this.isMagicDamage = tag.getBoolean("MagicDamage");
    this.isExplosion = tag.getBoolean("Explosion");
    this.isProjectile = tag.getBoolean("Projectile");
    if (tag.contains("ImmediateSource")) {
      this.immediateSource = ForgeRegistries.ENTITY_TYPES
          .getValue(new ResourceLocation(tag.getString("ImmediateSource")));
    }
    if (tag.contains("TrueSource")) {
      this.trueSource = ForgeRegistries.ENTITY_TYPES
          .getValue(new ResourceLocation(tag.getString("TrueSource")));
    }
    this.dimension = new ResourceLocation(tag.getString("Dimension"));
    this.gameStages = new ArrayList<>();
    ListTag list = tag.getList("GameStages", Tag.TAG_STRING);
    list.forEach(stage -> this.gameStages.add(stage.getAsString()));
  }
}
