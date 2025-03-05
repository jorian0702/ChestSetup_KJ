package com.kj.chestsetup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestUtil {

    /**
     * 指定のLocationにあるチェストに、config.ymlで設定されたアイテムを追加します。
     *
     * @param plugin    プラグインインスタンス
     * @param loc       チェストブロックのLocation
     * @param chestName config.yml内のチェスト名のキー
     */
    public static void fillChest(ChestSetup plugin, Location loc, String chestName) {
        Block block = loc.getBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }
        Chest chest = (Chest) block.getState();
        Inventory inv = chest.getInventory();
        
        // 既存のアイテムをクリア
        inv.clear();
        
        // config.ymlから該当するチェスト設定を取得
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("chests." + chestName);
        if (section == null) {
            plugin.getLogger().warning("Configに '" + chestName + "' の設定が見つかりません。");
            return;
        }
        
        // 設定された各アイテムをチェストに追加
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material != null) {
                int amount = section.getInt(key);
                if (amount > 0) {
                    inv.addItem(new ItemStack(material, amount));
                }
            } else {
                plugin.getLogger().warning("無効な素材名: " + key);
            }
        }
    }
}
