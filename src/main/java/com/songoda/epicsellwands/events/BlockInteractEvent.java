package com.songoda.epicsellwands.events;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicsellwands.EpicSellWands;
import com.songoda.epicsellwands.player.PlayerManager;
import com.songoda.epicsellwands.settings.Settings;
import com.songoda.epicsellwands.wand.Wand;
import com.songoda.epicsellwands.wand.WandManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.HashMap;

public class BlockInteractEvent implements Listener {
	
	private final EpicSellWands plugin;
	private final PlayerManager playerManager;
	private final WandManager wandManager;
	
	public BlockInteractEvent(EpicSellWands plugin) {
		this.plugin = plugin;
		this.playerManager = plugin.getPlayerManager();
		this.wandManager = plugin.getWandManager();
	}
	
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || !player.getItemInHand().hasItemMeta()
                || !player.getItemInHand().getItemMeta().hasLore())
            return;

        ItemStack wandItem =  player.getItemInHand();

        NBTItem nbtItem = NmsManager.getNbt().of(wandItem);
        if (!nbtItem.has("wand")) return;
        Wand wand = plugin.getWandManager().getWand(wandItem);

        event.setCancelled(true);

        Block block = event.getClickedBlock();
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getInventory();

            if (!player.hasPermission("epicsellwands.use")) {
                plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
                return;
            }

            if (playerManager.hasActiveCooldown(player)) {
                plugin.getLocale().getMessage("event.use.cooldown")
                        .processPlaceholder("seconds",
                                (playerManager.getActiveCooldown(player) - System.currentTimeMillis()) / 1000).sendPrefixedMessage(player);
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
                if (wandManager.isSellable(material)) {
                    // Get the item price.
                    double singleSale = wandManager.getPriceFor(material);

                    // Remove the item from the inventory.
                    chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

                    // Declare the value of the item.
                    double itemValue = singleSale * chestItem.getAmount() * Settings.PRICE_MULTIPLIER.getDouble();

                    // Add the price of this item to the total sale.
                    totalSale += itemValue;

                    // Add the item to the map.
                    if (items.containsKey(material))
                        items.get(material).addAmount(chestItem.getAmount()).addTotal(itemValue);
                    else
                        items.put(material, new SoldItem(material, chestItem.getAmount(), itemValue));
                }
                slot++;
            }
            if (items.isEmpty()) {
                plugin.getLocale().getMessage("event.use.empty").sendPrefixedMessage(player);
                return;
            }
            if (EconomyManager.deposit(player, totalSale)) {
                plugin.getLocale().getMessage("event.use.sale")
                        .processPlaceholder("amount", format.format(totalSale)).sendPrefixedMessage(player);
                if (Settings.SALE_BREAKDOWN.getBoolean()) {
                    for (SoldItem soldItem : items.values()) {
                        if (soldItem == null || soldItem.material == null) continue;
                        plugin.getLocale().getMessage("event.use.breakdown")
                                .processPlaceholder("amount", soldItem.getAmount())
                                .processPlaceholder("item", WordUtils
                                        .capitalizeFully(soldItem.material.name().toLowerCase()
                                                .replace("_", " ")))
                                .processPlaceholder("price", format.format(soldItem.getTotal()))
                                .sendPrefixedMessage(player);
                    }
                }

                int remainingUses = wand.use();
                if (remainingUses == 0) {
                    player.setItemInHand(null);
                    CompatibleSound.ENTITY_ITEM_BREAK.play(player);
                    plugin.getLocale().getMessage("event.use.broken")
                            .sendPrefixedMessage(player);
                } else {
                    player.setItemInHand(wand.asItemStack());
                    plugin.getLocale().getMessage("event.use.left")
                            .processPlaceholder("uses", remainingUses)
                            .sendPrefixedMessage(player);
                }

            } else {
                System.out.println("[EpicSellWands] Transaction has failed for Inventory Sale (player: "
                        + player.getName() + " amount: " + totalSale + ")");
            }

            if (Settings.COOLDOWN.getInt() > 0)
                playerManager.addNewCooldown(player);
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
            this.total += amount;
            return this;
        }

        public SoldItem addAmount(double amount) {
            this.amount += amount;
            return this;
        }
    }

}
