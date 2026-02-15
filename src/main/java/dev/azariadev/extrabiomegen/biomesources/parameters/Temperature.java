package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

import java.util.Map;

public enum Temperature {
    FROZEN,
    COLD,
    NORMAL,
    WARM,
    HOT;

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
