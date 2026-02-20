package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum Continentalness {
    EXOTIC,
    DEEP_OCEAN,
    OCEAN,
    COAST,
    LOWLAND,
    HIGHLAND,
    INTERIOR,
    DEEP_INTERIOR;

    @Override
    public String toString () {
        return super.toString().toLowerCase();
    }

    public static Continentalness parse (int value) {
        switch (value) {
            case 0: return EXOTIC;
            case 1: return DEEP_OCEAN;
            case 2: return OCEAN;
            case 3: return COAST;
            case 4: return LOWLAND;
            case 5: return HIGHLAND;
            case 6: return INTERIOR;
            case 7: return DEEP_INTERIOR;
            default: return null;
        }
    }

    public static final Codec<Continentalness> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "exotic": return EXOTIC;
                case "deep_ocean": return DEEP_OCEAN;
                case "ocean": return OCEAN;
                case "coast": return COAST;
                case "lowland": return LOWLAND;
                case "highland": return HIGHLAND;
                case "interior": return INTERIOR;
                case "deep_interior": return DEEP_INTERIOR;
                default: throw new IllegalArgumentException("Unknown continentalness: " + str);
            }
        },
        c -> {
            switch (c) {
                case EXOTIC: return "exotic";
                case DEEP_OCEAN: return "deep_ocean";
                case OCEAN: return "ocean";
                case COAST: return "coast";
                case LOWLAND: return "lowland";
                case INTERIOR: return "interior";
                case DEEP_INTERIOR: return "deep_interior";
                default: return "highland";
            }
        }
    );
}
