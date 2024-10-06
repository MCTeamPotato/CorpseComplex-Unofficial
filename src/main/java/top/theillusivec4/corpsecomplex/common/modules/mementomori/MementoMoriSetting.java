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

package top.theillusivec4.corpsecomplex.common.modules.mementomori;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.corpsecomplex.common.config.ConfigParser;
import top.theillusivec4.corpsecomplex.common.config.CorpseComplexConfig;
import top.theillusivec4.corpsecomplex.common.modules.Setting;

import java.util.ArrayList;
import java.util.List;

public class MementoMoriSetting implements Setting<MementoMoriOverride> {

  private List<ItemStack> mementoCures = new ArrayList<>();
  private boolean noFood;
  private double percentXp;

  public List<ItemStack> getMementoCures() {
    return mementoCures;
  }

  public void setMementoCures(List<ItemStack> mementoCures) {
    this.mementoCures = mementoCures;
  }

  public boolean isNoFood() {
    return noFood;
  }

  public void setNoFood(boolean noFood) {
    this.noFood = noFood;
  }

  public double getPercentXp() {
    return percentXp;
  }

  public void setPercentXp(double percentXp) {
    this.percentXp = percentXp;
  }

  @Override
  public void importConfig() {
    this.getMementoCures().clear();
    ConfigParser.parseItems(CorpseComplexConfig.mementoCures)
        .forEach((item, integer) -> this.getMementoCures().add(new ItemStack(item, integer)));
    this.setNoFood(CorpseComplexConfig.noFood);
    this.setPercentXp(CorpseComplexConfig.percentXp);
  }

  @Override
  public void applyOverride(MementoMoriOverride override) {
    override.getMementoCures().ifPresent(this::setMementoCures);
    override.getNoFood().ifPresent(this::setNoFood);
    override.getPercentXp().ifPresent(this::setPercentXp);
  }
}
