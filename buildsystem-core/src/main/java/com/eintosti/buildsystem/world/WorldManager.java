/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.eintosti.buildsystem.world;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.Messages;
import com.eintosti.buildsystem.config.ConfigValues;
import com.eintosti.buildsystem.config.WorldConfig;
import com.eintosti.buildsystem.navigator.world.FilteredWorldsInventory.Visibility;
import com.eintosti.buildsystem.util.FileUtils;
import com.eintosti.buildsystem.util.UUIDFetcher;
import com.eintosti.buildsystem.util.external.PlayerChatInput;
import com.eintosti.buildsystem.world.data.WorldStatus;
import com.eintosti.buildsystem.world.data.WorldType;
import com.eintosti.buildsystem.world.generator.CustomGenerator;
import com.eintosti.buildsystem.world.generator.Generator;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author einTosti
 */
public class WorldManager {

    private final BuildSystem plugin;
    private final ConfigValues configValues;
    private final WorldConfig worldConfig;

    private final List<BuildWorld> buildWorlds;

    public WorldManager(BuildSystem plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.worldConfig = new WorldConfig(plugin);

        this.buildWorlds = new ArrayList<>();
    }

    /**
     * Gets the {@link BuildWorld} by the given name.
     *
     * @param worldName The name of the world
     * @return The world object if one was found, {@code null} otherwise
     */
    public BuildWorld getBuildWorld(String worldName) {
        return this.buildWorlds.stream()
                .filter(buildWorld -> buildWorld.getName().equalsIgnoreCase(worldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the {@link BuildWorld} by the given {@link World}.
     *
     * @param world The bukkit world object
     * @return The world object if one was found, {@code null} otherwise
     */
    public BuildWorld getBuildWorld(World world) {
        return getBuildWorld(world.getName());
    }

    /**
     * Gets a list of all {@link BuildWorld}s.
     *
     * @return A list of all worlds
     */
    public List<BuildWorld> getBuildWorlds() {
        return buildWorlds;
    }

    /**
     * Gets a list of all {@link BuildWorld}s created by the given player.
     *
     * @param player The player who created the world
     * @return A list of all worlds created by the given player.
     */
    public List<BuildWorld> getBuildWorldsCreatedByPlayer(Player player) {
        return getBuildWorlds().stream()
                .filter(buildWorld -> buildWorld.isCreator(player))
                .collect(Collectors.toList());
    }

    /**
     * Gets a list of all {@link BuildWorld}s created by the given player.
     *
     * @param player     The player who created the world
     * @param visibility The visibility the world should have
     * @return A list of all worlds created by the given player.
     */
    public List<BuildWorld> getBuildWorldsCreatedByPlayer(Player player, Visibility visibility) {
        return getBuildWorldsCreatedByPlayer(player).stream()
                .filter(buildWorld -> isCorrectVisibility(buildWorld, visibility))
                .collect(Collectors.toList());
    }

    /**
     * Gets if a {@link BuildWorld}'s visibility is equal to the given visibility.
     *
     * @param buildWorld The world object
     * @param visibility The visibility the world should have
     * @return {@code true} if the world's visibility is equal to the given visibility, otherwise {@code false}
     */
    public boolean isCorrectVisibility(BuildWorld buildWorld, Visibility visibility) {
        switch (visibility) {
            case PRIVATE:
                return buildWorld.isPrivate();
            case PUBLIC:
                return !buildWorld.isPrivate();
            case IGNORE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Gets the name (and in doing so removes all illegal characters) of the {@link BuildWorld} the player is trying to create.
     * If the world is going to be a private world, its name will be equal to the player's name.
     *
     * @param player       The player who is creating the world
     * @param worldType    The world type
     * @param template     The name of the template world, if any, otherwise {@code null}
     * @param privateWorld Is world going to be a private world?
     */
    public void startWorldNameInput(Player player, WorldType worldType, @Nullable String template, boolean privateWorld) {
        if (privateWorld && getBuildWorld(player.getName()) == null) {
            player.closeInventory();
            new BuildWorldCreator(plugin, player.getName())
                    .setType(worldType)
                    .setTemplate(template)
                    .setPrivate(true)
                    .createWorld(player);
            return;
        }

        new PlayerChatInput(plugin, player, "enter_world_name", input -> {
            for (String charString : input.split("")) {
                if (charString.matches("[^A-Za-z\\d/_-]")) {
                    Messages.sendMessage(player, "worlds_world_creation_invalid_characters");
                    break;
                }
            }

            String worldName = input.replaceAll("[^A-Za-z\\d/_-]", "").replace(" ", "_").trim();
            if (worldName.isEmpty()) {
                Messages.sendMessage(player, "worlds_world_creation_name_bank");
                return;
            }

            player.closeInventory();
            new BuildWorldCreator(plugin, worldName)
                    .setType(worldType)
                    .setTemplate(template)
                    .setPrivate(privateWorld)
                    .createWorld(player);
        });
    }

    /**
     * Checks if a world with the given name already exists.
     *
     * @param player    The player who is creating the world
     * @param worldName The name of the world
     * @return Whether if a world with the given name already exists
     */
    public boolean worldExists(Player player, String worldName) {
        boolean worldExists = getBuildWorld(worldName) != null;
        File worldFile = new File(Bukkit.getWorldContainer(), worldName);
        if (worldExists || worldFile.exists()) {
            Messages.sendMessage(player, "worlds_world_exists");
            XSound.ENTITY_ITEM_BREAK.play(player);
            return true;
        }
        return false;
    }

    /**
     * Import a {@link BuildWorld} from a world directory.
     *
     * @param player        The player who is creating the world
     * @param worldName     Name of the world that the chunk generator should be applied to.
     * @param generator     The generator type used by the world
     * @param generatorName The name of the custom generator if generator type is {@link Generator#CUSTOM}
     */
    public void importWorld(Player player, String worldName, Generator generator, String generatorName) {
        for (String charString : worldName.split("")) {
            if (charString.matches("[^A-Za-z\\d/_-]")) {
                Messages.sendMessage(player, "worlds_import_invalid_character",
                        new AbstractMap.SimpleEntry<>("%world%", worldName),
                        new AbstractMap.SimpleEntry<>("%char%", charString)
                );
                return;
            }
        }

        File file = new File(Bukkit.getWorldContainer(), worldName);
        if (!file.exists() || !file.isDirectory()) {
            Messages.sendMessage(player, "worlds_import_unknown_world");
            return;
        }

        if (getBuildWorld(worldName) != null) {
            Messages.sendMessage(player, "worlds_world_exists");
            return;
        }

        ChunkGenerator chunkGenerator = null;
        if (generator == Generator.CUSTOM) {
            String[] generatorInfo = generatorName.split(":");
            if (generatorInfo.length == 1) {
                generatorInfo = new String[]{generatorInfo[0], generatorInfo[0]};
            }

            chunkGenerator = getChunkGenerator(generatorInfo[0], generatorInfo[1], worldName);
            if (chunkGenerator == null) {
                Messages.sendMessage(player, "worlds_import_unknown_generator");
                return;
            }
        }

        BuildWorld buildWorld = new BuildWorld(
                worldName,
                "-",
                null,
                WorldType.IMPORTED,
                FileUtils.getDirectoryCreation(file),
                false,
                null
        );

        BuildWorldCreator worldCreator = new BuildWorldCreator(plugin, buildWorld).setCustomGenerator(new CustomGenerator(generatorName, chunkGenerator));
        if (worldCreator.parseDataVersion() > plugin.getServerVersion().getDataVersion()) {
            Messages.sendMessage(player, "worlds_import_newer_version", new AbstractMap.SimpleEntry<>("%world%", worldName));
            return;
        }

        Messages.sendMessage(player, "worlds_import_started", new AbstractMap.SimpleEntry<>("%world%", worldName));
        buildWorlds.add(buildWorld);
        worldCreator.createWorld(player);
        Messages.sendMessage(player, "worlds_import_finished");

        if (configValues.isTeleportAfterCreation()) {
            teleport(player, buildWorld);
        }
    }

    /**
     * Parse the {@link ChunkGenerator} for the generation of a {@link BuildWorld} with {@link WorldType#CUSTOM}
     *
     * @param generator   The plugin's (generator) name
     * @param generatorId Unique ID, if any, that was specified to indicate which generator was requested
     * @param worldName   Name of the world that the chunk generator should be applied to.
     */
    public ChunkGenerator getChunkGenerator(String generator, String generatorId, String worldName) {
        if (generator == null) {
            return null;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(generator);
        if (plugin == null) {
            return null;
        }

        return plugin.getDefaultWorldGenerator(worldName, generatorId);
    }

    /**
     * Import all {@link BuildWorld} from a given list of world names.
     *
     * @param player    The player who is creating the world
     * @param worldList The list of world to be imported
     */
    public void importWorlds(Player player, String[] worldList) {
        int worlds = worldList.length;
        int delay = configValues.getImportDelay();

        Messages.sendMessage(player, "worlds_importall_started", new AbstractMap.SimpleEntry<>("%amount%", String.valueOf(worlds)));
        Messages.sendMessage(player, "worlds_importall_delay", new AbstractMap.SimpleEntry<>("%delay%", String.valueOf(delay)));

        AtomicInteger worldsImported = new AtomicInteger(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                int i = worldsImported.getAndIncrement();

                String worldName = worldList[i];
                for (String charString : worldName.split("")) {
                    if (charString.matches("[^A-Za-z\\d/_-]")) {
                        Messages.sendMessage(player, "worlds_importall_invalid_character",
                                new AbstractMap.SimpleEntry<>("%world%", worldName),
                                new AbstractMap.SimpleEntry<>("%char%", charString)
                        );
                        return;
                    }
                }

                if (getBuildWorld(worldName) != null) {
                    Messages.sendMessage(player, "worlds_importall_world_already_imported", new AbstractMap.SimpleEntry<>("%world%", worldName));
                    return;
                }

                long creation = FileUtils.getDirectoryCreation(new File(Bukkit.getWorldContainer(), worldName));
                BuildWorld buildWorld = new BuildWorld(worldName, "-", null, WorldType.IMPORTED, creation, false, null);
                BuildWorldCreator worldCreator = new BuildWorldCreator(plugin, buildWorld);
                if (worldCreator.parseDataVersion() > plugin.getServerVersion().getDataVersion()) {
                    Messages.sendMessage(player, "worlds_importall_newer_version", new AbstractMap.SimpleEntry<>("%world%", worldName));
                    return;
                }

                buildWorlds.add(buildWorld);
                worldCreator.setType(WorldType.VOID).generateBukkitWorld();
                Messages.sendMessage(player, "worlds_importall_world_imported", new AbstractMap.SimpleEntry<>("%world%", worldName));

                if (worldsImported.get() >= worlds) {
                    this.cancel();
                    Messages.sendMessage(player, "worlds_importall_finished");
                }
            }
        }.runTaskTimer(plugin, 0, 20L * delay);
    }

    /**
     * Delete an existing {@link BuildWorld}.
     * In comparison to {@link #unimportWorld(BuildWorld, boolean)}, deleting a world deletes the world's directory.
     *
     * @param player     The player who issued the deletion
     * @param buildWorld The world to be deleted
     */
    public void deleteWorld(Player player, BuildWorld buildWorld) {
        if (!buildWorlds.contains(buildWorld)) {
            Messages.sendMessage(player, "worlds_delete_unknown_world");
            return;
        }

        String worldName = buildWorld.getName();
        File deleteFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!deleteFolder.exists()) {
            Messages.sendMessage(player, "worlds_delete_unknown_directory");
            return;
        }

        Messages.sendMessage(player, "worlds_delete_started", new AbstractMap.SimpleEntry<>("%world%", worldName));
        removePlayersFromWorld(worldName, Messages.getString("worlds_delete_players_world"));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            unimportWorld(buildWorld, false);
            FileUtils.deleteDirectory(deleteFolder);
            Messages.sendMessage(player, "worlds_delete_finished");
        }, 20L);
    }

    /**
     * Unimport an existing {@link BuildWorld}.
     * In comparison to {@link #deleteWorld(Player, BuildWorld)}, unimporting a world does not delete the world's directory.
     *
     * @param buildWorld The build world object
     * @param save       Should the world be saved before unimporting
     */
    public void unimportWorld(BuildWorld buildWorld, boolean save) {
        buildWorld.forceUnload(save);
        this.buildWorlds.remove(buildWorld);
        removePlayersFromWorld(buildWorld.getName(), Messages.getString("worlds_unimport_players_world"));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.worldConfig.getFile().set("worlds." + buildWorld.getName(), null);
            this.worldConfig.saveFile();
        });
    }

    /**
     * In order to properly unload/rename/delete a world, no players may be present in the {@link World}.
     * Removes all player's from the world to insure proper manipulation.
     *
     * @param worldName The name of the world
     * @param message   The message sent to a player when they are removed from the world
     * @return A list of all players who were teleported out of the world
     */
    private List<Player> removePlayersFromWorld(String worldName, String message) {
        List<Player> players = new ArrayList<>();

        World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            return players;
        }

        SpawnManager spawnManager = plugin.getSpawnManager();
        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation().add(0.5, 0, 0.5);

        Bukkit.getOnlinePlayers().forEach(player -> {
            World playerWorld = player.getWorld();
            if (!playerWorld.equals(bukkitWorld)) {
                return;
            }

            if (spawnManager.spawnExists()) {
                if (!spawnManager.getSpawnWorld().equals(playerWorld)) {
                    spawnManager.teleport(player);
                } else {
                    PaperLib.teleportAsync(player, spawnLocation);
                    spawnManager.remove();
                }
            } else {
                PaperLib.teleportAsync(player, spawnLocation);
            }

            player.sendMessage(message);
            players.add(player);
        });

        return players;
    }

    /**
     * Change the name of a {@link BuildWorld} to a given name.
     *
     * @param player     The player who issued the world rename
     * @param buildWorld The build world object
     * @param newName    The name the world should be renamed to
     */
    public void renameWorld(Player player, BuildWorld buildWorld, String newName) {
        String oldName = buildWorld.getName();
        if (oldName.equalsIgnoreCase(newName)) {
            Messages.sendMessage(player, "worlds_rename_same_name");
            return;
        }

        for (String charString : newName.split("")) {
            if (charString.matches("[^A-Za-z\\d/_-]")) {
                Messages.sendMessage(player, "worlds_world_creation_invalid_characters");
                break;
            }
        }

        player.closeInventory();
        String parsedNewName = newName.replaceAll("[^A-Za-z\\d/_-]", "").replace(" ", "_").trim();
        if (parsedNewName.isEmpty()) {
            Messages.sendMessage(player, "worlds_world_creation_name_bank");
            return;
        }

        if (Bukkit.getWorld(oldName) == null && !buildWorld.isLoaded()) {
            buildWorld.load();
        }

        World oldWorld = Bukkit.getWorld(oldName);
        if (oldWorld == null) {
            Messages.sendMessage(player, "worlds_rename_unknown_world");
            return;
        }

        List<Player> removedPlayers = removePlayersFromWorld(oldName, Messages.getString("worlds_rename_players_world"));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            oldWorld.save();
            Bukkit.getServer().unloadWorld(oldWorld, true);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                worldConfig.getFile().set("worlds." + parsedNewName, worldConfig.getFile().getConfigurationSection("worlds." + buildWorld.getName()));
                worldConfig.getFile().set("worlds." + oldName, null);
            });

            File oldWorldFile = new File(Bukkit.getWorldContainer(), oldName);
            File newWorldFile = new File(Bukkit.getWorldContainer(), parsedNewName);
            FileUtils.copy(oldWorldFile, newWorldFile);
            FileUtils.deleteDirectory(oldWorldFile);

            buildWorld.setName(parsedNewName);
            World newWorld = new BuildWorldCreator(plugin, buildWorld).generateBukkitWorld(false);
            Location spawnLocation = oldWorld.getSpawnLocation();
            spawnLocation.setWorld(newWorld);

            removedPlayers.stream()
                    .filter(Objects::nonNull)
                    .forEach(pl -> PaperLib.teleportAsync(pl, spawnLocation.add(0.5, 0, 0.5)));
            removedPlayers.clear();

            SpawnManager spawnManager = plugin.getSpawnManager();
            if (spawnManager.spawnExists() && Objects.equals(spawnManager.getSpawnWorld(), oldWorld)) {
                Location oldSpawn = spawnManager.getSpawn();
                Location newSpawn = new Location(spawnLocation.getWorld(), oldSpawn.getX(), oldSpawn.getY(), oldSpawn.getZ(), oldSpawn.getYaw(), oldSpawn.getPitch());
                spawnManager.set(newSpawn, newSpawn.getWorld().getName());
            }

            Messages.sendMessage(player, "worlds_rename_set",
                    new AbstractMap.SimpleEntry<>("%oldName%", oldName),
                    new AbstractMap.SimpleEntry<>("%newName%", parsedNewName)
            );
        }, 20L);
    }

    /**
     * Teleport a player to a {@link BuildWorld}.
     *
     * @param player     The player to be teleported
     * @param buildWorld The build world object
     */
    public void teleport(Player player, BuildWorld buildWorld) {
        boolean hadToLoad = false;
        if (configValues.isUnloadWorlds() && !buildWorld.isLoaded()) {
            buildWorld.load(player);
            hadToLoad = true;
        }

        World bukkitWorld = Bukkit.getServer().getWorld(buildWorld.getName());
        if (bukkitWorld == null) {
            Messages.sendMessage(player, "worlds_tp_unknown_world");
            return;
        }

        Location location = bukkitWorld.getSpawnLocation().add(0.5, 0, 0.5);
        if (buildWorld.getCustomSpawn() == null) {
            switch (buildWorld.getType()) {
                case NETHER:
                case END:
                    Location blockLocation = null;
                    for (int y = 0; y < bukkitWorld.getMaxHeight(); y++) {
                        Block block = bukkitWorld.getBlockAt(location.getBlockX(), y, location.getBlockZ());
                        if (isSafeLocation(block.getLocation())) {
                            blockLocation = block.getLocation();
                            break;
                        }
                    }
                    if (blockLocation != null) {
                        location = new Location(bukkitWorld, blockLocation.getBlockX() + 0.5, blockLocation.getBlockY() + 1, blockLocation.getBlockZ() + 0.5);
                    }
                    break;
                default:
                    break;
            }
        } else {
            String[] spawnString = buildWorld.getCustomSpawn().split(";");
            location = new Location(bukkitWorld, Double.parseDouble(spawnString[0]), Double.parseDouble(spawnString[1]), Double.parseDouble(spawnString[2]), Float.parseFloat(spawnString[3]), Float.parseFloat(spawnString[4]));
        }

        Location finalLocation = location;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PaperLib.teleportAsync(player, finalLocation);
            Titles.clearTitle(player);
            XSound.ENTITY_ENDERMAN_TELEPORT.play(player);
        }, hadToLoad ? 20L : 0L);
    }

    /**
     * In order to correctly teleport a player to a {@link Location}, the block underneath the player's feet must be solid.
     *
     * @param location The location the player will be teleported to
     */
    public boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        if (feet.getType() != Material.AIR && feet.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR) {
            return false;
        }

        Block head = feet.getRelative(BlockFace.UP);
        if (head.getType() != Material.AIR) {
            return false;
        }

        Block ground = feet.getRelative(BlockFace.DOWN);
        return ground.getType().isSolid();
    }

    public boolean canEnter(Player player, BuildWorld buildWorld) {
        if (player.hasPermission(BuildSystem.ADMIN_PERMISSION)) {
            return true;
        }

        if (buildWorld.getPermission().equals("-")) {
            return true;
        }

        if (buildWorld.isCreator(player) || buildWorld.isBuilder(player)) {
            return true;
        }

        return player.hasPermission(buildWorld.getPermission());
    }

    public boolean canBypassBuildRestriction(Player player) {
        return player.hasPermission(BuildSystem.ADMIN_PERMISSION)
                || player.hasPermission("buildsystem.bypass.archive")
                || plugin.getPlayerManager().isInBuildMode(player);
    }

    /**
     * Gets whether the given player is permitted to run a command in the given world.
     * <p>
     * <ul>
     *   <li>The creator of a world is allowed to run the command if they have the given permission, optionally ending with {@code .self}.</li>
     *   <li>All other players will need the permission {@code <permission>.other} to run the command.</li>
     * </ul>
     *
     * @param player     The player trying to run the command
     * @param permission The permission needed to run the command
     * @param worldName  The name of the world the player wants to run the command on
     * @return {@code true} if the player is allowed to run the command, {@code false} otherwise
     */
    public boolean isPermitted(Player player, String permission, String worldName) {
        if (player.hasPermission(BuildSystem.ADMIN_PERMISSION)) {
            return true;
        }

        BuildWorld buildWorld = getBuildWorld(worldName);
        if (buildWorld == null) {
            // Most command require the world to be non-null.
            // Nevertheless, return true to allow a "world is null" message to be sent.
            return true;
        }

        if (buildWorld.isCreator(player)) {
            return (player.hasPermission(permission + ".self") || player.hasPermission(permission));
        }

        return player.hasPermission(permission + ".other");
    }

    public void save() {
        buildWorlds.forEach(worldConfig::saveWorld);
    }

    public void load() {
        FileConfiguration configuration = worldConfig.getFile();
        if (configuration == null) {
            return;
        }

        ConfigurationSection configurationSection = configuration.getConfigurationSection("worlds");
        if (configurationSection == null) {
            return;
        }

        Set<String> worlds = configurationSection.getKeys(false);
        if (worlds.isEmpty()) {
            return;
        }

        worlds.forEach(this::loadWorld);
        worldConfig.loadWorlds(this);
    }

    public void loadWorld(String worldName) {
        FileConfiguration configuration = worldConfig.getFile();
        if (configuration == null) {
            return;
        }

        String creator = configuration.isString("worlds." + worldName + ".creator") ? configuration.getString("worlds." + worldName + ".creator") : "-";
        UUID creatorId = parseCreatorId(configuration, worldName, creator);
        WorldType worldType = configuration.isString("worlds." + worldName + ".type") ? WorldType.valueOf(configuration.getString("worlds." + worldName + ".type")) : WorldType.UNKNOWN;
        boolean privateWorld = configuration.isBoolean("worlds." + worldName + ".private") && configuration.getBoolean("worlds." + worldName + ".private");
        XMaterial material = parseMaterial(configuration, worldName);
        WorldStatus worldStatus = WorldStatus.valueOf(configuration.getString("worlds." + worldName + ".status"));
        String project = configuration.getString("worlds." + worldName + ".project");
        String permission = configuration.getString("worlds." + worldName + ".permission");
        long date = configuration.isLong("worlds." + worldName + ".date") ? configuration.getLong("worlds." + worldName + ".date") : -1;
        boolean physics = configuration.getBoolean("worlds." + worldName + ".physics");
        boolean explosions = !configuration.isBoolean("worlds." + worldName + ".explosions") || configuration.getBoolean("worlds." + worldName + ".explosions");
        boolean mobAI = !configuration.isBoolean("worlds." + worldName + ".mobai") || configuration.getBoolean("worlds." + worldName + ".mobai");
        String customSpawn = configuration.getString("worlds." + worldName + ".spawn");
        boolean blockBreaking = !configuration.isBoolean("worlds." + worldName + ".block-breaking") || configuration.getBoolean("worlds." + worldName + ".block-breaking");
        boolean blockPlacement = !configuration.isBoolean("worlds." + worldName + ".block-placement") || configuration.getBoolean("worlds." + worldName + ".block-placement");
        boolean blockInteractions = !configuration.isBoolean("worlds." + worldName + ".block-interactions") || configuration.getBoolean("worlds." + worldName + ".block-interactions");
        boolean buildersEnabled = configuration.isBoolean("worlds." + worldName + ".builders-enabled") && configuration.getBoolean("worlds." + worldName + ".builders-enabled");
        Difficulty difficulty = Difficulty.valueOf(configuration.getString("worlds." + worldName + ".difficulty", "PEACEFUL").toUpperCase());
        List<Builder> builders = parseBuilders(configuration, worldName);
        String generatorName = configuration.getString("worlds." + worldName + ".chunk-generator");
        CustomGenerator customGenerator = new CustomGenerator(generatorName, parseChunkGenerator(worldName, generatorName));

        this.buildWorlds.add(new BuildWorld(
                worldName,
                creator,
                creatorId,
                worldType,
                privateWorld,
                material,
                worldStatus,
                project,
                permission,
                date,
                physics,
                explosions,
                mobAI,
                customSpawn,
                blockBreaking,
                blockPlacement,
                blockInteractions,
                buildersEnabled,
                difficulty,
                builders,
                customGenerator
        ));
    }

    private XMaterial parseMaterial(FileConfiguration configuration, String worldName) {
        String itemString = configuration.getString("worlds." + worldName + ".item");
        if (itemString == null) {
            itemString = XMaterial.BEDROCK.name();
            plugin.getLogger().warning("Unknown material found for \"" + worldName + "\" (" + itemString + ").");
            plugin.getLogger().warning("Defaulting back to BEDROCK.");
        }

        Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(itemString);
        if (xMaterial.isPresent()) {
            return xMaterial.get();
        } else {
            plugin.getLogger().warning("Unknown material found for \"" + worldName + "\" (" + itemString + ").");
            plugin.getLogger().warning("Defaulting back to BEDROCK.");
            return XMaterial.BEDROCK;
        }
    }

    private UUID parseCreatorId(FileConfiguration configuration, String worldName, String creator) {
        String path = "worlds." + worldName + ".creator-id";
        String id = configuration.isString(path) ? configuration.getString(path) : null;

        if (id == null || id.equalsIgnoreCase("null")) {
            if (!creator.equals("-")) {
                return UUIDFetcher.getUUID(creator);
            } else {
                return null;
            }
        } else {
            return UUID.fromString(id);
        }
    }

    private List<Builder> parseBuilders(FileConfiguration configuration, String worldName) {
        List<Builder> builders = new ArrayList<>();

        if (configuration.isString("worlds." + worldName + ".builders")) {
            String buildersString = configuration.getString("worlds." + worldName + ".builders");
            if (buildersString != null && !buildersString.isEmpty()) {
                String[] splitBuilders = buildersString.split(";");
                for (String builder : splitBuilders) {
                    String[] information = builder.split(",");
                    builders.add(new Builder(UUID.fromString(information[0]), information[1]));
                }
            }
        }

        return builders;
    }

    /**
     * @author Ein_Jojo, einTosti
     */
    @Nullable
    private ChunkGenerator parseChunkGenerator(String worldName, String generatorName) {
        if (generatorName == null) {
            return null;
        }

        String[] generatorInfo = generatorName.split(":");
        if (generatorInfo.length == 1) {
            generatorInfo = new String[]{generatorInfo[0], generatorInfo[0]};
        }

        return getChunkGenerator(generatorInfo[0], generatorInfo[1], worldName);
    }
}