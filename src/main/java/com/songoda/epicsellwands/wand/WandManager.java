package com.songoda.epicsellwands.wand;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTCore;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.core.utils.TextUtils;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WandManager {

    private static Map<String, Wand> registeredWands = new HashMap<>();
    private static Map<CompatibleMaterial, Double> registeredPrices = new HashMap<>();

    public Wand addWand(Wand wand) {
        registeredWands.put(wand.getKey().toUpperCase(), wand);
        return wand;
    }

    public Wand getWand(String key) {
        return registeredWands.get(key.toUpperCase());
    }

    public Wand getWand(ItemStack wandItem) {
        NBTItem nbtItem = NmsManager.getNbt().of(wandItem);
        if (!nbtItem.has("wand"))
            return null;

        Wand wand = registeredWands.get(nbtItem.getNBTObject("wand").asString()).clone();
        wand.setUses(nbtItem.getNBTObject("uses").asInt());
        return wand;
    }

    public void addPrice(CompatibleMaterial material, double price) {
        registeredPrices.put(material, price);
    }

    public double getPriceFor(CompatibleMaterial material) {
        return registeredPrices.get(material);
    }

    public boolean isSellable(CompatibleMaterial material) {
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
