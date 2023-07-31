package com.black_dog20.tabstats.common.datagen;

import com.black_dog20.bml.datagen.BaseLanguageProvider;
import com.black_dog20.tabstats.TabStats;
import net.minecraft.data.DataGenerator;

import static com.black_dog20.tabstats.common.utils.Translations.*;

public class GeneratorLanguageEnglish extends BaseLanguageProvider {

    public GeneratorLanguageEnglish(DataGenerator gen) {
        super(gen, TabStats.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Keys
        addPrefixed(CATEGORY, "Tab Stats");
        addPrefixed(SHOW_KEY, "Show Tab Stats list");

        // Gui
        addPrefixed(PAGE, "Page %d of %d");
        addPrefixed(NAME,"Name");
        addPrefixed(LAST_SEEN,"Last Seen");
        addPrefixed(TIME_PLAYED, "Time played");
        addPrefixed(KILLS, "Kills");
        addPrefixed(DEATHS, "Deaths");
        addPrefixed(DEATHS_PER_HOUR, "Deaths/Hour");
        addPrefixed(NOW, "Now");
        addPrefixed(ADVANCEMENTS, "Advancements");
    }
}
