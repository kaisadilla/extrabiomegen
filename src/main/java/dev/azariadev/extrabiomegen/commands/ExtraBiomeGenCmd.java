package dev.azariadev.extrabiomegen.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class ExtraBiomeGenCmd {
    public static void register (CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            // /extrabiomegen
            Commands.literal("extrabiomegen")
                .requires(src -> src.hasPermission(2))

                // biomemap <x> <y> <z> <scale> [<name>]
                .then(Commands.literal("biomemap")
                .then(Commands.argument("x", IntegerArgumentType.integer())
                .then(Commands.argument("y", IntegerArgumentType.integer())
                .then(Commands.argument("z", IntegerArgumentType.integer())
                .then(Commands.argument("scale", IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        return executeBiomeMap(ctx, "unnamed");
                    })
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");

                            return executeBiomeMap(ctx, name);
                        })
                    )
                )))))

                // dump possible_biomes
                .then(Commands.literal("dump")
                    .then(Commands.literal("possible_biomes")
                        .executes(ExtraBiomeGenCmd::executeDumpBiomes)
                    )
                )
        );
    }

    private static int executeBiomeMap (
        CommandContext<CommandSourceStack> ctx, String name
    ) {
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        int scale = IntegerArgumentType.getInteger(ctx, "scale");

        var src = ctx.getSource();
        var level = src.getLevel();
        var gen = level.getChunkSource().getGenerator();
        var randomState = level.getChunkSource().randomState();

        src.sendSuccess(() -> Component.literal("Generating PNG map..."), false);

        CompletableFuture.runAsync(() -> {
            try {
                long start = System.nanoTime();

                generateBiomeImage(level, gen, randomState, x, y, z, scale, name);

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
    }

    private static int executeDumpBiomes (CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var level = src.getLevel();
        var gen = level.getChunkSource().getGenerator();
        var registry = level.registryAccess().registryOrThrow(Registries.BIOME);

        for (var biome : gen.getBiomeSource().possibleBiomes()) {
            var key = registry.getKey(biome.value());
            src.sendSuccess(() -> Component.literal(" - " + key.toString()), false);
        }

        return 1;
    }

    private static void generateBiomeImage (
        ServerLevel level,
        ChunkGenerator generator,
        RandomState randomState,
        int xCenter,
        int y,
        int zCenter,
        int scale,
        String name
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

                int xq = QuartPos.fromBlock(xWorld);
                int yq = QuartPos.fromBlock(y);
                int zq = QuartPos.fromBlock(zWorld);

                var biome = biomeSrc.getNoiseBiome(xq, yq, zq, randomState.sampler());
                var biomeId = registry.getKey(biome.value());

                int color = BiomeColorRegistry.getColor(biomeId);

                img.setRGB(dx, dz, color);
            }
        }

        long seed = level.getSeed();

        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        var timestamp = LocalDateTime.now().format(formatter);

        var root = level.getServer().getServerDirectory().toPath();
        var outDir = root.resolve("extrabiomegen");
        Files.createDirectories(outDir);

        var out = outDir.resolve(
            name + "." + seed + "." + xCenter + "." + y + "." + zCenter + "."
                + scale + "." + timestamp + ".png"
        );

        ImageIO.write(img, "png", out.toFile());
    }
}
