package com.itzacat;

import org.bukkit.plugin.java.JavaPlugin;

/*
 * skiesvouchers java plugin
 */
public class Plugin extends JavaPlugin {
  private VoucherManager voucherManager;
  private PlayerDataManager playerDataManager;
  private VoucherCommand voucherCommand;
  private VoucherListener voucherListener;

  @Override
  public void onEnable() {
    // Create data folder if it doesn't exist
    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }
    
    // Save default config
    saveDefaultConfig();
    
    // Initialize managers
    voucherManager = new VoucherManager(this);
    playerDataManager = new PlayerDataManager(this);
    voucherCommand = new VoucherCommand(this, voucherManager, playerDataManager);
    voucherListener = new VoucherListener(this, voucherManager, playerDataManager);
    
    // Load vouchers from config
    voucherManager.loadVouchers();
    
    // Register command
    getCommand("voucher").setExecutor(voucherCommand);
    getCommand("voucher").setTabCompleter(voucherCommand);

    // Register events
    getServer().getPluginManager().registerEvents(voucherListener, this);
    
    getLogger().info("SkiesVouchers has been enabled!");
  }

  @Override
  public void onDisable() {
    // Save player data
    if (playerDataManager != null) {
      playerDataManager.saveData();
    }
    
    getLogger().info("SkiesVouchers has been disabled!");
  }
}
