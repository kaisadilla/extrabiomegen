package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum Humidity {
    ARID,
    DRY,
    NORMAL,
    WET,
    HUMID,
    LUSH;

    @Override
    public String toString () {
        return super.toString().toLowerCase();
    }

    public static Humidity parse (int value) {
        switch (value) {
            case 0: return ARID;
            case 1: return DRY;
            case 2: return NORMAL;
            case 3: return WET;
            case 4: return HUMID;
            case 5: return LUSH;
            default: return null;
        }
    }

    public static final Codec<Humidity> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "arid": return ARID;
                case "dry": return DRY;
                case "normal": return NORMAL;
                case "wet": return WET;
                case "humid": return HUMID;
                case "lush": return LUSH;
                default: throw new IllegalArgumentException("Unknown humidity: " + str);
            }
        },
        h -> {
            switch (h) {
                case ARID: return "arid";
                case DRY: return "dry";
                case WET: return "wet";
                case HUMID: return "humid";
                case LUSH: return "lush";
                default: return "normal";
            }
        }
    );
}
