package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum Weirdness {
    NORMAL_SLOPE,
    NORMAL_PEAK,
    NORMAL_RIVERSIDE,
    VARIANT_RIVERSIDE,
    VARIANT_PEAK,
    VARIANT_SLOPE;

    public static final Codec<Weirdness> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "normal_slope": return NORMAL_SLOPE;
                case "normal_peak": return NORMAL_PEAK;
                case "normal_riverside": return NORMAL_RIVERSIDE;
                case "variant_riverside": return VARIANT_RIVERSIDE;
                case "variant_peak": return VARIANT_PEAK;
                case "variant_slope": return VARIANT_SLOPE;
                default: throw new IllegalArgumentException("Unknown weirdness: " + str);
            }
        },
        temp -> {
            switch (temp) {
                case NORMAL_SLOPE: return "normal_slope";
                case NORMAL_PEAK: return "normal_peak";
                case VARIANT_RIVERSIDE: return "variant_riverside";
                case VARIANT_PEAK: return "variant_peak";
                case VARIANT_SLOPE: return "variant_slope";
                default: return "normal_riverside";
            }
        }
    );
}