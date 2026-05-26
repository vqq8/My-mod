package com.example.renderutilitiz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class RenderUtilitizMod implements ClientModInitializer {
    public static final String MOD_ID = "renderutilitiz";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static boolean isEnabled = true;
    private static final int TOGGLE_KEY = GLFW.GLFW_KEY_Y;
    private static long lastToggleTime = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            long now = System.currentTimeMillis();
            if (now - lastToggleTime > 200 && InputConstants.isKeyDown(client.getWindow().getWindow(), TOGGLE_KEY)) {
                isEnabled = !isEnabled;
                lastToggleTime = now;
                client.player.displayClientMessage(Component.literal("RenderUtilitiz " + (isEnabled ? "§aON" : "§cOFF")), true);
            }
            if (!isEnabled) return;
            if (client.level == null) return;
            if (!client.options.keyAttack.isDown()) return;
            // aim assist logic will go here
        });
        LOGGER.info("RenderUtilitiz initialized!");
    }
}