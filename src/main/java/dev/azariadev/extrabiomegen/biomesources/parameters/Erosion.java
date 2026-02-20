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

    @Override
    public String toString () {
        return super.toString().toLowerCase();
    }

    public static Erosion parse (int value) {
        switch (value) {
            case 0: return JAGGED;
            case 1: return RUGGED;
            case 2: return CRAGGY;
            case 3: return NORMAL;
            case 4: return ROLLING;
            case 5: return SMOOTH;
            case 6: return FLAT;
            default: return null;
        }
    }

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
