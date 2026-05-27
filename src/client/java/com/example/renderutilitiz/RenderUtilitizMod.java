package com.example.renderutilitiz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

@Environment(EnvType.CLIENT)
public class RenderUtilitizMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("renderutilitiz");
    
    // Configurable settings
    public static class Config {
        public int toggleKey = GLFW.GLFW_KEY_Y;
        public long aimCooldownMs = 100;
        public double maxRange = 6.0;
        public double maxFov = 45.0;
        public float aimSpeed = 0.35f;
        public boolean enabledByDefault = true;
    }
    
    private static Config config = new Config();
    private static boolean isEnabled;
    private static long lastAimTime = 0;
    private static final String CONFIG_PATH = "config/renderutilitiz.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KeyMapping toggleKeyMapping;
    
    private static void loadConfig() {
        Path path = Paths.get(CONFIG_PATH);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                config = GSON.fromJson(reader, Config.class);
                LOGGER.info("Loaded config from " + CONFIG_PATH);
            } catch (IOException e) {
                LOGGER.error("Failed to load config", e);
            }
        } else {
            saveConfig();
            LOGGER.info("Created default config at " + CONFIG_PATH);
        }
        isEnabled = config.enabledByDefault;
    }
    
    private static void saveConfig() {
        try {
            Files.createDirectories(Paths.get("config"));
            try (Writer writer = Files.newBufferedWriter(Paths.get(CONFIG_PATH))) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
    
    @Override
    public void onInitializeClient() {
        loadConfig();
        
        // Register key binding using the config's toggle key
        toggleKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.renderutilitiz.toggle",
                InputConstants.Type.KEYSYM,
                config.toggleKey,
                "category.renderutilitiz"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            // Toggle with the registered key binding
            while (toggleKeyMapping.consumeClick()) {
                isEnabled = !isEnabled;
                client.player.displayClientMessage(Component.literal("RenderUtilitiz " + (isEnabled ? "§aON" : "§cOFF")), true);
            }
            
            if (!isEnabled) return;
            if (client.level == null) return;
            if (!client.options.keyAttack.isDown()) return;
            
            long now = System.currentTimeMillis();
            if (now - lastAimTime < config.aimCooldownMs) return;
            lastAimTime = now;
            
            Entity target = findBestTarget(client);
            if (target != null) smoothLookAt(client, target);
        });
        
        LOGGER.info("RenderUtilitiz aim assist initialized. Config file: " + CONFIG_PATH);
        LOGGER.info("Toggle key: " + config.toggleKey + " (change in config)");
    }
    
    private static Entity findBestTarget(Minecraft client) {
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player) continue;
            if (!(entity instanceof LivingEntity)) continue;
            if (!entity.isAlive()) continue;
            double dist = client.player.distanceTo(entity);
            if (dist > config.maxRange) continue;
            double fov = getAngleToEntity(client, entity);
            if (fov > config.maxFov) continue;
            double score = dist * 0.7 + fov * 0.3;
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }
        return best;
    }
    
    private static double getAngleToEntity(Minecraft client, Entity entity) {
        Vec3 cameraVec = client.player.getViewVector(1.0f);
        Vec3 toEntity = entity.getBoundingBox().getCenter()
                .subtract(client.player.getEyePosition())
                .normalize();
        double dot = cameraVec.dot(toEntity);
        dot = Math.max(-1.0, Math.min(1.0, dot));
        return Math.toDegrees(Math.acos(dot));
    }
    
    private static void smoothLookAt(Minecraft client, Entity target) {
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 playerEye = client.player.getEyePosition();
        double dx = targetPos.x - playerEye.x;
        double dy = targetPos.y - playerEye.y;
        double dz = targetPos.z - playerEye.z;
        double horizontal = Math.sqrt(dx*dx + dz*dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        float newYaw = client.player.getYRot() + (targetYaw - client.player.getYRot()) * config.aimSpeed;
        float newPitch = client.player.getXRot() + (targetPitch - client.player.getXRot()) * config.aimSpeed;
        client.player.setYRot(newYaw);
        client.player.setXRot(newPitch);
    }
}
