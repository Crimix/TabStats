package com.black_dog20.tabstats.common.utils;

import com.black_dog20.bml.utils.translate.ITranslation;
import com.black_dog20.tabstats.TabStats;

public enum Translations  implements ITranslation {
    CATEGORY("keys.category"),
    SHOW_KEY("keys.show"),
    PAGE("gui.page"),
    NAME("gui.name"),
    LAST_SEEN("gui.last_seen"),
    TIME_PLAYED("gui.time_played"),
    KILLS("gui.kills"),
    DEATHS("gui.deaths"),
    DEATHS_PER_HOUR("gui.deaths_per_hour"),
    NOW("gui.now");


    private final String modId;
    private final String key;

    Translations(String key) {
        this.modId = TabStats.MOD_ID;
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getModId() {
        return this.modId;
    }
}