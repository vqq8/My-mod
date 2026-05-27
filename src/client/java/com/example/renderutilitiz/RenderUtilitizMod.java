package com.example.renderutilitiz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class RenderUtilitizMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("renderutilitiz");
    private static boolean isEnabled = true;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            // Toggle message every 5 seconds for testing (no key bind yet)
            // This is just to prove the tick event works
            if (client.tickCount % 100 == 0) {
                client.player.sendSystemMessage(Component.literal("RenderUtilitiz tick - enabled: " + isEnabled));
            }
        });
        LOGGER.info("RenderUtilitiz mod loaded!");
    }
}