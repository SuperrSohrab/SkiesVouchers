package com.itzacat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data including voucher usage counts and cooldowns
 */
public class PlayerDataManager {
    private final Plugin plugin;
    private final File dataFile;
    private FileConfiguration data;
    
    // Cache for player data
    private final Map<UUID, Map<String, Integer>> usageCache = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldownCache = new HashMap<>();
    
    public PlayerDataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }
    
    /**
     * Loads player data from file
     */
    public void loadData() {
        if (!dataFile.exists()) {
            data = new YamlConfiguration();
            saveData();
        } else {
            data = YamlConfiguration.loadConfiguration(dataFile);
        }
        
        // Load cache from file
        usageCache.clear();
        cooldownCache.clear();
        
        if (data.contains("players")) {
            for (String uuidStr : data.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                
                // Load usage counts
                if (data.contains("players." + uuidStr + ".usage")) {
                    Map<String, Integer> usage = new HashMap<>();
                    for (String voucherId : data.getConfigurationSection("players." + uuidStr + ".usage").getKeys(false)) {
                        usage.put(voucherId, data.getInt("players." + uuidStr + ".usage." + voucherId));
                    }
                    usageCache.put(uuid, usage);
                }
                
                // Load cooldowns
                if (data.contains("players." + uuidStr + ".cooldowns")) {
                    Map<String, Long> cooldowns = new HashMap<>();
                    for (String voucherId : data.getConfigurationSection("players." + uuidStr + ".cooldowns").getKeys(false)) {
                        cooldowns.put(voucherId, data.getLong("players." + uuidStr + ".cooldowns." + voucherId));
                    }
                    cooldownCache.put(uuid, cooldowns);
                }
            }
        }
    }
    
    /**
     * Saves player data to file
     */
    public void saveData() {
        // Save usage cache
        for (Map.Entry<UUID, Map<String, Integer>> entry : usageCache.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Integer> usage : entry.getValue().entrySet()) {
                data.set("players." + uuidStr + ".usage." + usage.getKey(), usage.getValue());
            }
        }
        
        // Save cooldown cache
        for (Map.Entry<UUID, Map<String, Long>> entry : cooldownCache.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Long> cooldown : entry.getValue().entrySet()) {
                data.set("players." + uuidStr + ".cooldowns." + cooldown.getKey(), cooldown.getValue());
            }
        }
        
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }
    
    /**
     * Gets the usage count for a player's voucher
     */
    public int getUsageCount(UUID player, String voucherId) {
        return usageCache.getOrDefault(player, new HashMap<>()).getOrDefault(voucherId, 0);
    }
    
    /**
     * Increments the usage count for a player's voucher
     */
    public void incrementUsage(UUID player, String voucherId) {
        usageCache.computeIfAbsent(player, k -> new HashMap<>())
                  .merge(voucherId, 1, Integer::sum);
        saveData();
    }
    
    /**
     * Gets the cooldown expiry time for a player's voucher
     */
    public long getCooldownExpiry(UUID player, String voucherId) {
        return cooldownCache.getOrDefault(player, new HashMap<>()).getOrDefault(voucherId, 0L);
    }
    
    /**
     * Sets the cooldown for a player's voucher
     */
    public void setCooldown(UUID player, String voucherId, long expiryTime) {
        cooldownCache.computeIfAbsent(player, k -> new HashMap<>())
                     .put(voucherId, expiryTime);
        saveData();
    }
    
    /**
     * Checks if a player is on cooldown for a voucher
     */
    public boolean isOnCooldown(UUID player, String voucherId) {
        long expiry = getCooldownExpiry(player, voucherId);
        return expiry > System.currentTimeMillis();
    }
    
    /**
     * Gets remaining cooldown time in seconds
     */
    public long getRemainingCooldown(UUID player, String voucherId) {
        long expiry = getCooldownExpiry(player, voucherId);
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
}
