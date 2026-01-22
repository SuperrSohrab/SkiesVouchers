package com.itzacat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Listens for voucher item usage (right-click)
 */
public class VoucherListener implements Listener {
    private final Plugin plugin;
    private final VoucherManager voucherManager;
    private final PlayerDataManager playerDataManager;
    private final NamespacedKey voucherKey;
    
    public VoucherListener(Plugin plugin, VoucherManager voucherManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.voucherManager = voucherManager;
        this.playerDataManager = playerDataManager;
        this.voucherKey = voucherManager.getVoucherKey();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoucherUse(PlayerInteractEvent event) {
        // Only main hand right-clicks
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String voucherId = data.get(voucherKey, PersistentDataType.STRING);
        if (voucherId == null) return;
        
        event.setCancelled(true);
        Player player = event.getPlayer();
        
        if (!player.hasPermission("skiesvouchers.redeem")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use vouchers.");
            return;
        }
        
        Voucher voucher = voucherManager.getVoucher(voucherId);
        if (voucher == null) {
            player.sendMessage(ChatColor.RED + "This voucher is no longer valid.");
            return;
        }
        
        // Check max uses
        if (voucher.hasMaxUses()) {
            int currentUses = playerDataManager.getUsageCount(player.getUniqueId(), voucherId);
            if (currentUses >= voucher.getMaxUses()) {
                player.sendMessage(ChatColor.RED + "You have already used this voucher the maximum number of times!");
                return;
            }
        }
        
        // Check cooldown
        if (voucher.hasCooldown() && playerDataManager.isOnCooldown(player.getUniqueId(), voucherId)) {
            long remaining = playerDataManager.getRemainingCooldown(player.getUniqueId(), voucherId);
            player.sendMessage(ChatColor.RED + "This voucher is on cooldown! Time remaining: " + formatTime(remaining));
            return;
        }
        
        // Execute commands
        for (String cmd : voucher.getCommands()) {
            String processedCmd = processCommand(cmd, player);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
        }
        
        // Update usage and cooldown
        playerDataManager.incrementUsage(player.getUniqueId(), voucherId);
        if (voucher.hasCooldown()) {
            long expiryTime = System.currentTimeMillis() + (voucher.getCooldown() * 1000);
            playerDataManager.setCooldown(player.getUniqueId(), voucherId, expiryTime);
        }
        
        // Consume one voucher item
        int amount = item.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(amount - 1);
            player.getInventory().setItemInMainHand(item);
        }
        
        player.sendMessage(ChatColor.GREEN + "Successfully redeemed voucher: " + voucher.getName());
    }
    
    /**
     * Processes command placeholders
     */
    private String processCommand(String command, Player player) {
        return command
            .replace("[player]", player.getName())
            .replace("[uuid]", player.getUniqueId().toString())
            .replace("[world]", player.getWorld().getName())
            .replace("[x]", String.valueOf(player.getLocation().getBlockX()))
            .replace("[y]", String.valueOf(player.getLocation().getBlockY()))
            .replace("[z]", String.valueOf(player.getLocation().getBlockZ()));
    }
    
    /**
     * Formats time in seconds to a readable format
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + " hour" + (hours != 1 ? "s" : "") +
                   (minutes > 0 ? " " + minutes + " minute" + (minutes != 1 ? "s" : "") : "");
        }
    }
}
