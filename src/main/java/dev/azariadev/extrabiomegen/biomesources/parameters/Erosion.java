package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum Erosion {
    JAGGED,
    RUGGED,
    NORMAL,
    SMOOTH,
    FLAT;

    public static final Codec<Erosion> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "jagged": return JAGGED;
                case "rugged": return RUGGED;
                case "normal": return NORMAL;
                case "smooth": return SMOOTH;
                case "flat": return FLAT;
                default: throw new IllegalArgumentException("Unknown erosion: " + str);
            }
        },
        temp -> {
            switch (temp) {
                case JAGGED: return "jagged";
                case RUGGED: return "rugged";
                case SMOOTH: return "smooth";
                case FLAT: return "flat";
                default: return "normal";
            }
        }
    );
}
