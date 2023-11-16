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
package de.eintosti.buildsystem.player;

import de.eintosti.buildsystem.api.player.BuildPlayer;
import de.eintosti.buildsystem.navigator.settings.NavigatorInventoryType;
import de.eintosti.buildsystem.settings.CraftSettings;
import de.eintosti.buildsystem.world.CraftBuildWorld;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CraftBuildPlayer implements BuildPlayer, ConfigurationSerializable {

    private final UUID uuid;
    private final CraftSettings settings;
    private final CachedValues cachedValues;

    private CraftBuildWorld cachedWorld;
    private LogoutLocation logoutLocation;
    private Location previousLocation;
    private NavigatorInventoryType lastLookedAt;

    public CraftBuildPlayer(UUID uuid, CraftSettings settings) {
        this.uuid = uuid;
        this.settings = settings;
        this.cachedValues = new CachedValues();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public CraftSettings getSettings() {
        return settings;
    }

    /**
     * Gets values that are supposed to be cached for a short amount of time.
     *
     * @return The player's cached values
     */
    public CachedValues getCachedValues() {
        return cachedValues;
    }

    /**
     * Gets the world the player has selected for an action.
     *
     * @return The cached world, if any
     */
    @Nullable
    public CraftBuildWorld getCachedWorld() {
        return cachedWorld;
    }

    /**
     * Marks a world as selected to later be used.
     *
     * @param buildWorld The world to select.
     */
    public void setCachedWorld(CraftBuildWorld buildWorld) {
        this.cachedWorld = buildWorld;
    }

    /**
     * Gets the location where the player was last before logging off.
     *
     * @return The location
     */
    @Nullable
    public LogoutLocation getLogoutLocation() {
        return logoutLocation;
    }

    /**
     * Sets the location where the player was last before logging off.
     *
     * @param logoutLocation The logout location
     */
    public void setLogoutLocation(LogoutLocation logoutLocation) {
        this.logoutLocation = logoutLocation;
    }

    /**
     * Gets the location the player was last at.
     * <p>
     * Usually this is the last location before teleportation.
     *
     * @return The player's previous location
     */
    @Nullable
    public Location getPreviousLocation() {
        return previousLocation;
    }

    /**
     * Sets the location the player was last at.
     * <p>
     * Usually this is the last location before teleportation.
     *
     * @param location The location
     */
    public void setPreviousLocation(Location location) {
        this.previousLocation = location;
    }

    /**
     * Gets the {@link NavigatorInventoryType} the player last looked at.
     *
     * @return The last looked navigator inventory type
     */
    @Nullable
    public NavigatorInventoryType getLastLookedAt() {
        return lastLookedAt;
    }

    /**
     * Sets the {@link NavigatorInventoryType} the player last looked at.
     *
     * @param type The last looked navigator inventory type
     */
    public void setLastLookedAt(NavigatorInventoryType type) {
        this.lastLookedAt = type;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> player = new HashMap<>();

        player.put("settings", settings.serialize());
        if (logoutLocation != null) {
            player.put("logout-location", logoutLocation.toString());
        }

        return player;
    }
}