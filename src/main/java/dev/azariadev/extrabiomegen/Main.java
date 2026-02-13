package dev.azariadev.extrabiomegen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import dev.azariadev.extrabiomegen.commands.ExtraBiomeGenCmd;
import dev.azariadev.extrabiomegen.registries.BiomeColorReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@net.minecraftforge.fml.common.Mod(Main.MODID)
public class Main  {
    public static final String MODID = "extrabiomegen";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Main (FMLJavaModLoadingContext ctx) {
        IEventBus modEventBus = ctx.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        BiomeSources.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);

        ctx.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup (final FMLCommonSetupEvent evt) {

    }

    @SubscribeEvent
    public void onServerStarting (ServerStartingEvent evt) {

    }

    @SubscribeEvent
    public void onRegisterCommands (RegisterCommandsEvent evt) {
        ExtraBiomeGenCmd.register(evt.getDispatcher());
    }

    public void addReloadListeners (AddReloadListenerEvent evt) {
        evt.addListener(new BiomeColorReloadListener());
    }
}
