package dev.azariadev.extrabiomegen.registries;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class BiomeColorReloadListener extends SimpleJsonResourceReloadListener {
    public BiomeColorReloadListener () {
        super(new Gson(), "biomes");
    }

    @Override
    protected void apply (
        Map<ResourceLocation, JsonElement> objects,
        ResourceManager resourceMgr,
        ProfilerFiller profiler
    ) {
        BiomeColorRegistry.clear();

        for (var entry : objects.entrySet()) {
            var el = entry.getValue();
            if (!el.isJsonArray()) {
                System.out.println("Skipped biome color file.");
                continue;
            }

            var arr = el.getAsJsonArray();

            for (var biomeEl : arr) {
                var obj = biomeEl.getAsJsonObject();
                var id = ResourceLocation.parse(obj.get("id").getAsString());

                var hex = obj.get("color").getAsString();
                int color = parseHexColor(hex);

                BiomeColorRegistry.put(id, color);
            }
        }

        System.out.println("Loaded " + objects.size() + " biome color files.");
    }

    private int parseHexColor (String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        return Integer.parseInt(hex, 16);
    }
}
