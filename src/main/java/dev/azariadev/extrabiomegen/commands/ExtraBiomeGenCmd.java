package dev.azariadev.extrabiomegen.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.azariadev.extrabiomegen.registries.BiomeColorRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class ExtraBiomeGenCmd {
    public static void register (CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("extrabiomegen")
                .requires(src -> src.hasPermission(2))
                .then(
                    Commands.literal("biomemap")
                        .then(
                            Commands.argument("x", IntegerArgumentType.integer())
                                .then(
                                    Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                            int z = IntegerArgumentType.getInteger(ctx, "z");

                                            return executeBiomeMap(ctx.getSource(), x, z, 1);
                                        })
                                        .then(Commands.argument("scale", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {
                                                int x = IntegerArgumentType.getInteger(ctx, "x");
                                                int z = IntegerArgumentType.getInteger(ctx, "z");
                                                int scale = IntegerArgumentType.getInteger(ctx, "scale");

                                                return executeBiomeMap(ctx.getSource(), x, z, scale);
                                            })
                                        )
                                )
                        )
                )
        );
    }

    private static int executeBiomeMap (
        CommandSourceStack src, int x, int z, int scale
    ) {
        var level = src.getLevel();
        var gen = level.getChunkSource().getGenerator();
        var randomState = level.getChunkSource().randomState();

        src.sendSuccess(() -> Component.literal("Generating PNG map..."), false);

        CompletableFuture.runAsync(() -> {
            try {
                long start = System.nanoTime();

                generateBiomeImage(level, gen, randomState, x, z, scale);

                long end = System.nanoTime();
                long ms = (end - start) / 1_000_000;

                src.sendSuccess(() -> Component.literal(
                    "Completed in " + ms + " ms."
                ), false);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return 1;

        //var biome = biomeSrc.getNoiseBiome(
        //    quartX, quartY, quartZ, level.getChunkSource().randomState().sampler()
        //);
//
        //var biomeId = level.registryAccess()
        //    .registryOrThrow(Registries.BIOME)
        //    .getKey(biome.value());
//
        //src.sendSuccess(() -> Component.literal(
        //    "Biome at your position (calculated): " + biomeId
        //), false);
//
        //return 1;
    }

    private static void generateBiomeImage (
        ServerLevel level,
        ChunkGenerator generator,
        RandomState randomState,
        int xCenter,
        int zCenter,
        int scale
    ) throws Exception {
        var biomeSrc = generator.getBiomeSource();

        int size = 2500;
        int half = size / 2;

        var img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        var registry = level.registryAccess().registryOrThrow(Registries.BIOME);

        for (int dz = 0; dz < size; dz++) {
            for (int dx = 0; dx < size; dx++) {
                int xWorld = xCenter + ((dx - half) * scale);
                int zWorld = zCenter + ((dz - half) * scale);
                int yWorld = 256;

                int xq = QuartPos.fromBlock(xWorld);
                int yq = QuartPos.fromBlock(yWorld);
                int zq = QuartPos.fromBlock(zWorld);

                var biome = biomeSrc.getNoiseBiome(xq, yq, zq, randomState.sampler());
                var biomeId = registry.getKey(biome.value());

                int color;
                if (biomeId != null && biomeId.equals(Biomes.OCEAN.location())) {
                    color = 0x0000ff;
                }
                else {
                    color = BiomeColorRegistry.getColor(biomeId);
                }

                img.setRGB(dx, dz, color);
            }
        }

        long seed = level.getSeed();

        var out = level.getServer().getServerDirectory().toPath().resolve(
            "biome_map." + seed + "." + xCenter + ", " + zCenter + ".png"
        );
        ImageIO.write(img, "png", out.toFile());
    }
}
