package com.example.renderutilitiz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class RenderUtilitizMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("renderutilitiz");
    private static boolean isEnabled = true;
    private static long lastToggleTime = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            long now = System.currentTimeMillis();
            long window = client.getWindow().getWindow();
            if (now - lastToggleTime > 200 && InputConstants.isKeyDown(window, GLFW.GLFW_KEY_Y)) {
                isEnabled = !isEnabled;
                lastToggleTime = now;
                client.player.displayClientMessage(Component.literal("RenderUtilitiz " + (isEnabled ? "§aON" : "§cOFF")), true);
            }
        });
        LOGGER.info("RenderUtilitiz mod loaded! Press Y to toggle.");
    }
}