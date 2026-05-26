package com.example.renderutilitiz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class RenderUtilitizMod implements ClientModInitializer {
    public static final String MOD_ID = "renderutilitiz";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KeyMapping toggleKey;
    private static boolean isEnabled = true;
    private static long lastAimTime = 0;
    private static final long AIM_COOLDOWN_MS = 100;
    private static final double MAX_RANGE = 6.0;
    private static final double MAX_FOV = 45.0;
    private static final float AIM_SPEED = 0.35f;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.renderutilitiz.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.renderutilitiz"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.consumeClick()) {
                isEnabled = !isEnabled;
                if (client.player != null) {
                    client.player.displayClientMessage(
                        Component.literal("RenderUtilitiz " + (isEnabled ? "§aON" : "§cOFF")),
                        true
                    );
                }
            }
            if (!isEnabled) return;
            if (client.player == null || client.level == null) return;
            if (!client.options.keyAttack.isDown()) return;
            long now = System.currentTimeMillis();
            if (now - lastAimTime < AIM_COOLDOWN_MS) return;
            lastAimTime = now;
            Entity target = findBestTarget(client);
            if (target != null) smoothLookAt(client, target);
        });
        LOGGER.info("RenderUtilitiz initialized!");
    }

    private static Entity findBestTarget(Minecraft client) {
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player) continue;
            if (!(entity instanceof LivingEntity)) continue;
            if (!entity.isAlive()) continue;
            double dist = client.player.distanceTo(entity);
            if (dist > MAX_RANGE) continue;
            double fov = getAngleToEntity(client, entity);
            if (fov > MAX_FOV) continue;
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
        float newYaw = client.player.getYRot() + (targetYaw - client.player.getYRot()) * AIM_SPEED;
        float newPitch = client.player.getXRot() + (targetPitch - client.player.getXRot()) * AIM_SPEED;
        client.player.setYRot(newYaw);
        client.player.setXRot(newPitch);
    }
}