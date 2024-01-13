package com.craftaro.epicsellwands.gui;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicsellwands.wand.Wand;
import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicsellwands.EpicSellWands;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class GuiEditWand extends Gui {

    private final EpicSellWands plugin;
    private final Gui returnGui;
    private final Wand wand;

    public GuiEditWand(EpicSellWands plugin, Gui returnGui, Wand wand) {
        super(3);
        this.plugin = plugin;
        this.returnGui = returnGui;
        this.wand = wand;
        setOnClose((event) -> plugin.saveWands());
        paint();
    }

    void paint() {
        setTitle(TextUtils.formatText(wand.getName()));

        setButton(0, 4, GuiUtils.createButtonItem(wand.getType(),
                TextUtils.formatText("&7Current Material: &6" + wand.getType().name()),
                TextUtils.formatText(Arrays.asList("",
                        "&8Click to set the material to",
                        "&8the material in your hand.")
                )), (event) -> {
            ItemStack stack = event.player.getInventory().getItemInHand();
            wand.setType(XMaterial.matchXMaterial(stack));
            paint();
        });

        setButton(0,8, GuiUtils.createButtonItem(XMaterial.OAK_FENCE_GATE,
                TextUtils.formatText("&cBack")),
                (event) -> {
                    guiManager.showGUI(event.player, returnGui);
                    ((GuiAdmin) returnGui).paint();
                });

        setButton(1, 1,
                GuiUtils.createButtonItem(XMaterial.BOOK,
                        TextUtils.formatText("&6Edit Wand Key"),
                        "",
                        TextUtils.formatText("&cThe key is the identifier for this"),
                        TextUtils.formatText("&cwand and must be unique!")),
                (event) -> {
                        AnvilGui gui = new AnvilGui(event.player, this);
                        gui.setAction((anvil) -> {
                            String oldKey = wand.getKey();
                            wand.setKey(gui.getInputText().trim());
                            plugin.getWandManager().reKey(oldKey, wand.getKey());
                            anvil.player.closeInventory();
                            paint();
                        });
                        gui.setTitle("Edit Key");
                        gui.setInput(GuiUtils.createButtonItem(XMaterial.PAPER,
                                wand.getKey()));
                        guiManager.showGUI(event.player, gui);
                });

        setButton(1, 2,
                GuiUtils.createButtonItem(XMaterial.WRITTEN_BOOK,
                        TextUtils.formatText("&9Left Click to edit the Name"),
                        TextUtils.formatText("&8Right Click to set Lore")),
                (event) -> {
                    if (event.clickType == ClickType.LEFT) {
                        AnvilGui gui = new AnvilGui(event.player, this);
                        gui.setAction((anvil) -> {
                            wand.setName(gui.getInputText().trim());
                            anvil.player.closeInventory();
                            paint();
                        });
                        gui.setTitle("Edit Name");
                        gui.setInput(GuiUtils.createButtonItem(XMaterial.PAPER,
                                wand.getName()));
                        guiManager.showGUI(event.player, gui);
                    } else if (event.clickType == ClickType.RIGHT) {
                        guiManager.showGUI(event.player, new GuiLoreEditor(wand, this));
                    }
                });

        setItem(1, 4, wand.asItemStack());

        setButton(1, 6, GuiUtils
                        .createButtonItem(XMaterial.ENCHANTED_BOOK,
                                TextUtils.formatText(wand.isEnchanted() ? "&cSet not enchanted" : "&aSet Enchanted")),
                (event) -> {
                    wand.setEnchanted(!wand.isEnchanted());
                    paint();
                });

        setButton(1, 7, GuiUtils
                        .createButtonItem(XMaterial.REDSTONE,
                                TextUtils.formatText("&bChange allowed use Count"),
                                "",
                                TextUtils.formatText("&8Use -1 for infinite.")),
                (event) -> {
                    AnvilGui gui = new AnvilGui(event.player, this);
                    gui.setAction((anvil) -> {
                        wand.setUses(Integer.parseInt(gui.getInputText().trim()));
                        anvil.player.closeInventory();
                        paint();
                    });
                    gui.setTitle("Edit Allowed Uses");
                    gui.setInput(GuiUtils.createButtonItem(XMaterial.PAPER,
                            String.valueOf(wand.getUses())));
                    guiManager.showGUI(event.player, gui);
                });

        setButton(2, 4, GuiUtils.createButtonItem(XMaterial.BARRIER,
                TextUtils.formatText("&cDelete"),
                TextUtils.formatText(Arrays.asList("",
                        "&8This action is irreversible!")
                )), (event) -> {
            plugin.getWandManager().removeWand(wand);
            guiManager.showGUI(event.player, returnGui);
            ((GuiAdmin) returnGui).paint();
        });
    }
}
