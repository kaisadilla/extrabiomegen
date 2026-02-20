package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

import java.util.Map;

public enum Temperature {
    FROZEN,
    COLD,
    NORMAL,
    WARM,
    HOT;

    @Override
    public String toString () {
        return super.toString().toLowerCase();
    }

    public static Temperature parse (int value) {
        switch (value) {
            case 0: return FROZEN;
            case 1: return COLD;
            case 2: return NORMAL;
            case 3: return WARM;
            case 4: return HOT;
            default: return null;
        }
    }

    public static final Codec<Temperature> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "frozen": return FROZEN;
                case "cold": return COLD;
                case "normal": return NORMAL;
                case "warm": return WARM;
                case "hot": return HOT;
                default: throw new IllegalArgumentException("Unknown temperature: " + str);
            }
        },
        t -> {
            switch (t) {
                case FROZEN: return "frozen";
                case COLD: return "cold";
                case WARM: return "warm";
                case HOT: return "hot";
                default: return "normal";
            }
        }
    );
}
