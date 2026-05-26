package com.example.renderutilitiz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class RenderUtilitizMod implements ClientModInitializer {
    public static final String MOD_ID = "renderutilitiz";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info(Text.translatable("message.renderutilitiz.ready").getString());
    }
}