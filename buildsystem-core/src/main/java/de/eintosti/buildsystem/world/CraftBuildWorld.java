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
package de.eintosti.buildsystem.world;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.messages.Titles;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.api.event.world.BuildWorldLoadEvent;
import de.eintosti.buildsystem.api.event.world.BuildWorldPostLoadEvent;
import de.eintosti.buildsystem.api.event.world.BuildWorldPostUnloadEvent;
import de.eintosti.buildsystem.api.event.world.BuildWorldUnloadEvent;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.api.world.Builder;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.api.world.data.WorldType;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.util.InventoryUtils;
import de.eintosti.buildsystem.util.UUIDFetcher;
import de.eintosti.buildsystem.world.data.BuildWorldData;
import de.eintosti.buildsystem.world.generator.CustomGeneratorImpl;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CraftBuildWorld implements BuildWorld, ConfigurationSerializable {

    private final BuildSystemPlugin plugin;
    private final ConfigValues configValues;

    private String name;
    private String creator;
    private UUID creatorId;

    private final WorldType worldType;
    private final BuildWorldData worldData;
    private final long creationDate;
    private final de.eintosti.buildsystem.api.world.generator.CustomGenerator customGenerator;
    private final List<Builder> builders;

    private long seconds;
    private boolean loaded;
    private BukkitTask unloadTask;

    public CraftBuildWorld(
            String name,
            String creator,
            UUID creatorId,
            WorldType worldType,
            long creationDate,
            boolean privateWorld,
            de.eintosti.buildsystem.api.world.generator.CustomGenerator customGenerator
    ) {
        this.plugin = JavaPlugin.getPlugin(BuildSystemPlugin.class);
        this.configValues = plugin.getConfigValues();

        this.name = name;
        this.creator = creator;
        this.creatorId = creatorId;
        this.worldType = worldType;
        this.worldData = new BuildWorldData(
                name,
                configValues,
                privateWorld
        );
        this.customGenerator = customGenerator;
        this.creationDate = creationDate;
        this.builders = new ArrayList<>();

        InventoryUtils inventoryUtils = plugin.getInventoryUtil();
        XMaterial material;
        switch (worldType) {
            case NORMAL:
                material = inventoryUtils.getDefaultItem(WorldType.NORMAL);
                break;
            case FLAT:
                material = inventoryUtils.getDefaultItem(WorldType.FLAT);
                break;
            case NETHER:
                material = inventoryUtils.getDefaultItem(WorldType.NETHER);
                break;
            case END:
                material = inventoryUtils.getDefaultItem(WorldType.END);
                break;
            case VOID:
                material = inventoryUtils.getDefaultItem(WorldType.VOID);
                break;
            case CUSTOM:
            case TEMPLATE:
                material = XMaterial.FILLED_MAP;
                break;
            case IMPORTED:
                material = inventoryUtils.getDefaultItem(WorldType.IMPORTED);
                break;
            default:
                throw new IllegalArgumentException("Unsupported world type '" + worldType.name() + "' for world " + name);
        }
        if (privateWorld) {
            material = XMaterial.PLAYER_HEAD;
        }
        worldData.material().set(material);

        manageUnload();
    }

    public CraftBuildWorld(
            String name,
            String creator,
            UUID creatorId,
            WorldType worldType,
            BuildWorldData worldData,
            long creationDate,
            CustomGeneratorImpl customGenerator,
            List<Builder> builders
    ) {
        this.plugin = JavaPlugin.getPlugin(BuildSystemPlugin.class);
        this.configValues = plugin.getConfigValues();

        this.name = name;
        this.creator = creator;
        this.creatorId = creatorId;
        this.worldType = worldType;
        this.worldData = worldData;
        this.creationDate = creationDate;
        this.customGenerator = customGenerator;
        this.builders = builders;

        manageUnload();
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.worldData.setWorldName(name);
    }

    @Override
    public boolean hasCreator() {
        return getCreator() != null;
    }

    @Override
    @Nullable
    public String getCreator() {
        return creator;
    }

    @Override
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Override
    @Nullable
    public UUID getCreatorId() {
        return creatorId;
    }

    @Override
    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public boolean isCreator(Player player) {
        return creatorId != null && creatorId.equals(player.getUniqueId());
    }

    /**
     * Save the creator's unique-id in a string which is suitable to be stored.
     *
     * @return The creator's unique-id as a string
     */
    @Nullable
    private String saveCreatorId() {
        if (creatorId != null) {
            return String.valueOf(getCreatorId());
        }

        String creator = getCreator();
        if (creator != null && !creator.equalsIgnoreCase("-")) {
            return String.valueOf(UUIDFetcher.getUUID(creator));
        } else {
            return null;
        }
    }

    @Override
    public WorldType getType() {
        return worldType;
    }

    @Override
    public WorldData getData() {
        return worldData;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    @Nullable
    public de.eintosti.buildsystem.api.world.generator.CustomGenerator getCustomGenerator() {
        return customGenerator;
    }

    /**
     * Get the display name of a {@link Difficulty}.
     *
     * @param player The player to parse the placeholders against
     * @return the difficulty's display name
     * @see BuildWorldData#difficulty()
     */
    public String getDifficultyName(Player player) {
        switch (worldData.difficulty().get()) {
            case PEACEFUL:
                return Messages.getString("difficulty_peaceful", player);
            case EASY:
                return Messages.getString("difficulty_easy", player);
            case NORMAL:
                return Messages.getString("difficulty_normal", player);
            case HARD:
                return Messages.getString("difficulty_hard", player);
            default:
                return "-";
        }
    }

    @Override
    public void cycleDifficulty() {
        switch (worldData.difficulty().get()) {
            case PEACEFUL:
                worldData.difficulty().set(Difficulty.EASY);
                break;
            case EASY:
                worldData.difficulty().set(Difficulty.NORMAL);
                break;
            case NORMAL:
                worldData.difficulty().set(Difficulty.HARD);
                break;
            case HARD:
                worldData.difficulty().set(Difficulty.PEACEFUL);
                break;
        }
    }

    @Override
    public List<Builder> getBuilders() {
        return builders;
    }

    /**
     * Format the {@code %builder%} placeholder.
     *
     * @param player The player to parse the placeholders against
     * @return The list of builders which have been added to the given world as a string
     */
    public String getBuildersInfo(Player player) {
        String template = Messages.getString("world_item_builders_builder_template", player);
        List<String> builderNames = getBuilderNames();

        String string = "";
        if (builderNames.isEmpty()) {
            string = template.replace("%builder%", "-").trim();
        } else {
            for (String builderName : builderNames) {
                string = string.concat(template.replace("%builder%", builderName));
            }
            string = string.trim();
        }

        return string.substring(0, string.length() - 1);
    }

    @Override
    public List<String> getBuilderNames() {
        return getBuilders().stream()
                .map(Builder::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Nullable
    public Builder getBuilder(UUID uuid) {
        return this.builders.stream()
                .filter(builder -> builder.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isBuilder(UUID uuid) {
        return this.builders.stream().anyMatch(builder -> builder.getUuid().equals(uuid));
    }

    @Override
    public boolean isBuilder(Player player) {
        return isBuilder(player.getUniqueId());
    }

    @Override
    public void addBuilder(Builder builder) {
        this.builders.add(builder);
    }


    @Override
    public void removeBuilder(Builder builder) {
        this.builders.remove(builder);
    }

    @Override
    public void removeBuilder(UUID uuid) {
        removeBuilder(getBuilder(uuid));
    }

    /***
     * Save the list of {@link Builder}s in a string which is suitable to be stored.
     *
     * @return The list of builders as a string
     */
    private String saveBuilders() {
        StringBuilder builderList = new StringBuilder();
        for (Builder builder : getBuilders()) {
            builderList.append(";").append(builder.toString());
        }
        return builderList.length() > 0 ? builderList.substring(1) : builderList.toString();
    }

    /**
     * Get the time in the {@link World} linked to the build world.
     *
     * @return The world time
     */
    public String getWorldTime() {
        World bukkitWorld = getWorld();
        if (bukkitWorld == null) {
            return "?";
        }
        return String.valueOf(bukkitWorld.getTime());
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    public void manageUnload() {
        if (!configValues.isUnloadWorlds()) {
            this.loaded = true;
            return;
        }

        this.seconds = configValues.getTimeUntilUnload();
        this.loaded = (getWorld() != null);
        startUnloadTask();
    }

    private void startUnloadTask() {
        if (!configValues.isUnloadWorlds()) {
            return;
        }

        this.unloadTask = Bukkit.getScheduler().runTaskLater(plugin, this::unload, 20L * seconds);
    }

    public void resetUnloadTask() {
        if (this.unloadTask != null) {
            this.unloadTask.cancel();
        }

        startUnloadTask();
    }

    @Override
    public void unload() {
        World bukkitWorld = getWorld();
        if (bukkitWorld == null) {
            return;
        }

        if (!bukkitWorld.getPlayers().isEmpty()) {
            resetUnloadTask();
            return;
        }

        if (configValues.getBlackListedWorldsToUnload().contains(name) || isSpawnWorld(bukkitWorld)) {
            return;
        }

        forceUnload(true);
    }

    @Override
    public void forceUnload(boolean save) {
        World bukkitWorld = getWorld();
        if (bukkitWorld == null) {
            return;
        }

        BuildWorldUnloadEvent unloadEvent = new BuildWorldUnloadEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(unloadEvent);
        if (unloadEvent.isCancelled()) {
            return;
        }

        for (Chunk chunk : bukkitWorld.getLoadedChunks()) {
            chunk.unload(save);
        }
        Bukkit.unloadWorld(bukkitWorld, save);
        Bukkit.getWorlds().remove(bukkitWorld);

        this.worldData.lastUnloaded().set(System.currentTimeMillis());
        this.loaded = false;
        this.unloadTask = null;

        Bukkit.getServer().getPluginManager().callEvent(new BuildWorldPostUnloadEvent(this));

        plugin.getLogger().info("*** Unloaded world \"" + name + "\" ***");
    }

    private boolean isSpawnWorld(World bukkitWorld) {
        SpawnManager spawnManager = plugin.getSpawnManager();
        if (!spawnManager.spawnExists()) {
            return false;
        }

        return Objects.equals(spawnManager.getSpawn().getWorld(), bukkitWorld);
    }

    public void load(Player player) {
        if (isLoaded()) {
            return;
        }

        player.closeInventory();
        Titles.sendTitle(player, 5, 70, 20, " ",
                Messages.getString("loading_world", player, new AbstractMap.SimpleEntry<>("%world%", name))
        );

        load();
    }

    @Override
    public void load() {
        if (isLoaded()) {
            return;
        }

        BuildWorldLoadEvent loadEvent = new BuildWorldLoadEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(loadEvent);
        if (loadEvent.isCancelled()) {
            return;
        }

        plugin.getLogger().info("*** Loading world \"" + name + "\" ***");
        World world = new CraftBuildWorldCreator(plugin, this).generateBukkitWorld();
        if (world == null) {
            return;
        }

        this.worldData.lastLoaded().set(System.currentTimeMillis());
        this.loaded = true;

        Bukkit.getServer().getPluginManager().callEvent(new BuildWorldPostLoadEvent(this));

        resetUnloadTask();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> world = new HashMap<>();

        world.put("creator", creator);
        world.put("creator-id", saveCreatorId());
        world.put("type", worldType.name());
        world.put("data", worldData.serialize());
        world.put("date", creationDate);
        world.put("builders", saveBuilders());
        if (customGenerator != null) {
            world.put("chunk-generator", customGenerator.getName());
        }

        return world;
    }

    public enum Time {
        SUNRISE, NOON, NIGHT, UNKNOWN
    }
}