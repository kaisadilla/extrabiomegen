package dev.azariadev.extrabiomegen;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ExtraBiomeGenMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onLoad (String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig () {
        return null;
    }

    @Override
    public boolean shouldApplyMixin (String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("LevelUtilsMixin")) {
            if (LoadingModList.get().getModFileById("terrablender") != null) {
                LOGGER.info("Injecting mixin into TerraBlender's 'LevelUtils'.");

                return true;
            }

            return false;
        }

        return true;
    }

    @Override
    public void acceptTargets (Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins () {
        return null;
    }

    @Override
    public void preApply (String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply (String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
