package com.songoda.sellwands.wands;

import com.songoda.core.utils.TextUtils;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WandManager {

    private static Map<String, Wand> registeredWands = new HashMap<>();

    public Wand addWand(Wand wand) {
        registeredWands.put(wand.getKey().toUpperCase(), wand);
        return wand;
    }

    public Wand getWand(String key) {
        return registeredWands.get(key.toUpperCase());
    }

    public Wand getWand(ItemStack wandItem) {
        String[] split = TextUtils.convertFromInvisibleString(wandItem.getItemMeta().getDisplayName()).split(":");
        Wand wand = registeredWands.get(split[1]).clone();
        wand.setUses(Integer.parseInt(split[2]));
        return wand;
    }

    public Collection<Wand> getWands() {
        return Collections.unmodifiableCollection(registeredWands.values());
    }
}
