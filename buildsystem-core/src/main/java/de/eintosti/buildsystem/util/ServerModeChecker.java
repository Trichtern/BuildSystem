/*
 * Copyright (c) 2018-2025, Thomas Meaney
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utility class to determine the mode in which the server is running. The server can be in one of the following modes:
 * <ul>
 *     <li>{@link ServerMode#ONLINE} - The server is running in online mode</li>
 *     <li>{@link ServerMode#OFFLINE} - The server is running in offline mode</li>
 * </ul>
 */
public final class ServerModeChecker {

    private static final ServerMode SERVER_MODE = determineServerMode();

    private ServerModeChecker() {
    }

    /**
     * Determines the server mode based on configuration settings.
     *
     * @return The detected server mode
     */
    private static ServerMode determineServerMode() {
        try {
            Class<?> CLASS_GlobalConfiguration = Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            Class<?> CLASS_Proxies = Class.forName("io.papermc.paper.configuration.GlobalConfiguration$Proxies");
            Field FIELD_instance = CLASS_GlobalConfiguration.getDeclaredField("instance");
            FIELD_instance.setAccessible(true);
            Field FIELD_proxies = CLASS_GlobalConfiguration.getDeclaredField("proxies");
            FIELD_proxies.setAccessible(true);
            Method METHOD_isProxyOnlineMode = CLASS_Proxies.getDeclaredMethod("isProxyOnlineMode");
            METHOD_isProxyOnlineMode.setAccessible(true);
            Object OBJECT_instance = FIELD_instance.get(null);
            Object OBJECT_proxies = FIELD_proxies.get(OBJECT_instance);
            boolean isOnline = (boolean) METHOD_isProxyOnlineMode.invoke(OBJECT_proxies);
            return isOnline ? ServerMode.ONLINE : ServerMode.OFFLINE;
        } catch (Exception e) {
            return Bukkit.getOnlineMode() ? ServerMode.ONLINE : ServerMode.OFFLINE;
        }
    }

    /**
     * Retrieves the current server mode.
     *
     * @return The detected server mode
     */
    public static ServerMode getServerMode() {
        return SERVER_MODE;
    }

    /**
     * Enum representing the possible server modes.
     */
    public enum ServerMode {

        /**
         * The server is online.
         */
        ONLINE,

        /**
         * The server is offline.
         */
        OFFLINE
    }
}
