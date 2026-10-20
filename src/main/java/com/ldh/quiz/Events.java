package com.ldh.quiz;

import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = LdhQuizMod.MODID)
public class Events {

    private static void giveAll(Player player) {
        for (RegistryObject<Item> b : LdhQuizMod.BOOKS) {
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(b.get()));
        }
    }

    /** Lan dau vao the gioi: nhan du 6 quyen sach. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.getBoolean("ldh_given")) {
            giveAll(player);
            persisted.putBoolean("ldh_given", true);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        }
    }

    /** /ldhquiz : nhan lai du 6 quyen sach. */
    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ldhquiz").executes(ctx -> {
            Player player = ctx.getSource().getPlayerOrException();
            giveAll(player);
            ctx.getSource().sendSuccess(() -> Component.literal("Đã nhận 6 quyển sách ôn thi."), false);
            return 1;
        }));
    }
}
