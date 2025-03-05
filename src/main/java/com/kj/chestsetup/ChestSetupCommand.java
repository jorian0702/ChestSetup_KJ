package com.kj.chestsetup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChestSetupCommand implements CommandExecutor {
    private final ChestSetup plugin;
    
    public ChestSetupCommand(ChestSetup plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // OP権限がない場合は実行不可
        if (!sender.isOp()) {
            sender.sendMessage("このコマンドはOP専用です。");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます。");
            return true;
        }
        
        Player player = (Player) sender;
        
        // サブコマンドの処理
        if (args.length == 1) {
            // ターゲットしたチェストのみ削除
            if (args[0].equalsIgnoreCase("remove")) {
                Block targetBlock = player.getTargetBlockExact(10);
                if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
                    player.sendMessage("ターゲットブロックがチェストではありません。");
                    return true;
                }
                Location loc = targetBlock.getLocation();
                if (plugin.getActiveChests().containsKey(loc)) {
                    targetBlock.setType(Material.AIR);
                    plugin.getActiveChests().remove(loc);
                    player.sendMessage("対象のチェストを削除しました。");
                } else {
                    player.sendMessage("ターゲットしたチェストは管理対象のチェストではありません。");
                }
                return true;
            }
            
            // 管理している全チェストを削除
            if (args[0].equalsIgnoreCase("removeall")) {
                for (Location loc : plugin.getActiveChests().keySet()) {
                    loc.getBlock().setType(Material.AIR);
                }
                plugin.getActiveChests().clear();
                player.sendMessage("全てのチェストを削除しました。");
                return true;
            }
        }
        
        // チェスト設置コマンド (/chestsetup <チェスト名>) の処理
        if (args.length != 1) {
            player.sendMessage("使用方法: /chestsetup <チェスト名|remove|removeall>");
            return true;
        }
        
        String chestName = args[0];
        // config.ymlに該当のチェスト設定が存在するかチェック
        if (plugin.getConfig().getConfigurationSection("chests." + chestName) == null) {
            player.sendMessage("Configに '" + chestName + "' の設定が見つかりません。");
            return true;
        }
        
        // プレイヤーの視線先（最大10ブロック先）のブロックを取得
        Block targetBlock = player.getTargetBlockExact(10);
        if (targetBlock == null) {
            player.sendMessage("ターゲットブロックが見つかりません。");
            return true;
        }
        
        // ターゲットブロックの上にチェストを設置
        Location chestLocation = targetBlock.getLocation().add(0, 1, 0);
        chestLocation.getBlock().setType(Material.CHEST);
        
        // config.yml の設定でチェストにアイテムをセット
        ChestUtil.fillChest(plugin, chestLocation, chestName);
        
        // 管理対象として記録
        plugin.getActiveChests().put(chestLocation.getBlock().getLocation(), chestName);
        plugin.getBaseLocations().put(chestName, chestLocation);
        
        player.sendMessage("チェスト '" + chestName + "' を設置しました。");
        return true;
    }
}
