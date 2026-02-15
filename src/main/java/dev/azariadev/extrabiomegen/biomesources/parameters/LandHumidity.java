package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum LandHumidity {
    ARID,
    DRY,
    NORMAL,
    WET,
    HUMID;

    public static final Codec<LandHumidity> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "arid": return ARID;
                case "dry": return DRY;
                case "normal": return NORMAL;
                case "wet": return WET;
                case "humid": return HUMID;
                default: throw new IllegalArgumentException("Unknown humidity: " + str);
            }
        },
        h -> {
            switch (h) {
                case ARID: return "arid";
                case DRY: return "dry";
                case WET: return "wet";
                case HUMID: return "humid";
                default: return "normal";
            }
        }
    );
}
