package com.itzacat;

import java.util.List;
import org.bukkit.Material;

/**
 * Represents a voucher with its properties
 */
public class Voucher {
    private final String name;
    private final String id;
    private final List<String> commands;
    private final long cooldown; // in seconds, 0 means no cooldown
    private final int maxUses; // 0 means unlimited
    private final Material itemMaterial;
    private final String itemName;
    private final List<String> itemLore;
    private final boolean glow;
    
    public Voucher(String name, String id, List<String> commands, long cooldown, int maxUses,
                   Material itemMaterial, String itemName, List<String> itemLore, boolean glow) {
        this.name = name;
        this.id = id;
        this.commands = commands;
        this.cooldown = cooldown;
        this.maxUses = maxUses;
        this.itemMaterial = itemMaterial;
        this.itemName = itemName;
        this.itemLore = itemLore;
        this.glow = glow;
    }
    
    public String getName() {
        return name;
    }
    
    public String getId() {
        return id;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public long getCooldown() {
        return cooldown;
    }
    
    public int getMaxUses() {
        return maxUses;
    }
    
    public boolean hasCooldown() {
        return cooldown > 0;
    }
    
    public boolean hasMaxUses() {
        return maxUses > 0;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public String getItemName() {
        return itemName;
    }

    public List<String> getItemLore() {
        return itemLore;
    }

    public boolean shouldGlow() {
        return glow;
    }
}
