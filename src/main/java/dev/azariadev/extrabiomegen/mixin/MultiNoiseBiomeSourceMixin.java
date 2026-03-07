package dev.azariadev.extrabiomegen.mixin;

import dev.azariadev.extrabiomegen.biomesources.TerrainParams;
import dev.azariadev.extrabiomegen.biomesources.parameters.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {

    @Inject(at = @At("HEAD"), method = "addDebugInfo")
    public void addDebugInfo(
        List<String> info, BlockPos pos, Climate.Sampler sampler, CallbackInfo ci
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
    }
}
