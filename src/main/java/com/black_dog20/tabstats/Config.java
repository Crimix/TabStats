package com.black_dog20.tabstats;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Path;


@Mod.EventBusSubscriber(modid = TabStats.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public static final String CATEGORY_GENERAL = "general";

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public static ModConfigSpec SERVER_CONFIG;

    public static ModConfigSpec.IntValue REFRESH_TICKS;

    static {
        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        REFRESH_TICKS = SERVER_BUILDER.comment("How often should the server send updates to clients", "Default is 100 ticks")
                .defineInRange("refreshTicks", 100, 1,  6000);
        SERVER_BUILDER.pop();

        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static void loadConfig(ModConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {


    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {

    }

}
