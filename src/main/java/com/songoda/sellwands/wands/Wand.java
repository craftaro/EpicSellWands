package com.songoda.sellwands.wands;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.ItemUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.sellwands.SellWands;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Wand implements Cloneable {

    private final String key, name;
    private final CompatibleMaterial type;
    private List<String> lore = new ArrayList<>();
    private boolean enchanted = false;
    private int uses = -1;

    private String recipeLayout;
    private List<String> recipeIngredients = new ArrayList<>();

    public Wand(String key, String name, CompatibleMaterial type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public ItemStack asItemStack() {
        ItemStack item = GuiUtils.createButtonItem(type,
                TextUtils.convertToInvisibleString("SELLWAND:" + key + ":" + uses + ":") + TextUtils.formatText(name));

        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        for (String line : this.lore)
            lore.add(TextUtils.formatText(line));
        if (uses != -1)
            lore.add(TextUtils.formatText(SellWands.getInstance().getConfig().getString("messages.item-use-lore")
                    .replace("%uses%", Integer.toString(uses))));
        meta.setLore(lore);
        item.setItemMeta(meta);

        if (enchanted)
            ItemUtils.addGlow(item);
        return item;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public CompatibleMaterial getType() {
        return type;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public Wand setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public Wand setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
        return this;
    }

    public int getUses() {
        return uses;
    }

    public int use() {
        uses --;
        return uses;
    }

    public Wand setUses(int uses) {
        this.uses = uses;
        return this;
    }

    public String getRecipeLayout() {
        return recipeLayout;
    }

    public Wand setRecipeLayout(String recipeLayout) {
        this.recipeLayout = recipeLayout;
        return this;
    }

    public List<String> getRecipeIngredients() {
        return recipeIngredients;
    }

    public Wand setRecipeIngredients(List<String> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
        return this;
    }

    public Wand clone() {
        try {
            return (Wand) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}
