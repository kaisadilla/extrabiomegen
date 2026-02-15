package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum Erosion {
    JAGGED,
    RUGGED,
    CRAGGY,
    NORMAL,
    ROLLING,
    SMOOTH,
    FLAT;

    public static final Codec<Erosion> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "jagged": return JAGGED;
                case "rugged": return RUGGED;
                case "craggy": return CRAGGY;
                case "normal": return NORMAL;
                case "rolling": return ROLLING;
                case "smooth": return SMOOTH;
                case "flat": return FLAT;
                default: throw new IllegalArgumentException("Unknown erosion: " + str);
            }
        },
        e -> {
            switch (e) {
                case JAGGED: return "jagged";
                case RUGGED: return "rugged";
                case CRAGGY: return "craggy";
                case ROLLING: return "rolling";
                case SMOOTH: return "smooth";
                case FLAT: return "flat";
                default: return "normal";
            }
        }
    );
}
