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
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;

public class ItemOverride {

  private final List<Item> items;
  @Nullable
  private final Double dropDurabilityLoss;
  @Nullable
  private final Double keepDurabilityLoss;

  private ItemOverride(Builder builder) {
    this.items = builder.items;
    this.dropDurabilityLoss = builder.dropDurabilityLoss;
    this.keepDurabilityLoss = builder.keepDurabilityLoss;
  }

  public List<Item> getItems() {
    return this.items;
  }

  public Optional<Double> getDropDurabilityLoss() {
    return Optional.ofNullable(this.dropDurabilityLoss);
  }

  public Optional<Double> getKeepDurabilityLoss() {
    return Optional.ofNullable(this.keepDurabilityLoss);
  }

  public static class Builder {

    private List<Item> items;
    private Double dropDurabilityLoss;
    private Double keepDurabilityLoss;

    public Builder items(List<Item> items) {
      this.items = items;
      return this;
    }

    public Builder dropDurabilityLoss(Double dropDurabilityLoss) {
      this.dropDurabilityLoss = dropDurabilityLoss;
      return this;
    }

    public Builder keepDurabilityLoss(Double keepDurabilityLoss) {
      this.keepDurabilityLoss = keepDurabilityLoss;
      return this;
    }

    public ItemOverride build() {

      if (this.items == null) {
        this.items = new ArrayList<>();
      }
      return new ItemOverride(this);
    }
  }
}
