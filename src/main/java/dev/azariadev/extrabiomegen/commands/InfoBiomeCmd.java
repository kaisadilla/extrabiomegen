package dev.azariadev.extrabiomegen.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;

public class InfoBiomeCmd {
    public static void register (
        CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx
    ) {
        dispatcher.register(
            Commands.literal("extrabiomegen")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("info")
                .then(Commands.literal("biome")
                .then(Commands.argument("biome_id", ResourceOrTagArgument.resourceOrTag(ctx, Registries.BIOME))
                .then(Commands.literal("features")
                    .executes(InfoBiomeCmd::features)
                ))))
        );
    }

    private static int features (CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var src = ctx.getSource();

        var biomeArg = ResourceOrTagArgument.getResourceOrTag(
            ctx, "biome_id", Registries.BIOME
        );

        var either = biomeArg.unwrap();
        ResourceLocation biomeId;

        if (either.right().isPresent()) {
            src.sendFailure(Component.literal("Tags are not supported"));
            return 0;
        }
        else {
            biomeId = either.left().get().key().location();
        }

        var regAccess = src.getServer().registryAccess();
        var biomeReg = regAccess.registryOrThrow(Registries.BIOME);
        var placedReg = regAccess.registryOrThrow(Registries.PLACED_FEATURE);

        var biomeHolder = biomeReg.getHolder(
            ResourceKey.create(Registries.BIOME, biomeId)
        );

        if (biomeHolder.isEmpty()) {
            src.sendFailure(Component.literal("Biome not found: " + biomeId));
            return 0;
        }

        var biome = biomeHolder.get().value();
        var gen = biome.getGenerationSettings();
        var features = gen.features();

        src.sendSuccess(
            () -> Component.literal("=== Features in " + biomeId + " ==="), false
        );

        for (int i = 0; i < features.size(); i++) {
            var i2 = i;
            var step = GenerationStep.Decoration.values()[i];
            var feats = features.get(i);

            src.sendSuccess(
                () -> Component.literal("Step " + i2 + " (" + step.name() + ")"),
                false
            );

            for (var holder : feats) {
                holder.unwrapKey().ifPresent(k -> {
                    var id = k.location();
                    src.sendSuccess(
                        () -> Component.literal(" - " + id.toString()), false
                    );
                });
            }
        }

        return 1;
    }
}
