package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum Weirdness {
    NORMAL_OUTER_VALLEY,
    NORMAL_OUTER_SLOPE,
    NORMAL_OUTER_PEAK,
    NORMAL_INNER_SLOPE,
    NORMAL_INNER_VALLEY,
    NORMAL_RIVER_BANK,
    VARIANT_RIVER_BANK,
    VARIANT_INNER_VALLEY,
    VARIANT_INNER_SLOPE,
    VARIANT_OUTER_PEAK,
    VARIANT_OUTER_SLOPE,
    VARIANT_OUTER_VALLEY;

    public static final Codec<Weirdness> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "normal_outer_valley": return NORMAL_OUTER_VALLEY;
                case "normal_outer_slope":  return NORMAL_OUTER_SLOPE;
                case "normal_outer_peak":   return NORMAL_OUTER_PEAK;
                case "normal_inner_slope":  return NORMAL_INNER_SLOPE;
                case "normal_inner_valley": return NORMAL_INNER_VALLEY;
                case "normal_river_bank":   return NORMAL_RIVER_BANK;
                case "variant_river_bank":   return VARIANT_RIVER_BANK;
                case "variant_inner_valley": return VARIANT_INNER_VALLEY;
                case "variant_inner_slope":  return VARIANT_INNER_SLOPE;
                case "variant_outer_peak":   return VARIANT_OUTER_PEAK;
                case "variant_outer_slope":  return VARIANT_OUTER_SLOPE;
                case "variant_outer_valley": return VARIANT_OUTER_VALLEY;
                default: throw new IllegalArgumentException("Unknown weirdness: " + str);
            }
        },
        w -> {
            switch (w) {
                case NORMAL_OUTER_VALLEY:  return "normal_outer_valley";
                case NORMAL_OUTER_SLOPE:   return "normal_outer_slope";
                case NORMAL_OUTER_PEAK:    return "normal_outer_peak";
                case NORMAL_INNER_SLOPE:   return "normal_inner_slope";
                case NORMAL_INNER_VALLEY:  return "normal_inner_valley";
                case NORMAL_RIVER_BANK:    return "normal_river_bank";
                case VARIANT_RIVER_BANK:   return "variant_river_bank";
                case VARIANT_INNER_VALLEY: return "variant_inner_valley";
                case VARIANT_INNER_SLOPE:  return "variant_inner_slope";
                case VARIANT_OUTER_PEAK:   return "variant_outer_peak";
                case VARIANT_OUTER_SLOPE:  return "variant_outer_slope";
                case VARIANT_OUTER_VALLEY: return "variant_outer_valley";
                default: throw new IllegalArgumentException("Unknown weirdness enum: " + w);
            }
        }
    );
}