package dev.azariadev.extrabiomegen;

public class Utils {

    public static int saltSeed (int seed, int salt) {
        int h = seed ^ salt;
        h ^= (h >>> 16);
        h *= 0x7feb352d;
        h ^= (h >>> 15);
        h *= 0x846ca68b;
        h ^= (h >>> 16);
        return h;
    }
}
