package com.songoda.epicsellwands.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicsellwands.EpicSellWands;
import com.songoda.epicsellwands.wand.Wand;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiAdmin extends Gui {

    private final EpicSellWands plugin;

    public GuiAdmin(EpicSellWands plugin) {
        this.plugin = plugin;
        setTitle("EpicSellWands Admin");
        paint();
    }

    void paint() {
        if (inventory != null)
            inventory.clear();
        setActionForRange(0, 0, 5, 9, null);

        List<Wand> wands = new ArrayList<>(plugin.getWandManager().getWands());

        setButton(0, 8, GuiUtils.createButtonItem(CompatibleMaterial.REDSTONE, "Create Wand"),
                (event) -> {
                    Wand wand = new Wand("WAND_" + (wands.size() + 1),
                            "Wand " + (wands.size() + 1),
                            CompatibleMaterial.WOODEN_HOE);
                    plugin.getWandManager().addWand(wand);
                    guiManager.showGUI(event.player, new GuiEditWand(plugin, this, wand));

                });

        for (int i = 0; i < wands.size(); i++) {
            Wand wand = wands.get(i);

            ItemStack item = wand.asItemStack();

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            lore.addAll(Arrays.asList("", TextUtils.formatText("&6Left Click &7to edit"), TextUtils.formatText("&6Right Click &7to take")));

            meta.setLore(lore);
            item.setItemMeta(meta);

            setButton(i + 9, item,
                    (event) -> {
                        if (event.clickType == ClickType.LEFT)
                            guiManager.showGUI(event.player, new GuiEditWand(plugin, this, wand));
                        else if (event.clickType == ClickType.RIGHT)
                            event.player.getInventory().addItem(wand.asItemStack());
                    });

        }
    }
}
