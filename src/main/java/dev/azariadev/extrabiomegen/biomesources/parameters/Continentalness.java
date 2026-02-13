package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum Continentalness {
    COAST,
    LOWLAND,
    HIGHLAND,
    INTERIOR;

    public static final Codec<Continentalness> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "coast": return COAST;
                case "lowland": return LOWLAND;
                case "highland": return HIGHLAND;
                case "interior": return INTERIOR;
                default: throw new IllegalArgumentException("Unknown continentalness: " + str);
            }
        },
        temp -> {
            switch (temp) {
                case COAST: return "coast";
                case LOWLAND: return "lowland";
                case INTERIOR: return "interior";
                default: return "highland";
            }
        }
    );
}
