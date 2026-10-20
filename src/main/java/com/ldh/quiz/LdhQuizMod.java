package com.ldh.quiz;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(LdhQuizMod.MODID)
public class LdhQuizMod {
    public static final String MODID = "ldhquiz";
    public static final int QUESTIONS = 6;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final List<RegistryObject<Item>> BOOKS = new ArrayList<>();

    static {
        for (int i = 1; i <= QUESTIONS; i++) {
            final int n = i;
            BOOKS.add(ITEMS.register("cau_" + n, () -> new QuizItem(n, new Item.Properties().stacksTo(1))));
        }
    }

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ldhquiz"))
            .icon(() -> new ItemStack(BOOKS.get(0).get()))
            .displayItems((params, output) -> {
                for (RegistryObject<Item> b : BOOKS) {
                    output.accept(b.get());
                }
            })
            .build());

    public LdhQuizMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        TABS.register(bus);
    }
}
