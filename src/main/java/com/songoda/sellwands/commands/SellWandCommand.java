package com.songoda.sellwands.commands;

import com.songoda.sellwands.SellWands;
import com.songoda.sellwands.wands.Wand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellWandCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("sellwand") && sender instanceof Player) {
            if (sender.hasPermission("sellwands.giveself")) {

                Wand wand = SellWands.getInstance().getWandManager().getWand(args[0]);

                Player target = (Player) sender;
                target.getInventory().addItem(wand.asItemStack());
                target.sendMessage(ChatColor.translateAlternateColorCodes('&', SellWands.getInstance().getConfig().getString("messages.item-receive")));

            }
            return true;
        }
        return false;
    }
}