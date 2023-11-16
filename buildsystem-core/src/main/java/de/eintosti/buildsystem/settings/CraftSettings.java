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
package de.eintosti.buildsystem.settings;

import de.eintosti.buildsystem.api.settings.DesignColor;
import de.eintosti.buildsystem.api.settings.NavigatorType;
import de.eintosti.buildsystem.api.settings.Settings;
import de.eintosti.buildsystem.api.settings.WorldDisplay;
import de.eintosti.buildsystem.navigator.settings.BuildWorldDisplay;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CraftSettings implements Settings, ConfigurationSerializable {

    private DesignColor designColor;
    private NavigatorType navigatorType;
    private BuildWorldDisplay worldDisplay;
    private boolean clearInventory;
    private boolean disableInteract;
    private boolean hidePlayers;
    private boolean instantPlaceSigns;
    private boolean keepNavigator;
    private boolean nightVision;
    private boolean noClip;
    private boolean placePlants;
    private boolean scoreboard;
    private boolean slabBreaking;
    private boolean spawnTeleport;
    private boolean trapDoor;

    private BukkitTask scoreboardTask;

    public CraftSettings() {
        this.navigatorType = NavigatorType.OLD;
        this.designColor = DesignColor.BLACK;
        this.worldDisplay = new BuildWorldDisplay();
        this.clearInventory = false;
        this.disableInteract = false;
        this.hidePlayers = false;
        this.instantPlaceSigns = false;
        this.keepNavigator = false;
        this.nightVision = false;
        this.noClip = false;
        this.placePlants = false;
        this.scoreboard = true;
        this.slabBreaking = false;
        this.spawnTeleport = true;
        this.trapDoor = false;
    }

    public CraftSettings(
            NavigatorType navigatorType,
            DesignColor designColor,
            BuildWorldDisplay worldDisplay,
            boolean clearInventory,
            boolean disableInteract,
            boolean hidePlayers,
            boolean instantPlaceSigns,
            boolean keepNavigator,
            boolean nightVision,
            boolean noClip,
            boolean placePlants,
            boolean scoreboard,
            boolean slabBreaking,
            boolean spawnTeleport,
            boolean trapDoor
    ) {
        this.navigatorType = navigatorType == null ? NavigatorType.OLD : navigatorType;
        this.designColor = designColor == null ? DesignColor.BLACK : designColor;
        this.worldDisplay = worldDisplay;
        this.clearInventory = clearInventory;
        this.disableInteract = disableInteract;
        this.hidePlayers = hidePlayers;
        this.instantPlaceSigns = instantPlaceSigns;
        this.keepNavigator = keepNavigator;
        this.nightVision = nightVision;
        this.noClip = noClip;
        this.placePlants = placePlants;
        this.scoreboard = scoreboard;
        this.slabBreaking = slabBreaking;
        this.spawnTeleport = spawnTeleport;
        this.trapDoor = trapDoor;
    }

    @Override
    public NavigatorType getNavigatorType() {
        return navigatorType;
    }

    @Override
    public void setNavigatorType(NavigatorType navigatorType) {
        this.navigatorType = navigatorType;
    }

    @Override
    public DesignColor getDesignColor() {
        return designColor;
    }

    @Override
    public void setDesignColor(DesignColor designColor) {
        this.designColor = designColor;
    }

    @Override
    public WorldDisplay getWorldDisplay() {
        return worldDisplay;
    }

    @Override
    public void setWorldDisplay(WorldDisplay worldDisplay) {
        this.worldDisplay = (BuildWorldDisplay) worldDisplay;
    }

    @Override
    public boolean isClearInventory() {
        return clearInventory;
    }

    @Override
    public void setClearInventory(boolean clearInventory) {
        this.clearInventory = clearInventory;
    }

    @Override
    public boolean isDisableInteract() {
        return disableInteract;
    }

    @Override
    public void setDisableInteract(boolean disableInteract) {
        this.disableInteract = disableInteract;
    }

    @Override
    public boolean isHidePlayers() {
        return hidePlayers;
    }

    @Override
    public void setHidePlayers(boolean hidePlayers) {
        this.hidePlayers = hidePlayers;
    }

    @Override
    public boolean isInstantPlaceSigns() {
        return instantPlaceSigns;
    }

    @Override
    public void setInstantPlaceSigns(boolean instantPlaceSigns) {
        this.instantPlaceSigns = instantPlaceSigns;
    }

    @Override
    public boolean isKeepNavigator() {
        return keepNavigator;
    }

    @Override
    public void setKeepNavigator(boolean keepNavigator) {
        this.keepNavigator = keepNavigator;
    }

    @Override
    public boolean isNightVision() {
        return nightVision;
    }

    @Override
    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
    }

    @Override
    public boolean isNoClip() {
        return noClip;
    }

    @Override
    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
    }

    @Override
    public boolean isPlacePlants() {
        return placePlants;
    }

    @Override
    public void setPlacePlants(boolean placePlants) {
        this.placePlants = placePlants;
    }

    @Override
    public boolean isScoreboard() {
        return scoreboard;
    }

    @Override
    public void setScoreboard(boolean scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public boolean isSlabBreaking() {
        return slabBreaking;
    }

    @Override
    public void setSlabBreaking(boolean slabBreaking) {
        this.slabBreaking = slabBreaking;
    }

    @Override
    public boolean isSpawnTeleport() {
        return spawnTeleport;
    }

    @Override
    public void setSpawnTeleport(boolean spawnTeleport) {
        this.spawnTeleport = spawnTeleport;
    }

    @Override
    public boolean isOpenTrapDoors() {
        return trapDoor;
    }

    @Override
    public void setOpenTrapDoors(boolean openTrapDoors) {
        this.trapDoor = openTrapDoors;
    }

    public BukkitTask getScoreboardTask() {
        return scoreboardTask;
    }

    public void setScoreboardTask(BukkitTask scoreboardTask) {
        this.scoreboardTask = scoreboardTask;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> settings = new HashMap<>();

        settings.put("type", navigatorType.toString());
        settings.put("glass", designColor.toString());
        settings.put("world-display", worldDisplay.serialize());
        settings.put("slab-breaking", slabBreaking);
        settings.put("no-clip", noClip);
        settings.put("trapdoor", trapDoor);
        settings.put("nightvision", nightVision);
        settings.put("scoreboard", scoreboard);
        settings.put("keep-navigator", keepNavigator);
        settings.put("disable-interact", disableInteract);
        settings.put("spawn-teleport", spawnTeleport);
        settings.put("clear-inventory", clearInventory);
        settings.put("instant-place-signs", instantPlaceSigns);
        settings.put("hide-players", hidePlayers);
        settings.put("place-plants", placePlants);

        return settings;
    }
}