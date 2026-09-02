package top.theillusivec4.corpsecomplex.common.modules.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static top.theillusivec4.corpsecomplex.common.CommonEventHandler.LAST_DEATH_DIMENSION;
import static top.theillusivec4.corpsecomplex.common.CommonEventHandler.LAST_DEATH_LOCATION;

public class ScrollItem extends Item {
    public ScrollItem() {
        super(new Properties().stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            int[] list = serverPlayer.getPersistentData().getIntArray(LAST_DEATH_LOCATION).clone();

            if (list.length == 0) {
                player.sendMessage(new TranslatableComponent("message.corpsecomplex.death_loc_null"), player.getUUID());
                return InteractionResultHolder.fail(itemstack);
            }
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entityLiving){
        if (!level.isClientSide && entityLiving instanceof ServerPlayer player){
            int[] list = player.getPersistentData().getIntArray(LAST_DEATH_LOCATION).clone();
            if (list.length > 0){
                ServerLevel destination = player.getLevel();
                String dimensionId = player.getPersistentData().getString(LAST_DEATH_DIMENSION);
                if (!dimensionId.isEmpty()) {
                    ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY,
                            new ResourceLocation(dimensionId));
                    destination = player.getServer().getLevel(dimension);
                }
                if (destination == null) {
                    player.sendMessage(new TranslatableComponent(
                            "message.corpsecomplex.death_dimension_missing"), player.getUUID());
                    return itemStack;
                }
                player.teleportTo(destination, list[0], list[1], list[2],
                        player.getYRot(), player.getXRot());
                if (player.getLevel() != destination) {
                    return itemStack;
                }
                player.getCooldowns().addCooldown(this, 20);
                if (!player.isCreative()) itemStack.shrink(1);
            }
        }
        return itemStack;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 20;
    }
    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(new TranslatableComponent("tooltip.corpsecomplex.scroll"));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return true;
    }
}
