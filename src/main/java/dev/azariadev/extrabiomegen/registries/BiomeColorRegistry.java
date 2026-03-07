package dev.azariadev.extrabiomegen.registries;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BiomeColorRegistry {
    private static final Map<ResourceLocation, Integer> COLORS = new HashMap<>();

    public static int getColor (ResourceLocation biome) {
        return COLORS.getOrDefault(biome, 0xff00ff);
    }

    static void clear () {
        COLORS.clear();
    }

    static void put (ResourceLocation biome, int color) {
        COLORS.put(biome, color);
    }
}
