package com.voidrealms.sellwands.events;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.utils.TextUtils;
import com.voidrealms.sellwands.SellWands;
import com.voidrealms.sellwands.commands.SellWandsCommand;
import com.voidrealms.sellwands.wands.Wand;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockInteractEvent implements Listener {
	
	private SellWands plugin;	
	
	public BlockInteractEvent(SellWands plugin) {
		this.plugin = plugin;
	}

	
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((event.getAction() != Action.RIGHT_CLICK_BLOCK)
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasLore())
            return;

        ItemStack wandItem =  player.getItemInHand();

        String name = TextUtils.convertFromInvisibleString(wandItem.getItemMeta().getDisplayName());

        if (!TextUtils.convertFromInvisibleString(name).startsWith("SELLWAND")) return;
        Wand wand = plugin.getWandManager().getWand(wandItem);

        event.setCancelled(true);
        Block block = event.getClickedBlock();
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getInventory();
            if (!player.hasPermission("sellwands.use")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.no-permission")));
                return;
            }
            if (plugin.playersCooldown.containsKey(player.getName())) {
                player.sendMessage(ChatColor
                        .translateAlternateColorCodes('&',
                                plugin.getConfig().getString("messages.cooldown"))
                        .replace("%seconds%", "" + plugin.playersCooldown.get(player.getName())));
                return;
            }
            int slot = 0;
            double totalSale = 0.0;
            NumberFormat format = NumberFormat.getInstance();
            format.setGroupingUsed(true);

            // Items that we are selling.
            HashMap<CompatibleMaterial, SoldItem> items = new HashMap<>();

            // Loop through all the inventory items.
            for (ItemStack chestItem : inventory) {
                if (chestItem == null || chestItem.getType().equals(Material.AIR)) {
                    slot++;
                    continue;
                }
                // Get the compatible material for this item.
                CompatibleMaterial material = CompatibleMaterial.getMaterial(chestItem);

                // Is this item sellable?
                if (plugin.prices.containsKey(material)) {
                    // Get the item price.
                    double singleSale = plugin.prices.get(material);

                    // Remove the item from the inventory.
                    chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

                    // Add the price of this item to the total sale.
                    totalSale += (singleSale * chestItem.getAmount() * plugin.priceMultiplier);

                    // Declare the value of the item.
                    double itemValue = singleSale * chestItem.getAmount() * plugin.priceMultiplier;

                    // Add the item to the map.
                    if (items.containsKey(material))
                        items.get(material).addAmount(chestItem.getAmount()).addTotal(itemValue);
                    else
                        items.put(material, new SoldItem(material, chestItem.getAmount(), itemValue));
                }
                slot++;
            }
            if (items.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.item-use-empty")));
                return;
            }
            if (EconomyManager.deposit(player, totalSale)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.sale"))
                        .replace("%amount%", format.format(totalSale) + ""));
                if (plugin.getConfig().getBoolean("sale-breakdown")) {
                    for (SoldItem soldItem : items.values()) {
                        player.sendMessage(ChatColor
                                .translateAlternateColorCodes('&',
                                        plugin.getConfig().getString("messages.sale-breakdown"))
                                .replace("%amount%", String.valueOf(soldItem.getAmount()))
                                .replace("%item%",
                                        StringUtils.capitaliseAllWords(
                                                soldItem.getMaterial().name()
														.toLowerCase().replace("_", " ")))
                                .replace("%price%", format.format(soldItem.getTotal())));
                    }
                }

                if (wand.use() == 0) {
                    player.setItemInHand(null);
                    CompatibleSound.ENTITY_ITEM_BREAK.play(player);
                } else {
                    player.setItemInHand(wand.asItemStack());
                }

            } else {
                System.out.println("[SellWands] Transaction has failed for Inventory Sale (player: "
                        + player.getName() + " amount: " + totalSale + ")");
            }
            if (plugin.getConfig().getInt("cooldown") > 0) {
                plugin.playersCooldown.put(player.getName(),
                        plugin.getConfig().getInt("cooldown"));
            }
        }
    }

    private static class SoldItem {

		private final CompatibleMaterial material;
        private int amount;
        private double total;

		public SoldItem(CompatibleMaterial material, int amount, double total) {
			this.material = material;
			this.amount = amount;
			this.total = total;
		}

		public CompatibleMaterial getMaterial() {
			return material;
		}

		public int getAmount() {
            return amount;
        }

        public double getTotal() {
            return total;
        }

        public SoldItem addTotal(double amount) {
            total += amount;
            return this;
        }

        public SoldItem addAmount(double amount) {
            amount += amount;
            return this;
        }
    }

}
