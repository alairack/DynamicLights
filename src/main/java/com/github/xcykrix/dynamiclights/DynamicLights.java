package com.github.xcykrix.dynamiclights;

import com.github.xcykrix.dynamiclights.module.DynamicLightCommand;
import com.github.xcykrix.dynamiclights.util.LightManager;
import com.github.xcykrix.dynamiclights.util.LightSources;
import com.github.xcykrix.plugincommon.PluginCommon;
import com.github.xcykrix.plugincommon.api.records.Resource;
import org.bukkit.entity.Player;

public final class DynamicLights extends PluginCommon {
    private final LightSources lightSources = new LightSources();

    @Override
    public void initialize() {
        // Register Configurations
        this.configurationAPI.register(new Resource("config.yml", null, this.getResource("config.yml")));

        // Initialize Light Manager
        LightManager lightManager = new LightManager(this, this.lightSources);

        // Register Commands
        this.commandAPI.register(new DynamicLightCommand(this, lightManager));

        // Register Current Players for Tracking.
        for (Player player : this.getServer().getOnlinePlayers()) {
            lightManager.addPlayer(player);
        }
    }

    @Override
    public void shutdown() {
    }
}