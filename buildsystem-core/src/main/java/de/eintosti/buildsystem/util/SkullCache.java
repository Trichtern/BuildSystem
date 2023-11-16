/*
 * Copyright (c) 2018-2023, Thomas Meaney
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.eintosti.buildsystem.util;

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.BuildSystemPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SkullCache {

    private final Map<String, Object> skullCache;

    private Class<?> craftItemStackClass;

    public SkullCache(String version) {
        try {
            this.craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.skullCache = new HashMap<>();
    }

    @SuppressWarnings("deprecation")
    private ItemStack getSkull(String name) {
        ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        skullMeta.setOwner(name);
        skull.setItemMeta(skullMeta);

        return skull;
    }

    private Object getNmsSkull(String name) throws Exception {
        ItemStack skull = getSkull(name);
        Method getNMSItem = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        return getNMSItem.invoke(null, skull);
    }

    public void cacheSkull(String name) {
        try {
            skullCache.put(name, getNmsSkull(name));
            JavaPlugin.getPlugin(BuildSystemPlugin.class).getLogger().info("Cached skull for: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ItemStack getCachedSkull(String name) {
        try {
            this.skullCache.putIfAbsent(name, getNmsSkull(name));
            Object cachedSkull = this.skullCache.get(name);
            return (ItemStack) craftItemStackClass.getMethod("asBukkitCopy", cachedSkull.getClass()).invoke(null, cachedSkull);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return XMaterial.PLAYER_HEAD.parseItem();
    }
}