package com.kj.chestsetup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestListener implements Listener {
    private final ChestSetup plugin;
    
    public ChestListener(ChestSetup plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // インベントリのホルダーがチェストの場合のみ
        if (!(event.getInventory().getHolder() instanceof Chest)) {
            return;
        }
        
        Chest chest = (Chest) event.getInventory().getHolder();
        Block block = chest.getBlock();
        Location loc = block.getLocation();
        
        // 管理対象のチェストかどうかチェック
        if (plugin.getActiveChests().containsKey(loc)) {
            // チェスト内の残りアイテムをドロップ
            Inventory inv = event.getInventory();
            for (ItemStack item : inv.getContents()) {
                if (item != null) {
                    block.getWorld().dropItemNaturally(loc, item);
                }
            }
            
            // チェストブロックを消去
            block.setType(Material.AIR);
            
            // 管理対象から削除
            plugin.getActiveChests().remove(loc);
        }
    }
}
