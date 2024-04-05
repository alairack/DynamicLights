package com.github.xcykrix.dynamiclights.util;

import com.github.xcykrix.plugincommon.PluginCommon;
import com.github.xcykrix.plugincommon.extendables.Stateful;
import com.github.xcykrix.plugincommon.extendables.implement.Shutdown;
import com.google.common.base.Strings;
import com.shaded._100.org.h2.mvstore.MVMap;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class LightManager extends Stateful implements Shutdown {
    public final LightSources lightSources;
    private final HashMap<String, Location> lastLightLocation = new HashMap<>();
    private final HashMap<UUID, BukkitTask> tasks = new HashMap<>();
    private final HashMap<UUID, BukkitTask> consumeTasks = new HashMap<>();

    private final List<ItemStack> usedItemStacks = new ArrayList<>();

    // Local Database Map
    public final MVMap<String, Boolean> lightLockStatus;

    // Configuration
    private long refresh = 5L;
    private final long consumeRefresh = 1200L; //每分钟刷新一次
    private int distance = 64;

    private double consumption = 1;

    private double torchDurability = this.pluginCommon.configurationAPI.get("config.yml").getInt("torch_durability");

    private double soulTorchDurability = this.pluginCommon.configurationAPI.get("config.yml").getInt("soul_torch_durability");

    public LightManager(PluginCommon pluginCommon, LightSources lightSources) {
        super(pluginCommon);
        this.lightLockStatus = this.pluginCommon.h2MVStoreAPI.getStore().openMap("lightLockStatus");
        this.lightSources = lightSources;

        this.refresh = this.pluginCommon.configurationAPI.get("config.yml").getLong("update-rate");
        this.distance = this.pluginCommon.configurationAPI.get("config.yml").getInt("light-culling-distance");
    }

    @Override
    public void shutdown() {
        synchronized (this.tasks) {
            for (UUID uuid : this.tasks.keySet()) {
                this.tasks.get(uuid).cancel();
            }
            this.tasks.clear();
        }
    }

    public void addPlayer(Player player) {
        synchronized (this.tasks) {
            if (this.tasks.containsKey(player.getUniqueId())) return;
            this.tasks.put(player.getUniqueId(), this.pluginCommon.getServer().getScheduler().runTaskTimerAsynchronously(this.pluginCommon, () -> {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                ItemStack offHand = player.getInventory().getItemInOffHand();


                if (mainHand.getAmount() != 0){
                    NBTItem nbti = new NBTItem(mainHand);
                    if (!nbti.hasTag("lightTime")){
                        if (mainHand.getType().equals(Material.TORCH)) {
                            NBT.modify(mainHand, nbt -> {
                                nbt.setInteger("lightLevel", 11);
                                nbt.setDouble("lightTime", torchDurability);
                                nbt.setDouble("originLightTime", torchDurability);
                            });
                            ItemMeta meta = mainHand.getItemMeta();
                            if (meta != null) {
                                if (meta.getLore() == null){
                                    meta.setLore(Arrays.asList(String.format("剩余照明时间 大概%s分钟", torchDurability)));
                                }
                                else {
                                    List<String> lore = meta.getLore();
                                    lore.set(0, String.format("剩余照明时间 大概%s分钟", torchDurability));
                                    meta.setLore(lore);
                                }
                                mainHand.setItemMeta(meta);
                            }
                        }
                        if (mainHand.getType().equals(Material.SOUL_TORCH)){
                            NBT.modify(mainHand, nbt -> {
                                nbt.setInteger("lightLevel", 11);
                                nbt.setDouble("lightTime", soulTorchDurability);
                                nbt.setDouble("originLightTime", soulTorchDurability);
                            });
                            ItemMeta meta = mainHand.getItemMeta();
                            if (meta != null) {
                                if (meta.getLore() == null){
                                    meta.setLore(Arrays.asList(String.format("剩余照明时间 大概%s分钟", soulTorchDurability)));
                                }
                                else {
                                    List<String> lore = meta.getLore();
                                    lore.set(0, String.format("剩余照明时间 大概%s分钟", soulTorchDurability));
                                    meta.setLore(lore);
                                }
                                mainHand.setItemMeta(meta);
                            }
                        }
                    }
                }

                if (offHand.getAmount() != 0){
                    NBTItem nbti = new NBTItem(offHand);
                    if (!nbti.hasTag("lightTime")){
                        if (offHand.getType().equals(Material.TORCH)) {
                            NBT.modify(offHand, nbt -> {
                                nbt.setInteger("lightLevel", 11);
                                nbt.setDouble("lightTime", torchDurability);
                                nbt.setDouble("originLightTime", torchDurability);
                            });
                            ItemMeta meta = offHand.getItemMeta();
                            if (meta != null) {
                                if (meta.getLore() == null){
                                    meta.setLore(Arrays.asList(String.format("剩余照明时间 大概%s分钟", torchDurability)));
                                }
                                else {
                                    List<String> lore = meta.getLore();
                                    lore.set(0, String.format("剩余照明时间 大概%s分钟", torchDurability));
                                    meta.setLore(lore);
                                }
                                offHand.setItemMeta(meta);
                            }
                        }
                        if (offHand.getType().equals(Material.SOUL_TORCH)){
                            NBT.modify(offHand, nbt -> {
                                nbt.setInteger("lightLevel", 11);
                                nbt.setDouble("lightTime", soulTorchDurability);
                                nbt.setDouble("originLightTime", soulTorchDurability);
                            });
                            ItemMeta meta = offHand.getItemMeta();
                            if (meta != null) {
                                if (meta.getLore() == null){
                                    meta.setLore(Arrays.asList(String.format("剩余照明时间 大概%s分钟", soulTorchDurability)));
                                }
                                else {
                                    List<String> lore = meta.getLore();
                                    lore.set(0, String.format("剩余照明时间 大概%s分钟", soulTorchDurability));
                                    meta.setLore(lore);
                                }
                                offHand.setItemMeta(meta);
                            }
                        }
                    }
                }



                // Check Light Source Validity

                boolean mainHandValid = this.valid(player, mainHand);
                int lightLevel = 0;
                if (mainHandValid) {
                    lightLevel = lightSources.getLightLevel(mainHand, mainHand.getType());
                    if (!this.usedItemStacks.contains(mainHand)){
                        this.usedItemStacks.add(mainHand);
                    }
                }
                boolean offHandValid = this.valid(player, offHand);
                if (offHandValid) {
                    lightLevel = lightSources.getLightLevel(offHand, offHand.getType());
                    if (!this.usedItemStacks.contains(offHand)){
                        this.usedItemStacks.add(offHand);
                    }
                }
                // Deploy Lighting
                for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
                    // Pull Last Location
                    String locationId = player.getUniqueId() + "/" + targetPlayer.getUniqueId();
                    Location lastLocation = this.getLastLocation(locationId);

                    // Test and Remove Old Lights
                    if (!mainHandValid && !offHandValid) {
                        if (lastLocation != null) {
                            this.removeLight(targetPlayer, lastLocation);
                            this.removeLastLocation(locationId);
                        }
                        continue;
                    }

                    // Get the Next Location
                    Location nextLocation = player.getEyeLocation();

                    // Add Light Sources
                    if (lightLevel > 0 && differentLocations(lastLocation, nextLocation)) {
                        if (player.getWorld().getName().equals(targetPlayer.getWorld().getName())) {
                            if (player.getLocation().distance(targetPlayer.getLocation()) <= this.distance) {
                                this.addLight(targetPlayer, nextLocation, lightLevel);
                                this.setLastLocation(locationId, nextLocation);
                            }
                        }
                    }

                    // Remove Last Locations
                    if (lastLocation != null && differentLocations(lastLocation, nextLocation)) {
                        this.removeLight(targetPlayer, lastLocation);
                    }
                }

            }, 2L, refresh));
        }
        synchronized (this.consumeTasks) {
            if (this.consumeTasks.containsKey(player.getUniqueId())) return;
            this.consumeTasks.put(player.getUniqueId(), this.pluginCommon.getServer().getScheduler().runTaskTimerAsynchronously(this.pluginCommon, () -> {
                for (int i =0; i< this.usedItemStacks.size(); i++){
                    ItemStack usedItem = this.usedItemStacks.get(i);
                    try {
                        if (usedItem.getType() != Material.AIR && usedItem.getAmount() != 0){
                            if (!consume(usedItem, player)){
                                usedItem.setAmount(usedItem.getAmount() - 1);
                                this.usedItemStacks.remove(usedItem);
                                i--;
                            }
                        }
                    } catch (NullPointerException ignored){}
                }
            }, 2L, consumeRefresh));
        }
    }


    public boolean consume(ItemStack mainhand, Player player){
        // 计算剩余的耐久度
        NBTItem nbti = new NBTItem(mainhand);
        if (nbti.hasTag("lightTime")) {
            Double lightTime = nbti.getDouble("lightTime");
            lightTime = lightTime - this.consumption;
            Double originLightTime = nbti.getDouble("originLightTime");
            if (lightTime <= 0){

                NBT.modify(mainhand, nbt -> {
                    nbt.setDouble("lightTime", originLightTime);
                });
                ItemMeta meta = mainhand.getItemMeta();
                if (meta != null) {
                    if (meta.getLore() == null){
                        meta.setLore(Arrays.asList(String.format("剩余照明时间 大概%s分钟", originLightTime)));
                    }
                    else {
                        List<String> lore = meta.getLore();
                        lore.set(0, String.format("剩余照明时间 大概%s分钟", originLightTime));
                        meta.setLore(lore);
                    }
                    mainhand.setItemMeta(meta);
                }
                return false;
            }
            else {
                Double finalLightTime = lightTime;
                NBT.modify(mainhand, nbt -> {
                    nbt.setDouble("lightTime", finalLightTime);
                });
                ItemMeta meta = mainhand.getItemMeta();
                if (meta != null) {
                    if (meta.getLore() == null){
                        meta.setLore(Arrays.asList(String.format("剩余照明时间 大概%s分钟", lightTime)));
                    }
                    else {
                        List<String> lore = meta.getLore();
                        lore.set(0, String.format("剩余照明时间 大概%s分钟", lightTime));
                        meta.setLore(lore);
                    }
                    mainhand.setItemMeta(meta);
                }

            }
        }
        return true;
    }

    public void removePlayer(UUID uid) {
        synchronized (this.tasks) {
            if (this.tasks.containsKey(uid)) {
                this.tasks.get(uid).cancel();
                this.tasks.remove(uid);
            }
        }
    }

    public void addLight(Player player, Location location, int lightLevel) {
        if (lightLevel == 0) return;
        Light light = (Light) Material.LIGHT.createBlockData();
        if (location.getWorld() == null) location.setWorld(player.getWorld());
        World world = location.getWorld();
        switch (world.getBlockAt(location).getType()) {
            case AIR, CAVE_AIR -> {
                light.setWaterlogged(false);
                light.setLevel(lightLevel);
            }
            case WATER -> {
                light.setWaterlogged(true);
                light.setLevel(lightLevel - 2);
            }
        }
        player.sendBlockChange(location, light);
    }

    public void removeLight(Player player, Location location) {
        if (location.getWorld() == null) location.setWorld(player.getWorld());
        player.sendBlockChange(location, location.getWorld().getBlockAt(location).getBlockData());
    }

    public boolean valid(Player player, ItemStack itemStack) {
        if (itemStack != null || itemStack.getAmount() != 0)
        {
            Material main = itemStack.getType();
            boolean hasLightLevel = lightSources.hasLightLevel(itemStack);
            if (!hasLightLevel) return false;

            Block currentLocation = player.getEyeLocation().getBlock();
            if (currentLocation.getType() == Material.AIR || currentLocation.getType() == Material.CAVE_AIR) return true;
            if (currentLocation instanceof Waterlogged && ((Waterlogged) currentLocation).isWaterlogged()) {
                return false;
            }
        }
        return false;
    }

    public Location getLastLocation(String uuid) {
        return lastLightLocation.getOrDefault(uuid, null);
    }

    public void setLastLocation(String uuid, Location location) {
        lastLightLocation.put(uuid, location);
    }

    public void removeLastLocation(String uuid) {
        lastLightLocation.remove(uuid);
    }

    private boolean differentLocations(Location l1, Location l2) {
        if (l1 == null || l2 == null) return true;
        if (l1.getWorld() == null || l2.getWorld() == null) return true;
        if (!l1.getWorld().getName().equals(l2.getWorld().getName())) return true;
        return l1.getBlockX() != l2.getBlockX() || l1.getBlockY() != l2.getBlockY() || l1.getBlockZ() != l2.getBlockZ();
    }

    public String getProgressBar(int current, int max, int totalBars, char symbol, ChatColor CompletedColor,
                                 ChatColor notCompletedColor) {
        float percent = (float) current / max;
        int ProgressBars = (int) (totalBars * percent);

        return Strings.repeat("" + CompletedColor + symbol, ProgressBars)
            + Strings.repeat("" + notCompletedColor + symbol, totalBars - ProgressBars);
    }
}
