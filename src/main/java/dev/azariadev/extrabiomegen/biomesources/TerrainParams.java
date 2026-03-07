package dev.azariadev.extrabiomegen.biomesources;

import net.minecraft.world.level.biome.Climate;

public class TerrainParams {
    public static final long DEPTH_SHALLOW = Climate.quantizeCoord(0.1f);
    public static final long DEPTH_NORMAL = Climate.quantizeCoord(0.55f);
    public static final long DEPTH_DEEP = Climate.quantizeCoord(1f);
    public static final long DEPTH_VERY_DEEP = Climate.quantizeCoord(1.15f);

    public static final long CONT_DEEP_OCEAN = Climate.quantizeCoord(-1.05f);
    public static final long CONT_SHALLOW_OCEAN = Climate.quantizeCoord(-0.455f);
    public static final long CONT_COAST = Climate.quantizeCoord(-0.19f);
    public static final long CONT_LOWLAND = Climate.quantizeCoord(-0.11f);
    public static final long CONT_HIGHLAND = Climate.quantizeCoord(0.03f);
    public static final long CONT_INTERIOR = Climate.quantizeCoord(0.3f);
    public static final long CONT_DEEP_INTERIOR = Climate.quantizeCoord(0.7f);

    public static final long EROSION_RUGGED = Climate.quantizeCoord(-0.7799f);
    public static final long EROSION_CRAGGY = Climate.quantizeCoord(-0.375f);
    public static final long EROSION_NORMAL = Climate.quantizeCoord(-0.2225f);
    public static final long EROSION_ROLLING = Climate.quantizeCoord(0.05f);
    public static final long EROSION_SMOOTH = Climate.quantizeCoord(0.45f);
    public static final long EROSION_FLAT = Climate.quantizeCoord(0.55f);

    public static final long TEMP_COLD = Climate.quantizeCoord(-0.45f);
    public static final long TEMP_NORMAL = Climate.quantizeCoord(-0.15f);
    public static final long TEMP_WARM = Climate.quantizeCoord(0.2f);
    public static final long TEMP_HOT = Climate.quantizeCoord(0.55f);

    public static final long HUMIDITY_DRY = Climate.quantizeCoord(-0.35f);
    public static final long HUMIDITY_NORMAL = Climate.quantizeCoord(-0.1f);
    public static final long HUMIDITY_WET = Climate.quantizeCoord(0.1f);
    public static final long HUMIDITY_HUMID = Climate.quantizeCoord(0.3f);
    public static final long HUMIDITY_LUSH = Climate.quantizeCoord(0.5f);

    public static final long WEIRD_NORMAL_OUTER_SLOPE = Climate.quantizeCoord(-0.9333f);
    public static final long WEIRD_NORMAL_OUTER_PEAK = Climate.quantizeCoord(-0.7666f);
    public static final long WEIRD_NORMAL_INNER_SLOPE = Climate.quantizeCoord(-0.5666f);
    public static final long WEIRD_NORMAL_INNER_VALLEY = Climate.quantizeCoord(-0.4f);
    public static final long WEIRD_NORMAL_RIVER_BANK = Climate.quantizeCoord(-0.2666f);
    public static final long WEIRD_VAR_RIVER_BANK = Climate.quantizeCoord(0f);
    public static final long WEIRD_VAR_INNER_VALLEY = Climate.quantizeCoord(0.2666f);
    public static final long WEIRD_VAR_INNER_SLOPE = Climate.quantizeCoord(0.4f);
    public static final long WEIRD_VAR_OUTER_PEAK = Climate.quantizeCoord(0.5666f);
    public static final long WEIRD_VAR_OUTER_SLOPE = Climate.quantizeCoord(0.7666f);
    public static final long WEIRD_VAR_OUTER_VALLEY = Climate.quantizeCoord(0.9333f);

    public static int depthLevel (long depth) {
        if (depth < DEPTH_NORMAL) return 0;
        if (depth < DEPTH_DEEP) return 1;
        if (depth < DEPTH_VERY_DEEP) return 2;
        return 3;
    }

    public static int continentalnessLevel (long continentalness) {
        if (continentalness < CONT_DEEP_OCEAN) return 0;
        if (continentalness < CONT_SHALLOW_OCEAN) return 1;
        if (continentalness < CONT_COAST) return 2;
        if (continentalness < CONT_LOWLAND) return 3;
        if (continentalness < CONT_HIGHLAND) return 4;
        if (continentalness < CONT_INTERIOR) return 5;
        if (continentalness < CONT_DEEP_INTERIOR) return 6;
        return 7;
    }

    public static int oceanContinentalnessLevel (long continentalness) {
        if (continentalness < CONT_SHALLOW_OCEAN) return 1;
        return 0;
    }

    public static int landContinentalnessLevel (long continentalness) {
        if (continentalness < CONT_LOWLAND) return 0;
        if (continentalness < CONT_HIGHLAND) return 1;
        if (continentalness < CONT_INTERIOR) return 2;
        return 3;
    }

    public static int erosionLevel (long erosion) {
        if (erosion < EROSION_RUGGED) return 0;
        if (erosion < EROSION_CRAGGY) return 1;
        if (erosion < EROSION_NORMAL) return 2;
        if (erosion < EROSION_ROLLING) return 3;
        if (erosion < EROSION_SMOOTH) return 4;
        if (erosion < EROSION_FLAT) return 5;
        return 6;
    }

    public static int temperatureLevel (long temperature) {
        if (temperature < TEMP_COLD) return 0;
        if (temperature < TEMP_NORMAL) return 1;
        if (temperature < TEMP_WARM) return 2;
        if (temperature < TEMP_HOT) return 3;
        return 4;
    }

    public static int humidityLevel (long humidity) {
        if (humidity < HUMIDITY_DRY) return 0;
        if (humidity < HUMIDITY_NORMAL) return 1;
        if (humidity < HUMIDITY_WET) return 2;
        if (humidity < HUMIDITY_HUMID) return 3;
        if (humidity < HUMIDITY_LUSH) return 4;
        return 5;
    }

    public static int landHumidityLevel (long humidity) {
        if (humidity < HUMIDITY_DRY) return 0;
        if (humidity < HUMIDITY_NORMAL) return 1;
        if (humidity < HUMIDITY_WET) return 2;
        if (humidity < HUMIDITY_HUMID) return 3;
        return 4;
    }

    public static int weirdnessLevel (long weirdness) {
        if (weirdness < WEIRD_NORMAL_OUTER_SLOPE) return 0;
        if (weirdness < WEIRD_NORMAL_OUTER_PEAK) return 1;
        if (weirdness < WEIRD_NORMAL_INNER_SLOPE) return 2;
        if (weirdness < WEIRD_NORMAL_INNER_VALLEY) return 3;
        if (weirdness < WEIRD_NORMAL_RIVER_BANK) return 4;
        if (weirdness < WEIRD_VAR_RIVER_BANK) return 5;
        // #6 is 'river_override', which is ignored here.
        if (weirdness < WEIRD_VAR_INNER_VALLEY) return 7;
        if (weirdness < WEIRD_VAR_INNER_SLOPE) return 8;
        if (weirdness < WEIRD_VAR_OUTER_PEAK) return 9;
        if (weirdness < WEIRD_VAR_OUTER_SLOPE) return 10;
        if (weirdness < WEIRD_VAR_OUTER_VALLEY) return 11;
        return 12;
    }
}
