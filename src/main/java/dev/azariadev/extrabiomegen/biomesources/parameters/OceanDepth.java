package dev.azariadev.extrabiomegen.biomesources.parameters;

import com.mojang.serialization.Codec;

public enum OceanDepth {
    SHALLOW,
    DEEP;

    public static final Codec<OceanDepth> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "shallow": return SHALLOW;
                case "deep": return DEEP;
                default: throw new IllegalArgumentException("Unknown ocean depth: " + str);
            }
        },
        temp -> {
            switch (temp) {
                case DEEP: return "deep";
                default: return "shallow";
            }
        }
    );
}
