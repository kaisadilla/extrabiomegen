package dev.azariadev.extrabiomegen.biomesources;

import com.mojang.serialization.Codec;
import dev.azariadev.extrabiomegen.biomesources.parameters.Humidity;

public enum BiomePlacementMode {
    TERRAIN,
    VORONOI,
    MIXED;

    @Override
    public String toString () {
        return super.toString().toLowerCase();
    }

    public static final Codec<BiomePlacementMode> CODEC = Codec.STRING.xmap(
        str -> {
            switch (str) {
                case "terrain": return TERRAIN;
                case "voronoi": return VORONOI;
                case "mixed": return MIXED;
                default: throw new IllegalArgumentException("Unknown biome placement: " + str);
            }
        },
        bpm -> {
            switch (bpm) {
                case TERRAIN: return "terrain";
                case VORONOI: return "voronoi";
                case MIXED: return "mixed";
                default: throw new IllegalArgumentException("Unknown biome placement enum: " + bpm);
            }
        }
    );
};