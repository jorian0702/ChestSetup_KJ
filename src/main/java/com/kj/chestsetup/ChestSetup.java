package com.kj.chestsetup;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

public class ChestSetup extends JavaPlugin {
    private final Map<Location, String> activeChests = new HashMap<>();
    private final Map<String, Location> baseLocations = new HashMap<>();

    @Override
    public void onEnable() {
        // コンフィグファイルの生成
        saveDefaultConfig();
        
        // コマンドの登録
        getCommand("chestsetup").setExecutor(new ChestSetupCommand(this));
        
        // イベントリスナーの登録
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
    }

    public Map<Location, String> getActiveChests() {
        return activeChests;
    }

    public Map<String, Location> getBaseLocations() {
        return baseLocations;
    }

    public void spawnChest(String chestName, Location baseLoc) {
        // ランダムな位置にチェストを出現させる（基準位置から±5ブロックの範囲）
        int randomX = (int) (Math.random() * 11) - 5;
        int randomZ = (int) (Math.random() * 11) - 5;
        
        Location newLoc = baseLoc.clone().add(randomX, 0, randomZ);
        newLoc.getBlock().setType(org.bukkit.Material.CHEST);
        
        // チェストにアイテムを設定
        ChestUtil.fillChest(this, newLoc, chestName);
        
        // 管理対象として記録
        activeChests.put(newLoc, chestName);
    }
} 