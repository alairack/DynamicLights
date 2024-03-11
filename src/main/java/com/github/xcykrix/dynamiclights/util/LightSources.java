package com.github.xcykrix.dynamiclights.util;

import com.github.xcykrix.plugincommon.PluginCommon;
import com.github.xcykrix.plugincommon.extendables.Stateful;
import com.shaded._100.dev.dejvokep.boostedyaml.YamlDocument;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LightSources extends Stateful {
    private final HashMap<Material, Integer> levelOfLights = new HashMap<>();
    private final HashSet<Material> submersibleLights = new HashSet<>();
    private final HashSet<Material> protectedLights = new HashSet<>();

    public LightSources(PluginCommon pluginCommon) {
        super(pluginCommon);
        YamlDocument lights = this.pluginCommon.configurationAPI.get("lights.yml");

        // Load Light Levels
        Map<String, Object> levels = lights.getSection("levels").getStringRouteMappedValues(false);
        for (String material : levels.keySet()) {
            try {
                int level = Integer.parseInt(levels.get(material).toString());
                this.levelOfLights.put(Material.valueOf(material), level);
            } catch(Exception exception) {
                this.pluginCommon.getLogger().warning("Unable to register level for '" + material  + "'. " + exception.getMessage());
            }
        }
        this.pluginCommon.getLogger().info("Registered " + this.levelOfLights.size() + " items for Dynamic Lights.");

        // Load Submersible Status
        List<String> submersibles = lights.getStringList("submersibles");
        for (String material : submersibles) {
            try {
                this.submersibleLights.add(Material.valueOf(material));
            } catch(Exception exception) {
                this.pluginCommon.getLogger().warning("Unable to register submersible for '" + material + "'. " + exception.getMessage());
            }
        }
        this.pluginCommon.getLogger().info("Registered " + this.submersibleLights.size() + " items for Dynamic Submersible Lights.");

        // Load Lockable Status
        List<String> lockables = lights.getStringList("lockables");
        for (String material : lockables) {
            try {
                this.protectedLights.add(Material.valueOf(material));
            } catch(Exception exception) {
                this.pluginCommon.getLogger().warning("Unable to register lockable for '" + material + "'. " + exception.getMessage());
            }
        }
        this.pluginCommon.getLogger().info("Registered " + this.protectedLights.size() + " items for Dynamic Lockable Lights.");
    }

    public boolean hasLightLevel(ItemStack item) {
        if (item.getType().equals(Material.AIR) || item.getAmount() == 0){
            return false;
        }
        NBTItem nbti = new NBTItem(item);
        return nbti.hasTag("lightLevel") ||  levelOfLights.containsKey(item.getType());
    }

    public Integer getLightLevel(ItemStack itemStack, Material fallback) {
        Integer lightLevel = 1;
        NBTItem nbti = new NBTItem(itemStack);
        if (nbti.hasTag("lightLevel")) {
            lightLevel = nbti.getInteger("lightLevel");
        }
        else {
            lightLevel = levelOfLights.getOrDefault(itemStack.getType(), levelOfLights.getOrDefault(fallback, 0));
        }
        return lightLevel;
    }

    public boolean isSubmersible(Material offHand, Material mainHand) {
        return submersibleLights.contains(offHand) || submersibleLights.contains(mainHand);
    }

    public boolean isProtectedLight(Material offHand) {
        return protectedLights.contains(offHand);
    }
}
