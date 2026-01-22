package com.itzacat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Handles voucher commands
 */
public class VoucherCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final VoucherManager voucherManager;
    private final PlayerDataManager playerDataManager;
    
    public VoucherCommand(Plugin plugin, VoucherManager voucherManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.voucherManager = voucherManager;
        this.playerDataManager = playerDataManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /voucher <redeem|give|reload|list>");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "redeem":
                return handleRedeem(sender, args);
            case "give":
                return handleGive(sender, args);
            case "reload":
                return handleReload(sender);
            case "list":
                return handleList(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /voucher <redeem|give|reload|list>");
                return true;
        }
    }
    
    private boolean handleRedeem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can claim voucher items!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /voucher redeem <voucherId>");
            return true;
        }
        
        Player player = (Player) sender;
        String voucherId = args[1];
        
        Voucher voucher = voucherManager.getVoucher(voucherId);
        if (voucher == null) {
            player.sendMessage(ChatColor.RED + "Unknown voucher ID: " + voucherId);
            return true;
        }
        
        ItemStack item = voucherManager.createVoucherItem(voucher);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            // Inventory full, drop the item at player's location
            leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
            player.sendMessage(ChatColor.YELLOW + "Your inventory was full; the voucher was dropped near you.");
        }
        player.sendMessage(ChatColor.GREEN + "You received the voucher item: " + voucher.getName());
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skiesvouchers.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /voucher give <player> <voucherId>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }
        
        String voucherId = args[2];
        Voucher voucher = voucherManager.getVoucher(voucherId);
        if (voucher == null) {
            sender.sendMessage(ChatColor.RED + "Unknown voucher ID: " + voucherId);
            return true;
        }
        
        ItemStack item = voucherManager.createVoucherItem(voucher);
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(stack -> target.getWorld().dropItemNaturally(target.getLocation(), stack));
            target.sendMessage(ChatColor.YELLOW + "Your inventory was full; the voucher was dropped near you.");
        }
        target.sendMessage(ChatColor.GREEN + "You have received a voucher: " + voucher.getName());
        target.sendMessage(ChatColor.YELLOW + "Right-click the voucher to redeem it!");
        sender.sendMessage(ChatColor.GREEN + "Gave voucher " + voucher.getName() + " to " + target.getName());
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("skiesvouchers.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        plugin.reloadConfig();
        voucherManager.loadVouchers();
        playerDataManager.loadData();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (voucherManager.getAllVouchers().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No vouchers are currently loaded.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "Available Vouchers:");
        for (Voucher voucher : voucherManager.getAllVouchers().values()) {
            StringBuilder info = new StringBuilder(ChatColor.YELLOW + "- " + voucher.getName() + ChatColor.GRAY + " (ID: " + voucher.getId() + ")");
            
            if (voucher.hasMaxUses()) {
                info.append(" [Max Uses: ").append(voucher.getMaxUses()).append("]");
            }
            if (voucher.hasCooldown()) {
                info.append(" [Cooldown: ").append(formatTime(voucher.getCooldown())).append("]");
            }
            
            sender.sendMessage(info.toString());
        }
        return true;
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("redeem", "give", "reload", "list"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("redeem") || args[0].equalsIgnoreCase("give")) {
                if (args[0].equalsIgnoreCase("give")) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                } else {
                    completions.addAll(voucherManager.getAllVouchers().keySet());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(voucherManager.getAllVouchers().keySet());
        }
        
        return completions;
    }
}
