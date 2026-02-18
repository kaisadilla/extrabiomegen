package dev.azariadev.extrabiomegen.mixin.terrablender;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrablender.api.RegionType;
import terrablender.util.LevelUtils;
import terrablender.worldgen.IExtendedNoiseGeneratorSettings;

@Pseudo
@Mixin(targets = "terrablender.util.LevelUtils", remap = false)
public abstract class LevelUtilsMixin {
    // Long story short: some mods use TerraBlender to define surface rules for
    // their biomes. These surface rules are then collected by TerraBlender and
    // injected into world generation. This is done in
    // "MixinNoiseGeneratorSettings", and this requires the noise generator to
    // have a custom field "regionType" be a value other than null.
    // Terrablender will fill this value in LevelUtils::initializeBiomes, but
    // will only do so when the biome source is MultiNoiseBiomeSource (vanilla's
    // default biome source, and remarkably not this mod's biome source).
    // As a result, surface rules for TerraBlender biomes simply don't apply
    // with this mod.
    //
    // To fix this, this mixin (which only runs if TerraBlender is installed)
    // will set the noise generator's region type regardless of the biome
    // source's type.
    @Inject(
        method = "initializeBiomes",
        at = @At("HEAD"),
        remap = false
    )
    private static void initializeBiomes (
        RegistryAccess registryAccess,
        Holder<DimensionType> dimensionType,
        ResourceKey<LevelStem> levelResourceKey,
        ChunkGenerator chunkGenerator,
        long seed,
        CallbackInfo ci
    ) {
        var regionType = LevelUtils.getRegionTypeForDimension(dimensionType);
        var chunkGen = (NoiseBasedChunkGenerator)chunkGenerator;
        var genSettings = chunkGen.generatorSettings().value();
        ((IExtendedNoiseGeneratorSettings)(Object)genSettings).setRegionType(regionType);
    }
}
