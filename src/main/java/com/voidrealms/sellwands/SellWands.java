package com.voidrealms.sellwands;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.hooks.EconomyManager;
import com.voidrealms.sellwands.commands.SellWandCommand;
import com.voidrealms.sellwands.commands.SellWandsCommand;
import com.voidrealms.sellwands.events.BlockInteractEvent;
import com.voidrealms.sellwands.wands.Wand;
import com.voidrealms.sellwands.wands.WandManager;
import me.mrCookieSlime.Slimefun.Lists.Categories;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellWands extends JavaPlugin {

    private static SellWands INSTANCE;
    public HashMap<CompatibleMaterial, Double> prices = new HashMap<>();
    public HashMap<String, Integer> playersCooldown = new HashMap<>();
    public boolean debug = false;
    public double priceMultiplier = 1;

    private WandManager wandManager;

    public static SellWands getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        EconomyManager.load();
        EconomyManager.getManager().setPreferredHook("Vault");

        saveDefaultConfig();
        parseConfigFile();
        cooldown();
        Bukkit.getPluginManager().registerEvents(new BlockInteractEvent(this), this);
        getServer().getPluginCommand("sellwands").setExecutor(new SellWandsCommand());
        getServer().getPluginCommand("sellwand").setExecutor(new SellWandCommand());
        priceMultiplier = getConfig().getDouble("price-multiplier");

        // Load wands.
        wandManager = new WandManager();
        loadWands();
        setupRecipes();
    }

    private void loadWands() {
        for (String key : getConfig().getConfigurationSection("items").getKeys(false)) {
            ConfigurationSection wand = getConfig().getConfigurationSection("items." + key);

            if (wand == null) continue;

            wandManager.addWand(new Wand(key, wand.getString("Name"),
                    CompatibleMaterial.getMaterial(wand.getString("Type")))
                    .setLore(wand.getStringList("Lore"))
                    .setEnchanted(wand.getBoolean("Enchanted"))
                    .setUses(wand.getInt("Uses"))

                    .setCraftable(wand.getBoolean("Slimefun-Craftable"))
                    .setRecipeLayout(wand.getString("Slimefun-Recipe-Layout"))
                    .setRecipeIngredients(wand.getStringList("Slimefun-Recipe-Ingredients")));
        }
    }


    private void setupRecipes() {
        for (Wand wand : wandManager.getWands()) {
            if (!wand.isCraftable()) continue;

            String recipe = wand.getRecipeLayout();

            if (recipe.length() != 9) continue;

            if (wand.getRecipeIngredients().isEmpty()) continue;

            Map<String, String> ingredients = new HashMap<>();

            for (String ingredient : wand.getRecipeIngredients()) {
                if (!ingredient.contains(",")) continue;
                String[] s = ingredient.split(",");
                String letter = s[0].trim();
                String item = s[1].trim();
                ingredients.put(letter, item);
            }

            List<ItemStack> items = new ArrayList<>();

            for (int i = 0; i < 9; i++) {
                String symbol = String.valueOf(recipe.charAt(i));
                String item = ingredients.get(symbol);

                SlimefunItem slimefunItem = SlimefunItem.getByID(item);
                if (slimefunItem == null) {
                    items.add(CompatibleMaterial.getMaterial(item).getItem());
                } else {
                    items.add(slimefunItem.getItem());
                }
            }

            System.out.println("[SellWands] Added Slimefun recipe for: " + wand.getKey());
            new SlimefunItem(Categories.TOOLS, wand.asItemStack(), wand.getKey(), RecipeType.ENHANCED_CRAFTING_TABLE,
                    items.toArray(new ItemStack[0])).register();
        }
    }

    private void cooldown() {
        BukkitScheduler cooldown = getServer().getScheduler();
        cooldown.scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (playersCooldown.containsKey(player.getName())) {
                    int cooldown1 = playersCooldown.get(player.getName());
                    cooldown1--;
                    if (cooldown1 <= 0) {
                        playersCooldown.remove(player.getName());
                    } else {
                        playersCooldown.put(player.getName(), cooldown1);
                    }
                }
            }
        }, 0L, 20L);
    }

    public void parseConfigFile() {
        prices.clear();
        FileConfiguration config = getConfig();
        ConfigurationSection pricesSection = config.getConfigurationSection("prices");
        for (String item : config.getConfigurationSection("prices").getKeys(false)) {
            Double price = pricesSection.getDouble(item);
            CompatibleMaterial material = CompatibleMaterial.getMaterial(item);

            System.out.println("[SellWands] Item " + material + " registered for $" + price);
            prices.put(material, price);
        }
        System.out.println("[SellWands] A total of " + prices.size() + " items have been registered.");
    }

    public WandManager getWandManager() {
        return wandManager;
    }
}
