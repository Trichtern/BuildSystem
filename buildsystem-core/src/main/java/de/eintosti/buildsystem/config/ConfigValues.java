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
package de.eintosti.buildsystem.config;

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.data.Visibility;
import org.bukkit.Difficulty;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigValues {

    private final BuildSystemPlugin plugin;

    private String dateFormat;
    private String timeUntilUnload;
    private String defaultPublicPermission;
    private String defaultPrivatePermission;
    private String invalidNameCharacters;

    private XMaterial navigatorItem;
    private XMaterial worldEditWand;
    private Difficulty worldDifficulty;

    private boolean archiveVanish;
    private boolean scoreboard;
    private boolean spawnTeleportMessage;
    private boolean joinQuitMessages;
    private boolean teleportAfterCreation;
    private boolean buildModeDropItems;
    private boolean buildModeMoveItems;
    private boolean lockWeather;
    private boolean unloadWorlds;
    private boolean voidBlock;
    private boolean updateChecker;
    private boolean blockWorldEditNonBuilder;
    private boolean giveNavigatorOnJoin;
    private boolean worldPhysics;
    private boolean worldExplosions;
    private boolean worldMobAi;
    private boolean worldBlockBreaking;
    private boolean worldBlockPlacement;
    private boolean worldBlockInteractions;
    private boolean[] worldBuildersEnabled;
    private boolean saveFromDeath;
    private boolean teleportToMapSpawn;

    private int sunriseTime;
    private int noonTime;
    private int nightTime;
    private int worldBorderSize;
    private int importDelay;
    private int maxPublicWorldAmount;
    private int maxPrivateWorldAmount;

    private Map<String, String> defaultGameRules;
    private Set<String> blackListedWorldsToUnload;

    public ConfigValues(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        setConfigValues();
    }

    public void setConfigValues() {
        final FileConfiguration config = plugin.getConfig();

        // Messages
        this.spawnTeleportMessage = config.getBoolean("messages.spawn-teleport-message", false);
        this.joinQuitMessages = config.getBoolean("messages.join-quit-messages", true);
        this.dateFormat = config.getString("messages.date-format", "dd/MM/yyyy");

        // Save from death
        this.saveFromDeath = config.getBoolean("settings.save-from-death.enable", true);
        this.teleportToMapSpawn = config.getBoolean("settings.save-from-death.teleport-to-map-spawn", true);

        // Settings
        this.updateChecker = config.getBoolean("settings.update-checker", true);
        this.scoreboard = config.getBoolean("settings.scoreboard", true);
        this.archiveVanish = config.getBoolean("settings.archive-vanish", true);
        this.teleportAfterCreation = config.getBoolean("settings.teleport-after-creation", true);
        this.buildModeDropItems = config.getBoolean("settings.build-mode.drop-items", true);
        this.buildModeMoveItems = config.getBoolean("settings.build-mode.move-items", true);

        this.blockWorldEditNonBuilder = config.getBoolean("settings.builder.block-worldedit-non-builder", true);
        this.worldEditWand = parseWorldEditWand();

        this.navigatorItem = XMaterial.valueOf(config.getString("settings.navigator.item", "CLOCK"));
        this.giveNavigatorOnJoin = config.getBoolean("settings.navigator.give-item-on-join", true);

        // World
        this.defaultPublicPermission = config.getString("world.default.permission.public", "-");
        this.defaultPrivatePermission = config.getString("world.default.permission.private", "-");
        this.lockWeather = config.getBoolean("world.lock-weather", true);
        this.invalidNameCharacters = config.getString("world.invalid-characters", "^\b$");
        this.worldDifficulty = Difficulty.valueOf(config.getString("world.default.difficulty", "PEACEFUL").toUpperCase());
        this.sunriseTime = config.getInt("world.default.time.sunrise", 0);
        this.noonTime = config.getInt("world.default.time.noon", 6000);
        this.nightTime = config.getInt("world.default.time.night", 18000);

        this.worldBorderSize = config.getInt("world.default.worldborder.size", 6000000);

        Map<String, String> defaultGameRules = new HashMap<>();
        ConfigurationSection configurationSection = config.getConfigurationSection("world.default.gamerules");
        if (configurationSection != null) {
            for (Map.Entry<String, Object> entry : configurationSection.getValues(true).entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue().toString();
                defaultGameRules.put(name, value);
            }
        }
        this.defaultGameRules = defaultGameRules;

        this.worldPhysics = config.getBoolean("world.default.settings.physics", true);
        this.worldExplosions = config.getBoolean("world.default.settings.explosions", true);
        this.worldMobAi = config.getBoolean("world.default.settings.mob-ai", true);
        this.worldBlockBreaking = config.getBoolean("world.default.settings.block-breaking", true);
        this.worldBlockPlacement = config.getBoolean("world.default.settings.block-placement", true);
        this.worldBlockInteractions = config.getBoolean("world.default.settings.block-interactions", true);
        this.worldBuildersEnabled = new boolean[]{
                config.getBoolean("world.default.settings.builders-enabled.public", false),
                config.getBoolean("world.default.settings.builders-enabled.private", true)
        };

        this.unloadWorlds = config.getBoolean("world.unload.enabled", true);
        this.timeUntilUnload = config.getString("world.unload.time-until-unload", "01:00:00");
        this.blackListedWorldsToUnload = new HashSet<>(config.getStringList("world.unload.blacklisted-worlds"));

        this.importDelay = config.getInt("world.import-all.delay", 30);

        this.maxPublicWorldAmount = config.getInt("world.max-amount.public", -1);
        this.maxPrivateWorldAmount = config.getInt("world.max-amount.private", -1);

        this.voidBlock = config.getBoolean("world.void-block", true);
    }

    private XMaterial parseWorldEditWand() {
        File pluginDir = plugin.getDataFolder().getParentFile();
        File configFile = null;

        File weConfig = new File(pluginDir + File.separator + "WorldEdit", "config.yml");
        if (weConfig.exists()) {
            configFile = weConfig;
        }

        File faweConfig = new File(pluginDir + File.separator + "FastAsyncWorldEdit", "worldedit-config.yml");
        if (faweConfig.exists()) {
            configFile = faweConfig;
        }

        XMaterial defaultWand = XMaterial.WOODEN_AXE;
        if (configFile == null) {
            return defaultWand;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String wand = config.getString("wand-item");
        if (wand == null) {
            return defaultWand;
        }

        String namespace = "minecraft:";
        if (wand.toLowerCase().startsWith(namespace)) {
            wand = wand.substring(namespace.length());
        }

        return XMaterial.matchXMaterial(wand).orElse(defaultWand);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public Difficulty getWorldDifficulty() {
        return worldDifficulty;
    }

    public long getTimeUntilUnload() {
        String[] timeArray = timeUntilUnload.split(":");
        int hours = Integer.parseInt(timeArray[0]);
        int minutes = Integer.parseInt(timeArray[1]);
        int seconds = Integer.parseInt(timeArray[2]);
        return hours * 3600L + minutes * 60L + seconds;
    }

    public boolean isArchiveVanish() {
        return archiveVanish;
    }

    public boolean isTeleportAfterCreation() {
        return teleportAfterCreation;
    }

    public boolean isBuildModeDropItems() {
        return buildModeDropItems;
    }

    public boolean isBuildModeMoveItems() {
        return buildModeMoveItems;
    }

    public boolean isScoreboard() {
        return scoreboard;
    }

    public boolean isSpawnTeleportMessage() {
        return spawnTeleportMessage;
    }

    public boolean isJoinQuitMessages() {
        return joinQuitMessages;
    }

    public boolean isLockWeather() {
        return lockWeather;
    }

    public boolean isUnloadWorlds() {
        return unloadWorlds;
    }

    public boolean isVoidBlock() {
        return voidBlock;
    }

    public boolean isUpdateChecker() {
        return updateChecker;
    }

    public boolean isBlockWorldEditNonBuilder() {
        return blockWorldEditNonBuilder;
    }

    public boolean isGiveNavigatorOnJoin() {
        return giveNavigatorOnJoin;
    }

    public boolean isWorldPhysics() {
        return worldPhysics;
    }

    public boolean isWorldExplosions() {
        return worldExplosions;
    }

    public boolean isWorldMobAi() {
        return worldMobAi;
    }

    public boolean isWorldBlockBreaking() {
        return worldBlockBreaking;
    }

    public boolean isWorldBlockPlacement() {
        return worldBlockPlacement;
    }

    public boolean isWorldBlockInteractions() {
        return worldBlockInteractions;
    }

    public boolean isWorldBuildersEnabled(boolean privateWorld) {
        int i = privateWorld ? 1 : 0;
        return worldBuildersEnabled[i];
    }

    public XMaterial getNavigatorItem() {
        return navigatorItem;
    }

    public XMaterial getWorldEditWand() {
        return worldEditWand;
    }

    public int getSunriseTime() {
        return sunriseTime;
    }

    public int getNoonTime() {
        return noonTime;
    }

    public int getNightTime() {
        return nightTime;
    }

    public int getWorldBorderSize() {
        return worldBorderSize;
    }

    public int getImportDelay() {
        return importDelay;
    }

    public int getMaxWorldAmount(Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return maxPublicWorldAmount;
            case PRIVATE:
                return maxPrivateWorldAmount;
            default:
                throw new IllegalArgumentException("Invalid visibility. Use PUBLIC or PRIVATE");
        }
    }

    public Map<String, String> getDefaultGameRules() {
        return defaultGameRules;
    }

    public Set<String> getBlackListedWorldsToUnload() {
        return blackListedWorldsToUnload;
    }

    public String getDefaultPermission(boolean privateWorld) {
        return (privateWorld ? defaultPrivatePermission : defaultPublicPermission);
    }

    public boolean isSaveFromDeath() {
        return saveFromDeath;
    }

    public boolean isTeleportToMapSpawn() {
        return teleportToMapSpawn;
    }

    public String getInvalidNameCharacters() {
        return invalidNameCharacters;
    }
}