package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum OceanContinentalness {
    SHALLOW,
    DEEP;

    public static final Codec<OceanContinentalness> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "shallow": return SHALLOW;
                case "deep": return DEEP;
                default: throw new IllegalArgumentException("Unknown ocean depth: " + str);
            }
        },
        c -> {
            switch (c) {
                case DEEP: return "deep";
                default: return "shallow";
            }
        }
    );
}
