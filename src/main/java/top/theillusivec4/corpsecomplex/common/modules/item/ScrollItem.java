package top.theillusivec4.corpsecomplex.common.modules.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import java.util.Optional;

public class ScrollItem extends Item {
    public ScrollItem() {
        super(new Properties().stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            Optional<GlobalPos> globalPos = player.getLastDeathLocation();
            if (globalPos.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.corpsecomplex.death_loc_null"));
                return InteractionResultHolder.fail(itemstack);
            }
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entityLiving){
        if (!level.isClientSide && entityLiving instanceof ServerPlayer player){
            Optional<GlobalPos> globalPos = player.getLastDeathLocation();
            if (globalPos.isPresent()){
                BlockPos blockPos = globalPos.get().pos();
                player.teleportTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
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
        tooltip.add(Component.translatable("tooltip.corpsecomplex.scroll"));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return true;
    }
}
