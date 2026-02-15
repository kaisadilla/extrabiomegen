package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum CaveDepth {
    SHALLOW,
    NORMAL,
    DEEP,
    VERY_DEEP;

    public static final Codec<CaveDepth> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "shallow": return SHALLOW;
                case "normal": return NORMAL;
                case "deep": return DEEP;
                case "very_deep": return VERY_DEEP;
                default: throw new IllegalArgumentException("Unknown humidity: " + str);
            }
        },
        d -> {
            switch (d) {
                case SHALLOW: return "shallow";
                case DEEP: return "deep";
                case VERY_DEEP: return "very_deep";
                default: return "normal";
            }
        }
    );
}
