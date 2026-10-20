package com.ldh.quiz;

import java.util.List;

import com.ldh.quiz.client.ClientHooks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Mot quyen sach = mot cau hoi tu luan. Chuot phai de mo bang cau hoi. */
public class QuizItem extends Item {
    private final int index;

    public QuizItem(int index, Item.Properties props) {
        super(props);
        this.index = index;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            final int n = this.index;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.open(n));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ldhquiz.cau_" + index).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ldhquiz.use").withStyle(ChatFormatting.YELLOW));
    }
}
