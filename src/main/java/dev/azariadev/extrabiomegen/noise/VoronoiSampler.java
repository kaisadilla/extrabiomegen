package dev.azariadev.extrabiomegen.noise;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.joml.Random;

public class VoronoiSampler {
    private static final double WARP_SCALE = 0.10;
    private static final double WARP_STRENGTH = 9;

    private final int _cellSize;
    private final long _seed;

    private final NormalNoise _warpX;
    private final NormalNoise _warpZ;

    public VoronoiSampler (long seed, int cellSize) {
        _seed = seed;
        _cellSize = cellSize;

        _warpX = NormalNoise.create(RandomSource.create(seed ^ 0xA2C2A), 0, 1);
        _warpZ = NormalNoise.create(RandomSource.create(seed ^ 0xB3D4F), 0, 1);
    }

    public long[] getCellOrigin (double x, double z) {
        double warpX = _warpX.getValue(x * WARP_SCALE, z * WARP_SCALE, 0.0);
        double warpZ = _warpZ.getValue(x * WARP_SCALE, z * WARP_SCALE, 0.0);

        x += (warpX * WARP_STRENGTH);
        z += (warpZ * WARP_STRENGTH);

        int gx = floorDiv((int)Math.floor(x), _cellSize);
        int gz = floorDiv((int)Math.floor(z), _cellSize);

        double minDistSq = Double.MAX_VALUE;
        long originX = 0;
        long originZ = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int nx = gx + dx;
                int nz = gz + dz;

                long[] gen = getGeneratorPoint(nx, nz);
                double dx2 = x - gen[0];
                double dz2 = z - gen[1];
                double sq = (dx2 * dx2) + (dz2 * dz2);

                if (sq < minDistSq) {
                    minDistSq = sq;
                    originX = gen[0];
                    originZ = gen[1];
                }
            }
        }

        return new long[] { originX, originZ };
    }

    private int floorDiv (int a, int b) {
        int div = a / b;
        if ((a ^ b) < 0 && a % b != 0) div--;

        return div;
    }

    private long[] getGeneratorPoint (int gx, int gz) {
        long hash = hash(gx, gz, _seed);
        Random rng = new Random(hash);

        double px = (gx * _cellSize) + (rng.nextFloat() * _cellSize);
        double pz = (gz * _cellSize) + (rng.nextFloat() * _cellSize);

        return new long[]{ (long)px, (long)pz };
    }

    private long hash (int x, int z, long seed) {
        long h = seed;
        h ^= Integer.toUnsignedLong(x) * 0x9E3779B97F4A7C15L;
        h ^= Integer.toUnsignedLong(z) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >> 30)) * 0xBF58476D1CE4E5B9L;
        h ^= (h >> 27);
        return h;
    }
}
