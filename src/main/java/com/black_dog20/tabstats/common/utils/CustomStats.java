package com.black_dog20.tabstats.common.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import static com.black_dog20.tabstats.TabStats.MOD_ID;

public class CustomStats {
	public static final ResourceLocation ADVANCEMENTS_GAINED = registerCustomStat("advancements_gained");

	public static void register() {}

	private static ResourceLocation registerCustomStat(String name) {
		ResourceLocation resourcelocation = new ResourceLocation(MOD_ID, name);
		Registry.register(Registry.CUSTOM_STAT, name, resourcelocation);
		Stats.CUSTOM.get(resourcelocation, StatFormatter.DEFAULT);
		return resourcelocation;
	}
}
