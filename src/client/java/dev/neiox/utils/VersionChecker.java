package dev.neiox.utils;

import net.fabricmc.loader.api.FabricLoader;


public class VersionChecker {

    public static String getCurrentVersion() {
        return FabricLoader.getInstance()
                .getModContainer("cooldown-enhanced")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("0.0.0");
    }

}
