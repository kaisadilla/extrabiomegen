package dev.azariadev.extrabiomegen;

import com.mojang.serialization.Codec;
import dev.azariadev.extrabiomegen.biomesources.VoronoiBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BiomeSources {
    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
        DeferredRegister.create(Registries.BIOME_SOURCE, "extrabiomegen");

    public static final RegistryObject<Codec<? extends BiomeSource>> VORONOI =
        BIOME_SOURCES.register("voronoi", () -> VoronoiBiomeSource.CODEC);

    public static void register (IEventBus bus) {
        BIOME_SOURCES.register(bus);
    }
}