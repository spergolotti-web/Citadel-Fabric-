package com.github.alexthe666.citadel.compat.terrablender;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.server.generation.SurfaceRulesManager;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.lang.reflect.Method;
import java.util.Map;

public class TerrablenderCompat {

    public static void setup() {
        try {
            Class<?> surfaceRuleManager = Class.forName("terrablender.api.SurfaceRuleManager");
            Object overworld = getEnum(surfaceRuleManager, "RuleCategory", "OVERWORLD");
            Object beforeBedrock = getEnum(surfaceRuleManager, "RuleStage", "BEFORE_BEDROCK");

            Map<String, SurfaceRules.RuleSource> vanillaBiomeRules = SurfaceRulesManager.getOverworldRulesByBiomeForTerrablender(true);
            Method addToDefault = surfaceRuleManager.getMethod("addToDefaultSurfaceRulesAtStage", overworld.getClass().getSuperclass(), beforeBedrock.getClass().getSuperclass(), int.class, SurfaceRules.RuleSource.class);
            for (Map.Entry<String, SurfaceRules.RuleSource> entry : vanillaBiomeRules.entrySet()) {
                addToDefault.invoke(null, overworld, beforeBedrock, 0, entry.getValue());
            }
            Citadel.LOGGER.info("Added {} vanilla biome surface rule types via terrablender", vanillaBiomeRules.size());

            Map<String, SurfaceRules.RuleSource> moddedBiomeRules = SurfaceRulesManager.getOverworldRulesByBiomeForTerrablender(false);
            Method addSurfaceRules = surfaceRuleManager.getMethod("addSurfaceRules", overworld.getClass().getSuperclass(), String.class, SurfaceRules.RuleSource.class);
            for (Map.Entry<String, SurfaceRules.RuleSource> entry : moddedBiomeRules.entrySet()) {
                addSurfaceRules.invoke(null, overworld, entry.getKey(), entry.getValue());
            }
            Citadel.LOGGER.info("Added {} modded biome surface rule types via terrablender", moddedBiomeRules.size());
        } catch (Exception e) {
            Citadel.LOGGER.error("Failed to setup Terrablender compat", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object getEnum(Class<?> apiClass, String innerName, String constant) throws Exception {
        Class<?> inner = Class.forName(apiClass.getName() + "$" + innerName);
        return Enum.valueOf((Class<Enum>) inner, constant);
    }
}
