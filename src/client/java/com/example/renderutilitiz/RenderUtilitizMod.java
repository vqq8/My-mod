package com.example.renderutilitiz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class RenderUtilitizMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("renderutilitiz");

    @Override
    public void onInitializeClient() {
        LOGGER.info("RenderUtilitiz mod loaded!");
    }
}