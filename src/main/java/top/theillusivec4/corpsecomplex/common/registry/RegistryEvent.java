package top.theillusivec4.corpsecomplex.common.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.corpsecomplex.CorpseComplex;

@Mod.EventBusSubscriber(modid = CorpseComplex.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryEvent {
    @SubscribeEvent
    public static void registerCreativeTab(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(CorpseComplex.MODID, CorpseComplex.MODID), (builder) -> builder
                .title(Component.translatable("creativetab.corpsecomplex.corpsecomplex"))
                .icon(() -> CorpseComplexRegistry.SCROLL.get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    output.accept(CorpseComplexRegistry.SCROLL.get());
                }));
    }
}
