package dev.azariadev.extrabiomegen.biomesources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.azariadev.extrabiomegen.biomesources.parameters.*;
import dev.azariadev.extrabiomegen.noise.VoronoiMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class VoronoiBiomeSource extends BiomeSource {
    private static final int REGION_SCALE = 400;
    private static final int BIOME_SCALE = 50;

    // region Thresholds
    private static final long RIVER_WEIRDNESS_MIN = Climate.quantizeCoord(-0.05f);
    private static final long RIVER_WEIRDNESS_MAX = Climate.quantizeCoord(0.05f);
    private static final long RIVER_CONT_OCEAN = Climate.quantizeCoord(-0.19f);
    private static final long RIVER_CONT_COAST = Climate.quantizeCoord(-0.11f);
    private static final long RIVER_CONT_INLAND = Climate.quantizeCoord(-0.03f);
    private static final long RIVER_EROSION_LOW = Climate.quantizeCoord(-0.375f);
    private static final long RIVER_EROSION_HIGH = Climate.quantizeCoord(0.55f);

    private static final long DEPTH_SHALLOW = Climate.quantizeCoord(0.1f);
    private static final long DEPTH_NORMAL = Climate.quantizeCoord(0.55f);
    private static final long DEPTH_DEEP = Climate.quantizeCoord(1f);
    private static final long DEPTH_VERY_DEEP = Climate.quantizeCoord(1.15f);

    private static final long CONT_DEEP_OCEAN = Climate.quantizeCoord(-1.05f);
    private static final long CONT_SHALLOW_OCEAN = Climate.quantizeCoord(-0.455f);
    private static final long CONT_COAST = Climate.quantizeCoord(-0.19f);
    private static final long CONT_LOWLAND = Climate.quantizeCoord(-0.11f);
    private static final long CONT_HIGHLAND = Climate.quantizeCoord(0.03f);
    private static final long CONT_INTERIOR = Climate.quantizeCoord(0.3f);
    private static final long CONT_DEEP_INTERIOR = Climate.quantizeCoord(0.7f);

    private static final long EROSION_RUGGED = Climate.quantizeCoord(-0.7799f);
    private static final long EROSION_CRAGGY = Climate.quantizeCoord(-0.375f);
    private static final long EROSION_NORMAL = Climate.quantizeCoord(-0.2225f);
    private static final long EROSION_ROLLING = Climate.quantizeCoord(0.05f);
    private static final long EROSION_SMOOTH = Climate.quantizeCoord(0.45f);
    private static final long EROSION_FLAT = Climate.quantizeCoord(0.55f);

    private static final long TEMP_COLD = Climate.quantizeCoord(-0.45f);
    private static final long TEMP_NORMAL = Climate.quantizeCoord(-0.15f);
    private static final long TEMP_WARM = Climate.quantizeCoord(0.2f);
    private static final long TEMP_HOT = Climate.quantizeCoord(0.55f);

    private static final long HUMIDITY_DRY = Climate.quantizeCoord(-0.35f);
    private static final long HUMIDITY_NORMAL = Climate.quantizeCoord(-0.1f);
    private static final long HUMIDITY_WET = Climate.quantizeCoord(0.1f);
    private static final long HUMIDITY_HUMID = Climate.quantizeCoord(0.3f);
    private static final long HUMIDITY_LUSH = Climate.quantizeCoord(0.5f);

    private static final long WEIRD_NORMAL_OUTER_SLOPE = Climate.quantizeCoord(-0.9333f);
    private static final long WEIRD_NORMAL_OUTER_PEAK = Climate.quantizeCoord(-0.7666f);
    private static final long WEIRD_NORMAL_INNER_SLOPE = Climate.quantizeCoord(-0.5666f);
    private static final long WEIRD_NORMAL_INNER_VALLEY = Climate.quantizeCoord(-0.4f);
    private static final long WEIRD_NORMAL_RIVER_BANK = Climate.quantizeCoord(-0.2666f);
    private static final long WEIRD_VAR_RIVER_BANK = Climate.quantizeCoord(0f);
    private static final long WEIRD_VAR_INNER_VALLEY = Climate.quantizeCoord(0.2666f);
    private static final long WEIRD_VAR_INNER_SLOPE = Climate.quantizeCoord(0.4f);
    private static final long WEIRD_VAR_OUTER_PEAK = Climate.quantizeCoord(0.5666f);
    private static final long WEIRD_VAR_OUTER_SLOPE = Climate.quantizeCoord(0.7666f);
    private static final long WEIRD_VAR_OUTER_VALLEY = Climate.quantizeCoord(0.9333f);
    // endregion Thresholds

    private static final long BEACH_CONT_MIN = Climate.quantizeCoord(-0.19f);
    private static final long BEACH_CONT_MAX = Climate.quantizeCoord(-0.11f);

    private static final long MIN_EROSION = Climate.quantizeCoord(-0.5f);

    // region Codec
    public static final Codec<VoronoiBiomeSource> CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(
                Temperature.CODEC,
                Codec.unboundedMap(
                    LandHumidity.CODEC,
                    Biome.CODEC.listOf()
                )
            ).fieldOf("river").forGetter(bs -> bs._riverDef),

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

        ).apply(instance, VoronoiBiomeSource::new));

    private final Map<Temperature, Map<LandHumidity, List<Holder<Biome>>>> _riverDef;
    private final Map<Temperature, Map<OceanContinentalness, List<Holder<Biome>>>> _oceanDef;
    private final Map<Temperature, List<Holder<Biome>>> _exoticDef;
    private final Map<LandContinentalness, Map<Erosion, Map<Temperature, Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>>>> _landDef;
    private final Map<CaveDepth, Map<Continentalness, Map<Erosion, Map<Temperature, Map<Humidity, @Nullable List<Holder<Biome>>>>>>> _caveDef;
    // endregion Codec

    private final Set<Holder<Biome>> _possibleBiomes = new HashSet<>();
    private final Holder<Biome>[][][] _riverBiomes;
    private final Holder<Biome>[][][] _oceanBiomes;
    private final Holder<Biome>[][] _exoticBiomes;
    private final Holder<Biome>[][][][][][] _landBiomes;
    private final Holder<Biome>[][][][]@Nullable[][] _caveBiomes;

    //private final FastNoiseLite _regionNoise;
    private final VoronoiMap _regionNoise;
    private final VoronoiMap _biomeMap;

    private final Long2ObjectMap<Holder<Biome>> _existingOrigins2;
    private final Long2ObjectMap<Holder<Biome>> _existingOrigins;

    public VoronoiBiomeSource (
        Map<Temperature, Map<LandHumidity, List<Holder<Biome>>>> riverDef,
        Map<Temperature, Map<OceanContinentalness, List<Holder<Biome>>>> oceanDef,
        Map<Temperature, List<Holder<Biome>>> exoticDef,
        Map<LandContinentalness, Map<Erosion, Map<Temperature, Map<LandHumidity, Map<Weirdness, List<Holder<Biome>>>>>>> landDef,
        Map<CaveDepth, Map<Continentalness, Map<Erosion, Map<Temperature, Map<Humidity, @Nullable List<Holder<Biome>>>>>>> caveDef
    ) {
        _riverDef = riverDef;
        _oceanDef = oceanDef;
        _exoticDef = exoticDef;
        _landDef = landDef;
        _caveDef = caveDef;

        long seed = 6622L * 0x9E3779B97F4A7C15L;
        seed ^= seed >>> 32;

        _regionNoise = new VoronoiMap((int)seed, REGION_SCALE);
        _regionNoise.setWarpScale(0.02f);
        _regionNoise.setWarpStrength(50f);
        _biomeMap = new VoronoiMap((int)seed, BIOME_SCALE);

        _riverBiomes = buildRiverBiomeArray(riverDef);
        _oceanBiomes = buildOceanBiomeArray(oceanDef);
        _exoticBiomes = buildExoticBiomeArray(exoticDef);
        _landBiomes = buildLandBiomeArray(landDef);
        _caveBiomes = buildCaveBiomeArray(caveDef);

        _existingOrigins2 = new Long2ObjectOpenHashMap<>();
        _existingOrigins = Long2ObjectMaps.synchronize(_existingOrigins2);
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
        if (d > DEPTH_SHALLOW) {
            var biome = getCaveOrNull(d, c, e, t, h, r);
            if (biome != null) return biome;
        }

        if (isRiver(w, c, e)) return getRiver(t, h, r);
        if (c < CONT_DEEP_OCEAN) return getExotic(t, r);
        if (c < CONT_COAST) return getOcean(t, c, r);

        return getLand(x, z, sampler);
        //return getLand(c, e, t, h, w, r);
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

    private Holder<Biome> getRiver (long temperature, long humidity, double region) {
        var arr = _riverBiomes[temperatureLevel(temperature)][landHumidityLevel(humidity)];
        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private Holder<Biome> getExotic (long temperature, double region) {
        var arr = _exoticBiomes[temperatureLevel(temperature)];
        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private Holder<Biome> getOcean (long temperature, long depth, double region) {
        var arr = _oceanBiomes[temperatureLevel(temperature)][oceanContinentalnessLevel(depth)];
        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private Holder<Biome> getLand (int x, int z, Climate.Sampler sampler) {
        int[] center = new int[2];
        _biomeMap.getCellOrigin(x, z, center);

        int xi = (int)center[0];
        int zi = (int)center[1];

        //Random rng = new Random(xi + (zi * 100000));
        //return _possibleBiomes.stream().skip(rng.nextInt(_possibleBiomes.size())).findFirst().orElseThrow();

        long lcenter = ((long) xi << 32) | (zi & 0xffffffffL);

        var biome = _existingOrigins.get(lcenter);
        if (biome != null) {
            return biome;
        }

        var target = sampler.sample(xi, 256, zi); // TODO: No hardcode.
        var d = target.depth();
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

    private Holder<Biome> getLand (
        long continentalness,
        long erosion,
        long temperature,
        long humidity,
        long weirdness,
        double region
    ) {
        var arr = _landBiomes
            [landContinentalnessLevel(continentalness)]
            [erosionLevel(erosion)]
            [temperatureLevel(temperature)]
            [landHumidityLevel(humidity)]
            [weirdnessLevel(weirdness)];

        return arr[getBiomeFromRegion(region, arr.length)];
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
            [depthLevel(depth)]
            [continentalnessLevel(continentalness)]
            [erosionLevel(erosion)]
            [temperatureLevel(temperature)]
            [humidityLevel(humidity)];

        if (arr == null) return null;
        return arr[getBiomeFromRegion(region, arr.length)];
    }

    private int getBiomeFromRegion (double region, int count) {
        return Math.min(Math.max((int)(region * count), 0), count - 1);
    }

    // region Parameter levels
    private int depthLevel (long depth) {
        if (depth < DEPTH_NORMAL) return 0;
        if (depth < DEPTH_DEEP) return 1;
        if (depth < DEPTH_VERY_DEEP) return 2;
        return 3;
    }

    private int continentalnessLevel (long continentalness) {
        if (continentalness < CONT_DEEP_OCEAN) return 0;
        if (continentalness < CONT_SHALLOW_OCEAN) return 1;
        if (continentalness < CONT_COAST) return 2;
        if (continentalness < CONT_LOWLAND) return 3;
        if (continentalness < CONT_HIGHLAND) return 4;
        if (continentalness < CONT_INTERIOR) return 5;
        if (continentalness < CONT_DEEP_INTERIOR) return 6;
        return 7;
    }

    private int oceanContinentalnessLevel (long continentalness) {
        if (continentalness < CONT_SHALLOW_OCEAN) return 1;
        return 0;
    }

    private int landContinentalnessLevel (long continentalness) {
        if (continentalness < CONT_LOWLAND) return 0;
        if (continentalness < CONT_HIGHLAND) return 1;
        if (continentalness < CONT_INTERIOR) return 2;
        return 3;
    }

    private int erosionLevel (long erosion) {
        if (erosion < EROSION_RUGGED) return 0;
        if (erosion < EROSION_CRAGGY) return 1;
        if (erosion < EROSION_NORMAL) return 2;
        if (erosion < EROSION_ROLLING) return 3;
        if (erosion < EROSION_SMOOTH) return 4;
        if (erosion < EROSION_FLAT) return 5;
        return 6;
    }

    private int temperatureLevel (long temperature) {
        if (temperature < TEMP_COLD) return 0;
        if (temperature < TEMP_NORMAL) return 1;
        if (temperature < TEMP_WARM) return 2;
        if (temperature < TEMP_HOT) return 3;
        return 4;
    }

    private int humidityLevel (long humidity) {
        if (humidity < HUMIDITY_DRY) return 0;
        if (humidity < HUMIDITY_NORMAL) return 1;
        if (humidity < HUMIDITY_WET) return 2;
        if (humidity < HUMIDITY_HUMID) return 3;
        if (humidity < HUMIDITY_LUSH) return 4;
        return 5;
    }

    private int landHumidityLevel (long humidity) {
        if (humidity < HUMIDITY_DRY) return 0;
        if (humidity < HUMIDITY_NORMAL) return 1;
        if (humidity < HUMIDITY_WET) return 2;
        if (humidity < HUMIDITY_HUMID) return 3;
        return 4;
    }

    private int weirdnessLevel (long weirdness) {
        if (weirdness < WEIRD_NORMAL_OUTER_SLOPE) return 0;
        if (weirdness < WEIRD_NORMAL_OUTER_PEAK) return 1;
        if (weirdness < WEIRD_NORMAL_INNER_SLOPE) return 2;
        if (weirdness < WEIRD_NORMAL_INNER_VALLEY) return 3;
        if (weirdness < WEIRD_NORMAL_RIVER_BANK) return 4;
        if (weirdness < WEIRD_VAR_RIVER_BANK) return 5;
        if (weirdness < WEIRD_VAR_INNER_VALLEY) return 6;
        if (weirdness < WEIRD_VAR_INNER_SLOPE) return 7;
        if (weirdness < WEIRD_VAR_OUTER_PEAK) return 8;
        if (weirdness < WEIRD_VAR_OUTER_SLOPE) return 9;
        if (weirdness < WEIRD_VAR_OUTER_VALLEY) return 10;
        return 11;
    }
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

    private Holder<Biome>[][][] buildRiverBiomeArray (Map<Temperature, Map<LandHumidity, List<Holder<Biome>>>> riverDef) {
        var tempValues = Temperature.values();
        var humValues = LandHumidity.values();

        Holder<Biome>[][][] arr = new Holder[tempValues.length][humValues.length][];

        Map<LandHumidity, List<Holder<Biome>>>[] temperatures
            = unwrap(riverDef, tempValues, Map[]::new);

        for (int t = 0; t < tempValues.length; t++) {
            var temp = temperatures[t];

            List<Holder<Biome>>[] humidities = unwrap(temp, humValues, List[]::new);

            for (int h = 0; h < humidities.length; h++) {
                List<Holder<Biome>> biomes = humidities[h];

                arr[t][h] = biomes.toArray(Holder[]::new);

                _possibleBiomes.addAll(biomes);
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
}
