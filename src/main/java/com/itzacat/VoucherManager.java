package com.itzacat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages voucher configuration and retrieval
 */
public class VoucherManager {
    private final Map<String, Voucher> vouchers = new HashMap<>();
    private final Plugin plugin;
    private final NamespacedKey voucherKey;
    
    public VoucherManager(Plugin plugin) {
        this.plugin = plugin;
        this.voucherKey = new NamespacedKey(plugin, "voucher-id");
    }
    
    /**
     * Loads all vouchers from the config.yml file
     */
    public void loadVouchers() {
        vouchers.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection vouchersSection = config.getConfigurationSection("vouchers");
        
        if (vouchersSection == null) {
            plugin.getLogger().warning("No vouchers section found in config.yml");
            return;
        }
        
        for (String key : vouchersSection.getKeys(false)) {
            ConfigurationSection voucherSection = vouchersSection.getConfigurationSection(key);
            if (voucherSection == null) continue;
            
            String name = voucherSection.getString("name", key);
            String id = voucherSection.getString("id", key);
            List<String> commands = voucherSection.getStringList("commands");
            long cooldown = voucherSection.getLong("cooldown", 0);
            int maxUses = voucherSection.getInt("max-uses", 0);

            // Item settings (accept both nested `item:` block or flat keys)
            ConfigurationSection itemSection = voucherSection.isConfigurationSection("item")
                ? voucherSection.getConfigurationSection("item")
                : voucherSection;

            String materialName = itemSection.getString("material", "PAPER");
            Material material = resolveMaterial(materialName);
            if (material == null) {
                material = Material.PAPER;
                plugin.getLogger().warning("Invalid material '" + materialName + "' for voucher " + id + ", defaulting to PAPER");
            }

            String itemName = itemSection.getString("name", name);
            List<String> itemLore = itemSection.getStringList("lore");
            boolean glow = itemSection.getBoolean("glow", false);

            itemName = translate(itemName);
            List<String> coloredLore = new ArrayList<>();
            for (String loreLine : itemLore) {
                coloredLore.add(translate(loreLine));
            }
            
            Voucher voucher = new Voucher(name, id, commands, cooldown, maxUses, material, itemName, coloredLore, glow);
            vouchers.put(id.toLowerCase(), voucher);
            
            plugin.getLogger().info("Loaded voucher: " + name + " (ID: " + id + ")");
        }
        
        plugin.getLogger().info("Loaded " + vouchers.size() + " vouchers");
    }
    
    /**
     * Gets a voucher by its ID
     */
    public Voucher getVoucher(String id) {
        return vouchers.get(id.toLowerCase());
    }
    
    /**
     * Gets all loaded vouchers
     */
    public Map<String, Voucher> getAllVouchers() {
        return new HashMap<>(vouchers);
    }

    /**
     * Creates the configured ItemStack for a voucher
     */
    public ItemStack createVoucherItem(Voucher voucher) {
        ItemStack item = new ItemStack(voucher.getItemMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = voucher.getItemName() != null ? voucher.getItemName() : translate(voucher.getName());
            meta.setDisplayName(displayName);
            meta.setLore(voucher.getItemLore());
            meta.getPersistentDataContainer().set(voucherKey, PersistentDataType.STRING, voucher.getId());
            if (voucher.shouldGlow()) {
                meta.addEnchant(Enchantment.LUCK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    public NamespacedKey getVoucherKey() {
        return voucherKey;
    }

    private String translate(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    // Tries multiple resolution strategies to support lower/upper case material names
    private Material resolveMaterial(String name) {
        if (name == null) return null;
        Material mat = Material.matchMaterial(name);
        if (mat != null) return mat;
        mat = Material.matchMaterial(name.toUpperCase());
        if (mat != null) return mat;
        return Material.getMaterial(name.toUpperCase());
    }
}
