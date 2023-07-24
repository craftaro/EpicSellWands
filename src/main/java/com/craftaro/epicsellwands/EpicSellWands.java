package com.craftaro.epicsellwands;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicsellwands.commands.CommandAdmin;
import com.craftaro.epicsellwands.commands.CommandGive;
import com.craftaro.epicsellwands.commands.CommandReload;
import com.craftaro.epicsellwands.listeners.BlockListeners;
import com.craftaro.epicsellwands.player.PlayerManager;
import com.craftaro.epicsellwands.settings.Settings;
import com.craftaro.epicsellwands.wand.Wand;
import com.craftaro.epicsellwands.wand.WandManager;
import com.craftaro.core.SongodaCore;
import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.commands.CommandManager;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.core.hooks.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;

public class EpicSellWands extends SongodaPlugin {

    private static EpicSellWands INSTANCE;

    private final Config wandsConfig = new Config(this, "wands.yml");
    private final Config pricesConfig = new Config(this, "prices.yml");

    private WandManager wandManager;
    private CommandManager commandManager;
    private PlayerManager playerManager;
    private final GuiManager guiManager = new GuiManager(this);

    public static EpicSellWands getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
        saveWands();
    }

    @Override
    public void onPluginEnable() {
        SongodaCore.registerPlugin(this, 456, XMaterial.DIAMOND_HOE);

        // Load Economy
        EconomyManager.load();

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Set Economy & Hologram preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());

        // Setup plugin commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("esw")
                .addSubCommand(new CommandAdmin(this))
                .addSubCommand(new CommandReload())
                .addSubCommand(new CommandGive(this));

        // Load wands.
        this.wandManager = new WandManager();
        this.playerManager = new PlayerManager();

        // Load Listeners
        Bukkit.getPluginManager().registerEvents(new BlockListeners(this), this);

    }
    @Override
    public void onDataLoad() {
        loadWands();
        loadPrices();
    }

    private void loadWands() {
        if (!new File(this.getDataFolder(), "wands.yml").exists())
            this.saveResource("wands.yml", false);
        wandsConfig.load();

        for (String key : wandsConfig.getKeys(false)) {
            ConfigurationSection wand = wandsConfig.getConfigurationSection(key);

            if (wand == null) continue;

            wandManager.addWand(new Wand(key, wand.getString("Name"),
                     XMaterial.matchXMaterial(Material.getMaterial(wand.getString("Type"))))
                    .setLore(wand.getStringList("Lore"))
                    .setEnchanted(wand.getBoolean("Enchanted"))
                    .setUses(wand.getInt("Uses"))

                    .setRecipeLayout(wand.getString("Recipe-Layout"))
                    .setRecipeIngredients(wand.getStringList("Recipe-Ingredients")));
        }
    }

    public void saveWands() {
        // Remove deleted wands.
        for (String key : wandsConfig.getDefaultSection().getKeys(false)) {
            if (wandManager.getWands().stream().noneMatch(wand -> wand.getKey().equals(key)))
                wandsConfig.set(key, null);
        }

        // Save wands.
        for (Wand wand : wandManager.getWands()) {
            String key = wand.getKey();
            wandsConfig.set(key + ".Name", wand.getName());
            wandsConfig.set(key + ".Type", wand.getType().name());
            wandsConfig.set(key + ".Lore", wand.getLore());
            wandsConfig.set(key + ".Enchanted", wand.isEnchanted());
            wandsConfig.set(key + ".Uses", wand.getUses());

            wandsConfig.set(key + ".Recipe-Layout", wand.getRecipeLayout());
            wandsConfig.set(key + ".Recipe-Ingredients", wand.getRecipeIngredients());
        }
        wandsConfig.saveChanges();
    }

    /* As ModdedCore repo was deleted, this strings now doesn't make any sense
    private void setupRecipes() {
        com.songoda.moddedcore.ModdedCore moddedCore = com.craftaro.moddedcore.ModdedCore.getInstance();
        com.songoda.moddedcore.items.ItemManager itemManager = moddedCore.getItemManager();
        for (Wand wand : wandManager.getWands()) {

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

                com.craftaro.moddedcore.items.ModdedItem moddedItem = itemManager.getItem(item);
                if (moddedItem == null) {
                    items.add(CompatibleMaterial.getMaterial(item).getItem());
                } else {
                    items.add(moddedItem.asItemStack());
                }
            }

            getLogger().info("Added ModdedCore recipe for: " + wand.getKey());
            itemManager.addItem(new com.songoda.moddedcore.items.ModdedItem(this, wand.getKey(), wand.asItemStack(), itemManager.getCategory("TOOLS")));
        }
        moddedCore.getRecipeManager().loadFromFile(this);
    }*/

    @Override
    public void onConfigReload() {
        wandManager.clearData();
        loadPrices();
        loadWands();
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(pricesConfig, wandsConfig);
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    private void loadPrices() {
        if (!new File(this.getDataFolder(), "prices.yml").exists())
            this.saveResource("prices.yml", false);
        pricesConfig.load();

        for (String key : pricesConfig.getKeys(false)) {
            double price = pricesConfig.getDouble(key);
            XMaterial material = XMaterial.matchXMaterial(Material.getMaterial(key));
            wandManager.addPrice(material, price);
        }
    }

    public WandManager getWandManager() {
        return wandManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
