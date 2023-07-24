package com.craftaro.epicsellwands.wand;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WandManager {
    private static Map<String, Wand> registeredWands = new LinkedHashMap<>();
    private static Map<XMaterial, Double> registeredPrices = new HashMap<>();

    public Wand addWand(Wand wand) {
        registeredWands.put(wand.getKey().toUpperCase(), wand);
        return wand;
    }

    public Wand getWand(String key) {
        return registeredWands.get(key.toUpperCase());
    }

    public Wand getWand(ItemStack wandItem) {
        NBTItem nbtItem = new NBTItem(wandItem);
        if (!nbtItem.hasKey("wand")) return null;

        Wand wand = registeredWands.get(nbtItem.getString("wand")).clone();
        wand.setUses(nbtItem.getInteger("uses"));
        return wand;
    }

    public void removeWand(Wand wand) {
        registeredWands.remove(wand.getKey());
    }

    public void reKey(String oldKey, String key) {
        Wand wand = getWand(oldKey);
        registeredWands.remove(oldKey);
        registeredWands.put(key, wand);
    }

    public void addPrice(XMaterial material, double price) {
        registeredPrices.put(material, price);
    }

    public double getPriceFor(XMaterial material) {
        return registeredPrices.get(material);
    }

    public boolean isSellable(XMaterial material) {
        return registeredPrices.containsKey(material);
    }

    public Collection<Wand> getWands() {
        return Collections.unmodifiableCollection(registeredWands.values());
    }

    public void clearData() {
        registeredWands.clear();
        registeredPrices.clear();
    }
}
