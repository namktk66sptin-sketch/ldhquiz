package com.ldh.quiz.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientHooks {
    private ClientHooks() {}

    public static void open(int index) {
        Minecraft.getInstance().setScreen(new QuizScreen(index));
    }
}
