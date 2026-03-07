package dev.azariadev.extrabiomegen.biomesources;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.azariadev.extrabiomegen.biomesources.parameters.*;
import dev.azariadev.extrabiomegen.noise.VoronoiMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class MultiNoiseDiscreteBiomeSource extends BiomeSource {
    // region Thresholds
    private static final long RIVER_WEIRDNESS_MIN = Climate.quantizeCoord(-0.05f);
    private static final long RIVER_WEIRDNESS_MAX = Climate.quantizeCoord(0.05f);
    private static final long RIVER_CONT_OCEAN = Climate.quantizeCoord(-0.19f);
    private static final long RIVER_CONT_COAST = Climate.quantizeCoord(-0.11f);
    private static final long RIVER_CONT_INLAND = Climate.quantizeCoord(-0.03f);
    private static final long RIVER_EROSION_LOW = Climate.quantizeCoord(-0.375f);
    private static final long RIVER_EROSION_HIGH = Climate.quantizeCoord(0.55f);
    // endregion Thresholds

    // region Codec
    public static final Codec<MultiNoiseDiscreteBiomeSource> CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf(
                "region_size", 400
            ).forGetter(bs -> bs._regionSize),

            Codec.INT.optionalFieldOf(
                "biome_size", 50
            ).forGetter(bs -> bs._biomeSize),

            Codec.unboundedMap(
                Temperature.CODEC,
                Codec.unboundedMap(
                    OceanContinentalness.CODEC,
                    Biome.CODEC.listOf()
                )
            ).fieldOf("ocean").forGetter(bs -> bs._oceanDef),

            Codec.unboundedMap(
                Temperature.CODEC,
                Biome.CODEC.listOf()
            ).fieldOf("exotic").forGetter(bs -> bs._exoticDef),

            BiomePlacementMode.CODEC.optionalFieldOf(
                "land_placement_mode", BiomePlacementMode.TERRAIN
            ).forGetter(bs -> bs._landPlacementMode),

            Codec.unboundedMap(
                LandContinentalness.CODEC,
                Codec.unboundedMap(
                    Erosion.CODEC,
                    Codec.unboundedMap(
                        Temperature.CODEC,
                        Codec.unboundedMap(
                            LandHumidity.CODEC,
                            Codec.unboundedMap(
                                Weirdness.CODEC,
                                Biome.CODEC.listOf()
                            )
                        )
                    )
                )
            ).fieldOf("land").forGetter(bs -> bs._landDef),

            Codec.unboundedMap(
                CaveDepth.CODEC,
                Codec.unboundedMap(
                    Continentalness.CODEC,
                    Codec.unboundedMap(
                        Erosion.CODEC,
                        Codec.unboundedMap(
                            Temperature.CODEC,
                            Codec.unboundedMap(
                                Humidity.CODEC,
                                Biome.CODEC.listOf()
                            )
                        )
                    )
                )
            ).fieldOf("cave").forGetter(bs -> bs._caveDef)

        ).apply(instance, MultiNoiseDiscreteBiomeSource::new));

    private final Map<Temperature, Map<OceanContinentalness, List<Holder<Biome>>>> _oceanDef;
    private final Map<Temperature, List<Holder<Biome>>> _exoticDef;
    private final Map<LandContinentalness, Map<Erosion, Map<Temperature, Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>>>> _landDef;
    private final Map<CaveDepth, Map<Continentalness, Map<Erosion, Map<Temperature, Map<Humidity, @Nullable List<Holder<Biome>>>>>>> _caveDef;
    // endregion Codec

    private int _regionSize;
    private int _biomeSize;
    private BiomePlacementMode _landPlacementMode;

    private final Set<Holder<Biome>> _possibleBiomes = new HashSet<>();
    private final Holder<Biome>[][][] _oceanBiomes;
    private final Holder<Biome>[][] _exoticBiomes;
    /**
     * [LandContinentalness][Erosion][Temperature][LandHumidity][Weirdness][Region].
     * The region array may contain null values in w = 6 ('river_override'),
     * representing no river override.
     */
    private final Holder<Biome>[][][][][][] _landBiomes;
    /**
     * [CaveDepth][Continentalness][Erosion][Temperature][Humidity][Region].
     * <p>
     * The humidity array may contain null values, representing no cave biomes
     * available for that set of parameters.
     * </p>
     */
    private final Holder<Biome>[][][][]@Nullable[][] _caveBiomes;

    private final VoronoiMap _regionNoise;
    private final VoronoiMap _biomeMap;

    private final Long2ObjectMap<Holder<Biome>> _existingOrigins;

    public MultiNoiseDiscreteBiomeSource (
        int regionSize,
        int biomeSize,
        Map<Temperature, Map<OceanContinentalness, List<Holder<Biome>>>> oceanDef,
        Map<Temperature, List<Holder<Biome>>> exoticDef,
        BiomePlacementMode landPlacementMode,
        Map<LandContinentalness, Map<Erosion, Map<Temperature, Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>>>> landDef,
        Map<CaveDepth, Map<Continentalness, Map<Erosion, Map<Temperature, Map<Humidity, @Nullable List<Holder<Biome>>>>>>> caveDef
    ) {
        _regionSize = regionSize;
        _biomeSize = biomeSize;
        _oceanDef = oceanDef;
        _exoticDef = exoticDef;
        _landPlacementMode = landPlacementMode;
        _landDef = landDef;
        _caveDef = caveDef;

        long seed = 6622L * 0x9E3779B97F4A7C15L;
        seed ^= seed >>> 32;

        _regionNoise = new VoronoiMap((int)seed, regionSize);
        _regionNoise.setWarpScale(0.02f);
        _regionNoise.setWarpStrength(50f);
        _biomeMap = new VoronoiMap((int)seed, biomeSize);

        _oceanBiomes = buildOceanBiomeArray(oceanDef);
        _exoticBiomes = buildExoticBiomeArray(exoticDef);
        _landBiomes = buildLandBiomeArray(landDef);
        _caveBiomes = buildCaveBiomeArray(caveDef);

        Long2ObjectMap<Holder<Biome>> origins = new Long2ObjectOpenHashMap<>();
        _existingOrigins = Long2ObjectMaps.synchronize(origins);
    }

    @Override
    protected Codec<? extends BiomeSource> codec () {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes () {
        return _possibleBiomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome (
        int x, int y, int z, Climate.Sampler sampler
    ) {
        var target = sampler.sample(x, y, z);

        var d = target.depth();
        var c = target.continentalness();
        var e = target.erosion();
        var t = target.temperature();
        var h = target.humidity();
        var w = target.weirdness();
        var r = getRegion(x, z);

        // Cave biomes.
        if (d > TerrainParams.DEPTH_SHALLOW) {
            var biome = getCaveOrNull(d, c, e, t, h, r);
            if (biome != null) return biome;
        }

        if (isRiver(w, c, e)) {
            switch (_landPlacementMode) {
                case TERRAIN: return getRiver(c, e, t, h, r);
                case VORONOI: return getRiverVoronoi(sampler, x, z);
                case MIXED: return getRiverVoronoi(sampler, x, z);
            }
        }
        if (c < TerrainParams.CONT_DEEP_OCEAN) return getExotic(t, r);
        if (c < TerrainParams.CONT_COAST) return getOcean(t, c, r);

        switch (_landPlacementMode) {
            case TERRAIN: return getLand(c, e, t, h, w, r);
            case VORONOI: return getLandVoronoi(sampler, x, z);
            case MIXED: return getLandMixed(sampler, x, z, c, w);
        }

        throw new IllegalStateException("Unhandled land placement mode.");
    }

    private double getRegion (int x, int z) {
        double n = _regionNoise.getValue(x, z);
        return (n * 0.5) + 0.5;
    }

    private boolean isRiver (long weirdness, long continentalness, long erosion) {
        if (weirdness < RIVER_WEIRDNESS_MIN) return false;
        if (weirdness > RIVER_WEIRDNESS_MAX) return false;

        // for -0.375 < e < 0.55, true if -0.19 < c.
        if (RIVER_EROSION_LOW < erosion && erosion < RIVER_EROSION_HIGH) {
            return continentalness > RIVER_CONT_OCEAN;
        }
        // for e < -0.375, true if -0.19 < c < -0.03.
        if (erosion < RIVER_EROSION_LOW) {
            return RIVER_CONT_OCEAN < continentalness
                && continentalness < RIVER_CONT_INLAND;
        }

        // for 0.55 < e, true if -0.19 < c < -0.11.
        return RIVER_CONT_OCEAN < continentalness
            && continentalness < RIVER_CONT_COAST;
    }

    private Holder<Biome> getRiver (
        long continentalness,
        long erosion,
        long temperature,
        long humidity,
        double region
    ) {
        var landArr = _landBiomes
            [TerrainParams.landContinentalnessLevel(continentalness)]
            [TerrainParams.erosionLevel(erosion)]
            [TerrainParams.temperatureLevel(temperature)]
            [TerrainParams.landHumidityLevel(humidity)]
            [6]; // river_override

        return landArr[getBiomeFromRegion(region, landArr.length)];

        //var riverArr = _riverBiomes[temperatureLevel(temperature)]
        //    [landHumidityLevel(humidity)];
        //return riverArr[getBiomeFromRegion(region, riverArr.length)];
    }

    private Holder<Biome> getRiverVoronoi (Climate.Sampler sampler, int x, int z) {
        int[] center = new int[2];
        _biomeMap.getCellOrigin(x, z, center);

        int xi = center[0];
        int zi = center[1];

        var target = sampler.sample(xi, 256, zi); // TODO: No hardcode.
        var c = target.continentalness();
        var e = target.erosion();
        var t = target.temperature();
        var h = target.humidity();
        var r = getRegion(xi, zi);

        var arr = _landBiomes
            [TerrainParams.landContinentalnessLevel(c)]
            [TerrainParams.erosionLevel(e)]
            [TerrainParams.temperatureLevel(t)]
            [TerrainParams.landHumidityLevel(h)]
            [6]; // river_override

        return arr[getBiomeFromRegion(r, arr.length)];
    }

    private Holder<Biome> getExotic (long temperature, double region) {
        var arr = _exoticBiomes[TerrainParams.temperatureLevel(temperature)];
        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private Holder<Biome> getOcean (long temperature, long depth, double region) {
        var arr = _oceanBiomes
            [TerrainParams.temperatureLevel(temperature)]
            [TerrainParams.oceanContinentalnessLevel(depth)];

        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private Holder<Biome> getLand (
        long continentalness,
        long erosion,
        long temperature,
        long humidity,
        long weirdness,
        double region
    ) {
        var arr = _landBiomes
            [TerrainParams.landContinentalnessLevel(continentalness)]
            [TerrainParams.erosionLevel(erosion)]
            [TerrainParams.temperatureLevel(temperature)]
            [TerrainParams.landHumidityLevel(humidity)]
            [TerrainParams.weirdnessLevel(weirdness)];

        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private Holder<Biome> getLandVoronoi (Climate.Sampler sampler, int x, int z) {
        int[] center = new int[2];
        _biomeMap.getCellOrigin(x, z, center);

        int xi = center[0];
        int zi = center[1];

        long lcenter = ((long)xi << 32) | (zi & 0xffffffffL);

        var biome = _existingOrigins.get(lcenter);
        if (biome != null) {
            return biome;
        }

        var target = sampler.sample(xi, 256, zi); // TODO: No hardcode.
        var c = target.continentalness();
        var e = target.erosion();
        var t = target.temperature();
        var h = target.humidity();
        var w = target.weirdness();
        var r = getRegion(xi, zi);

        biome = getLand(c, e, t, h, w, r);
        _existingOrigins.putIfAbsent(lcenter, biome);

        return biome;
    }

    private Holder<Biome> getLandMixed (
        Climate.Sampler sampler, int x, int z, long c, long w
    ) {
        int[] center = new int[2];
        _biomeMap.getCellOrigin(x, z, center);

        int xi = center[0];
        int zi = center[1];

        var target = sampler.sample(xi, 256, zi); // TODO: No hardcode.
        var e = target.erosion();
        var t = target.temperature();
        var h = target.humidity();
        var r = getRegion(xi, zi);

        var biome = getLand(c, e, t, h, w, r);

        return biome;
    }

    private @Nullable Holder<Biome> getCaveOrNull (
        long depth,
        long continentalness,
        long erosion,
        long temperature,
        long humidity,
        double region
    ) {
        var arr = _caveBiomes
            [TerrainParams.depthLevel(depth)]
            [TerrainParams.continentalnessLevel(continentalness)]
            [TerrainParams.erosionLevel(erosion)]
            [TerrainParams.temperatureLevel(temperature)]
            [TerrainParams.humidityLevel(humidity)];

        if (arr == null) return null;
        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private int getBiomeFromRegion (double region, int count) {
        return Math.min(Math.max((int)(region * count), 0), count - 1);
    }

    // region Parameter levels
    // endregion Parameter levels

    // region Decode
    private <TKey extends Enum<TKey>, TValue> TValue[] unwrap (Map<TKey, TValue> map, TKey[] values, IntFunction<TValue[]> arrayFactory) {
        var arr = arrayFactory.apply(values.length);

        for (var v : values) {
            if (map.containsKey(v)) {
                arr[v.ordinal()] = map.get(v);
            }
            else {
                if (v.ordinal() == 0) throw new IllegalArgumentException(
                    "First value in collection must be defined."
                );

                arr[v.ordinal()] = arr[v.ordinal() - 1];
            }
        }

        return arr;
    }

    private Holder<Biome>[][][] buildOceanBiomeArray (Map<Temperature, Map<OceanContinentalness, List<Holder<Biome>>>> riverDef) {
        var tempValues = Temperature.values();
        var depthValues = OceanContinentalness.values();

        Holder<Biome>[][][] arr = new Holder[tempValues.length][depthValues.length][];

        Map<OceanContinentalness, List<Holder<Biome>>>[] temperatures
            = unwrap(riverDef, tempValues, Map[]::new);

        for (int t = 0; t < tempValues.length; t++) {
            var temp = temperatures[t];

            List<Holder<Biome>>[] depths = unwrap(temp, depthValues, List[]::new);

            for (int d = 0; d < depths.length; d++) {
                List<Holder<Biome>> biomes = depths[d];

                arr[t][d] = biomes.toArray(Holder[]::new);

                _possibleBiomes.addAll(biomes);
            }

        }

        return arr;
    }

    private Holder<Biome>[][] buildExoticBiomeArray (Map<Temperature, List<Holder<Biome>>> exoticDef) {
        var tempValues = Temperature.values();

        Holder<Biome>[][] arr = new Holder[tempValues.length][];

        for (int t = 0; t < tempValues.length; t++) {
            List<Holder<Biome>> biomes = exoticDef.get(tempValues[t]);
            arr[t] = biomes.toArray(Holder[]::new);

            _possibleBiomes.addAll(biomes);
        }

        return arr;
    }

    private Holder<Biome>[][][][][][] buildLandBiomeArray (
        Map<LandContinentalness, Map<Erosion, Map<Temperature, Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>>>> landDef
    ) {
        var contValues = LandContinentalness.values();
        var erosionValues = Erosion.values();
        var tempValues = Temperature.values();
        var humValues = LandHumidity.values();
        var weirdValues = Weirdness.values();

        Holder<Biome>[][][][][][] arr = new Holder
            [contValues.length]
            [erosionValues.length]
            [tempValues.length]
            [humValues.length]
            [weirdValues.length]
            [];

        Map<Erosion, Map<Temperature, Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>>>[] continentals
            = unwrap(landDef, contValues, Map[]::new);

        for (int c = 0; c < contValues.length; c++) {
            Map<Temperature, Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>>[] erosions
                = unwrap(continentals[c], erosionValues, Map[]::new);

            for (int e = 0; e < erosionValues.length; e++) {
                Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>[] temps
                    = unwrap(erosions[e], tempValues, Map[]::new);

                for (int t = 0; t < tempValues.length; t++) {
                    Map<Weirdness, List<Holder<Biome>>>[] humidities
                        = unwrap(temps[t], humValues, Map[]::new);

                    for (int h = 0; h < humValues.length; h++) {
                        List<Holder<Biome>>[] weirdnesses
                            = unwrap(humidities[h], weirdValues, List[]::new);

                        for (int w = 0; w < weirdValues.length; w++) {
                            arr[c][e][t][h][w] = weirdnesses[w].toArray(Holder[]::new);

                            _possibleBiomes.addAll(weirdnesses[w]);
                        }
                    }
                }
            }
        }

        return arr;
    }

    private Holder<Biome>[][][][]@Nullable[][] buildCaveBiomeArray (
        Map<CaveDepth, Map<Continentalness, Map<Erosion, Map<Temperature, Map<Humidity, @Nullable List<Holder<Biome>>>>>>> caveDef
    ) {
        var depthValues = CaveDepth.values();
        var contValues = Continentalness.values();
        var erosionValues = Erosion.values();
        var tempValues = Temperature.values();
        var humValues = Humidity.values();

        Holder<Biome>[][][][]@Nullable[][] arr = new Holder
            [depthValues.length]
            [contValues.length]
            [erosionValues.length]
            [tempValues.length]
            [humValues.length]
            [];

        Map<Continentalness, Map<Erosion, Map<Temperature, Map<Humidity, List<Holder<Biome>>>>>>[] depths
            = unwrap(caveDef, depthValues, Map[]::new);

        for (int d = 0; d < depthValues.length; d++) {
            Map<Erosion, Map<Temperature, Map<Humidity, List<Holder<Biome>>>>>[] conts
                = unwrap(depths[d], contValues, Map[]::new);

            for (int c = 0; c < contValues.length; c++) {
                Map<Temperature, Map<Humidity, List<Holder<Biome>>>>[] erosions
                    = unwrap(conts[c], erosionValues, Map[]::new);

                for (int e = 0; e < erosionValues.length; e++) {
                    Map<Humidity, List<Holder<Biome>>>[] temperatures
                        = unwrap(erosions[e], tempValues, Map[]::new);

                    for (int t = 0; t < tempValues.length; t++) {
                        List<Holder<Biome>>[] humidities
                            = unwrap(temperatures[t], humValues, List[]::new);

                        for (int h = 0; h < humValues.length; h++) {
                            if (humidities[h].size() == 0) {
                                arr[d][c][e][t][h] = null;
                            }
                            else {
                                arr[d][c][e][t][h] = humidities[h].toArray(Holder[]::new);
                            }

                            _possibleBiomes.addAll(humidities[h]);
                        }
                    }
                }
            }
        }

        return arr;
    }
    // endregion Decode

    @Override
    public void addDebugInfo (
        List<String> info, BlockPos pos, Climate.Sampler sampler
    ) {
        var point = sampler.sample(
            QuartPos.fromBlock(pos.getX()),
            QuartPos.fromBlock(pos.getY()),
            QuartPos.fromBlock(pos.getZ())
        );

        int c = TerrainParams.continentalnessLevel(point.continentalness());
        int e = TerrainParams.erosionLevel(point.erosion());
        int t = TerrainParams.temperatureLevel(point.temperature());
        int h = TerrainParams.humidityLevel(point.humidity());
        int w = TerrainParams.weirdnessLevel(point.weirdness());

        var ec = Continentalness.parse(c);
        var ee = Erosion.parse(e);
        var et = Temperature.parse(t);
        var eh = Humidity.parse(h);
        var ew = Weirdness.parse(w);

        info.add(
            String.format("Biome builder: C: %s, E: %s, T: %s, H: %s, W: %s",
                ec != null ? ec.toString() : c,
                ee != null ? ee.toString() : e,
                et != null ? et.toString() : t,
                eh != null ? eh.toString() : h,
                ew != null ? ew.toString() : w
            )
        );

        info.add(
            String.format("Biome placement mode: Land: %s",
                _landPlacementMode.toString()
            )
        );
    }
}
