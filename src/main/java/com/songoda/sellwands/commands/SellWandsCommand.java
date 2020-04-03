package com.songoda.sellwands.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.sellwands.wands.Wand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.songoda.sellwands.SellWands;
import net.md_5.bungee.api.ChatColor;

public class SellWandsCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sellwands")) {
			int length = args.length;
			if (length == 0) {
				sender.sendMessage(ChatColor.GREEN + "/sellwands give <player> <type>");
				sender.sendMessage(ChatColor.GREEN + "/sellwands reload");
			}
			if (length == 1) {
				if (sender.hasPermission("sellwands.reload")) {
					if (args[0].equalsIgnoreCase("debug")) {
						SellWands.getInstance().debug = !SellWands.getInstance().debug;
						sender.sendMessage("SellWands debug messages: " + SellWands.getInstance().debug);
						return true;
					}
					SellWands.getInstance().reloadConfig();
					SellWands.getInstance().parseConfigFile();
					SellWands.getInstance().priceMultiplier = SellWands.getInstance().getConfig().getDouble("price-multiplier");
					sender.sendMessage("Reloaded SellWands.");
				}
			}
			if (length > 1) {
				if (sender.hasPermission("sellwands.give")) {
					Player target = Bukkit.getPlayer(args[1]);
					if (target == null) {
						sender.sendMessage(ChatColor.RED + "That player is offline.");
						return true;
					}

					Wand wand = SellWands.getInstance().getWandManager().getWand(args[2]);
					target.getInventory().addItem(wand.asItemStack());
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', SellWands.getInstance().getConfig().getString("messages.item-give")).replace("%player%", target.getName()));
					target.sendMessage(ChatColor.translateAlternateColorCodes('&', SellWands.getInstance().getConfig().getString("messages.item-receive")));
				}
			}
			return true;
		}
		return false;
	}
	
	public static boolean getHideable() {
		String version;
		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
			return false;
		}

		if (version.equals("v1_8_R3")) {
			return true;
		} else if (version.equals("v1_8_R2")) {
			return true;
		} else if (version.equals("v1_8_R1")) {
			return true;
		} else if (version.equals("v1_9_R2")) {
			return true;
		} else if (version.equals("v1_9_R1")) {
			return true;
		} else if (version.equals("v1_10_R1")) {
			return true;
		} else if (version.equals("v1_11_R1")) {
			return true;
		} else {
			return false;
		}

	}
}