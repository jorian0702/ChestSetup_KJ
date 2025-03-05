package com.kj.chestsetup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChestSetupCommand implements CommandExecutor, TabCompleter {
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
        
        // コマンド引数のチェック
        if (args.length == 0) {
            player.sendMessage("使用方法: /chestsetup <set|remove|removeall> [チェスト名]");
            return true;
        }
        
        // サブコマンドの処理
        String subCommand = args[0].toLowerCase();
        
        // ターゲットしたチェストのみ削除
        if (subCommand.equals("remove")) {
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
        if (subCommand.equals("removeall")) {
            for (Location loc : plugin.getActiveChests().keySet()) {
                loc.getBlock().setType(Material.AIR);
            }
            plugin.getActiveChests().clear();
            player.sendMessage("全てのチェストを削除しました。");
            return true;
        }
        
        // チェスト設置コマンド (/chestsetup set <チェスト名>) の処理
        if (subCommand.equals("set")) {
            if (args.length != 2) {
                player.sendMessage("使用方法: /chestsetup set <チェスト名>");
                return true;
            }
            
            String chestName = args[1];
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
        
        // 不明なサブコマンド
        player.sendMessage("使用方法: /chestsetup <set|remove|removeall> [チェスト名]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            // サブコマンドを追加
            completions.add("set");
            completions.add("remove");
            completions.add("removeall");
            
            // 入力された文字列でフィルタリング
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // set サブコマンドの後にチェスト名を補完
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            ConfigurationSection chestsSection = plugin.getConfig().getConfigurationSection("chests");
            if (chestsSection != null) {
                return chestsSection.getKeys(false).stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return Collections.emptyList();
    }
}
