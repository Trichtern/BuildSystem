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

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Method;

public class EntityAIManager {

    private static Class<?> nmsEntityClass;
    private static Class<?> nbtTagClass;

    private static Method getHandle;
    private static Method getNBTTag;
    private static Method c;
    private static Method setInt;
    private static Method f;

    public static void setAIEnabled(Entity entity, boolean enabled) {
        switch (entity.getType()) {
            case ARMOR_STAND:
            case ITEM_FRAME:
            case PAINTING:
            case PLAYER:
                return;
        }

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        switch (version) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
                break;
            default:
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.setAI(enabled);
                return;
        }

        try {
            if (getHandle == null) {
                Class<?> craftEntity = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");
                getHandle = craftEntity.getDeclaredMethod("getHandle");
                getHandle.setAccessible(true);
            }

            Object nmsEntity = getHandle.invoke(entity);
            if (nmsEntityClass == null) {
                nmsEntityClass = Class.forName("net.minecraft.server." + version + ".Entity");
            }

            if (getNBTTag == null) {
                getNBTTag = nmsEntityClass.getDeclaredMethod("getNBTTag");
                getNBTTag.setAccessible(true);
            }

            if (nbtTagClass == null) {
                nbtTagClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            }

            Object tag = getNBTTag.invoke(nmsEntity);
            if (tag == null) {
                tag = nbtTagClass.getDeclaredConstructor().newInstance();
            }

            if (c == null) {
                c = nmsEntityClass.getDeclaredMethod("c", nbtTagClass);
                c.setAccessible(true);
            }
            c.invoke(nmsEntity, tag);

            if (setInt == null) {
                setInt = nbtTagClass.getDeclaredMethod("setInt", String.class, Integer.TYPE);
                setInt.setAccessible(true);
            }

            int value = enabled ? 0 : 1;
            setInt.invoke(tag, "NoAI", value);

            if (f == null) {
                f = nmsEntityClass.getDeclaredMethod("f", nbtTagClass);
                f.setAccessible(true);
            }
            f.invoke(nmsEntity, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}