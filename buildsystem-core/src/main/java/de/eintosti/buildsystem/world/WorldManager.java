/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.world;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import de.eintosti.buildsystem.BuildSystem;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.config.ConfigValues;
import de.eintosti.buildsystem.config.WorldConfig;
import de.eintosti.buildsystem.navigator.inventory.FilteredWorldsInventory.Visibility;
import de.eintosti.buildsystem.util.FileUtils;
import de.eintosti.buildsystem.util.PlayerChatInput;
import de.eintosti.buildsystem.util.UUIDFetcher;
import de.eintosti.buildsystem.world.data.WorldData;
import de.eintosti.buildsystem.world.data.WorldStatus;
import de.eintosti.buildsystem.world.data.WorldType;
import de.eintosti.buildsystem.world.generator.CustomGenerator;
import de.eintosti.buildsystem.world.generator.Generator;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WorldManager {

    private final BuildSystem plugin;
    private final ConfigValues configValues;
    private final WorldConfig worldConfig;

    private final Map<String, BuildWorld> buildWorlds;

    private static boolean importingAllWorlds = false;

    public WorldManager(BuildSystem plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.worldConfig = new WorldConfig(plugin);

        this.buildWorlds = new HashMap<>();
    }

    /**
     * Gets the {@link BuildWorld} by the given name.
     *
     * @param worldName The name of the world
     * @return The world object if one was found, {@code null} otherwise
     */
    public BuildWorld getBuildWorld(String worldName) {
        return this.buildWorlds.get(worldName);
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
     * Adds a {@link BuildWorld} to the list of all worlds.
     *
     * @param buildWorld The world to add
     */
    public void addBuildWorld(BuildWorld buildWorld) {
        this.buildWorlds.put(buildWorld.getName(), buildWorld);
    }

    /**
     * Removes a {@link BuildWorld} from the list of all worlds.
     *
     * @param buildWorld The world to remove
     */
    public void removeBuildWorld(BuildWorld buildWorld) {
        this.buildWorlds.remove(buildWorld.getName());
    }

    /**
     * Gets a list of all {@link BuildWorld}s.
     *
     * @return A list of all worlds
     */
    @Unmodifiable
    public Collection<BuildWorld> getBuildWorlds() {
        return Collections.unmodifiableCollection(buildWorlds.values());
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
                .filter(buildWorld -> isCorrectVisibility(buildWorld.getData().privateWorld().get(), visibility))
                .collect(Collectors.toList());
    }

    /**
     * Gets if a {@link BuildWorld}'s visibility is equal to the given visibility.
     *
     * @param privateWorld Whether the world is private
     * @param visibility   The visibility the world should have
     * @return {@code true} if the world's visibility is equal to the given visibility, otherwise {@code false}
     */
    public boolean isCorrectVisibility(boolean privateWorld, Visibility visibility) {
        switch (visibility) {
            case PRIVATE:
                return privateWorld;
            case PUBLIC:
                return !privateWorld;
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
        player.closeInventory();
        new PlayerChatInput(plugin, player, "enter_world_name", input -> {
            if (Arrays.stream(input.split("")).anyMatch(c -> c.matches("[^A-Za-z\\d/_-]") || c.matches(configValues.getInvalidNameCharacters()))) {
                Messages.sendMessage(player, "worlds_world_creation_invalid_characters");
            }
            String worldName = input
                    .replaceAll("[^A-Za-z\\d/_-]", "")
                    .replaceAll(configValues.getInvalidNameCharacters(), "")
                    .replace(" ", "_")
                    .trim();
            if (worldName.isEmpty()) {
                Messages.sendMessage(player, "worlds_world_creation_name_bank");
                return;
            }

            if (worldType == WorldType.CUSTOM) {
                startCustomGeneratorInput(player, worldName, template, privateWorld);
            } else {
                createWorld(player, worldName, worldType, null, template, privateWorld);
            }
        });
    }

    private void startCustomGeneratorInput(Player player, String worldName, String template, boolean privateWorld) {
        new PlayerChatInput(plugin, player, "enter_generator_name", input -> {
            String[] generatorInfo = input.split(":");
            if (generatorInfo.length == 1) {
                generatorInfo = new String[]{generatorInfo[0], generatorInfo[0]};
            }

            ChunkGenerator chunkGenerator = getChunkGenerator(generatorInfo[0], generatorInfo[1], worldName);
            if (chunkGenerator == null) {
                Messages.sendMessage(player, "worlds_import_unknown_generator");
                XSound.ENTITY_ITEM_BREAK.play(player);
                return;
            }

            CustomGenerator customGenerator = new CustomGenerator(generatorInfo[0], chunkGenerator);
            plugin.getLogger().info("Using custom world generator: " + customGenerator.getName());
            createWorld(player, worldName, WorldType.CUSTOM, customGenerator, template, privateWorld);
        });
    }

    private void createWorld(Player player, String worldName, WorldType worldType, CustomGenerator customGenerator, String template, boolean privateWorld) {
        new BuildWorldCreator(plugin, worldName)
                .setType(worldType)
                .setTemplate(template)
                .setPrivate(privateWorld)
                .setCustomGenerator(customGenerator)
                .createWorld(player);
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
     * Gets the {@link ChunkGenerator} for the generation of a {@link BuildWorld} with {@link WorldType#CUSTOM}
     *
     * @param generator   The plugin's (generator) name
     * @param generatorId Unique ID, if any, that was specified to indicate which generator was requested
     * @param worldName   Name of the world that the chunk generator should be applied to.
     */
    @Nullable
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
     * Import a {@link BuildWorld} from a world directory.
     *
     * @param player        The player who is creating the world
     * @param worldName     Name of the world that the chunk generator should be applied to.
     * @param creator       The builder who should be set as the creator
     * @param generator     The generator type used by the world
     * @param generatorName The name of the custom generator if generator type is {@link Generator#CUSTOM}
     * @param single        Is only one world being imported? Used for message sent to the player
     * @return {@code true} if the world was successfully imported, otherwise {@code false}
     */
    public boolean importWorld(Player player, String worldName, Builder creator, Generator generator, String generatorName, boolean single) {
        ChunkGenerator chunkGenerator = null;
        if (generator == Generator.CUSTOM) {
            String[] generatorInfo = generatorName.split(":");
            if (generatorInfo.length == 1) {
                generatorInfo = new String[]{generatorInfo[0], generatorInfo[0]};
            }

            chunkGenerator = getChunkGenerator(generatorInfo[0], generatorInfo[1], worldName);
            if (chunkGenerator == null) {
                Messages.sendMessage(player, "worlds_import_unknown_generator");
                return false;
            }
        }

        BuildWorldCreator worldCreator = new BuildWorldCreator(plugin, worldName)
                .setType(WorldType.IMPORTED)
                .setCreator(creator)
                .setCustomGenerator(new CustomGenerator(generatorName, chunkGenerator))
                .setPrivate(false)
                .setCreationDate(FileUtils.getDirectoryCreation(new File(Bukkit.getWorldContainer(), worldName)));

        if (worldCreator.isHigherVersion()) {
            String key = single ? "import" : "importall";
            Messages.sendMessage(player, "worlds_" + key + "_newer_version", new AbstractMap.SimpleEntry<>("%world%", worldName));
            return false;
        }

        worldCreator.importWorld(player, single);
        return true;
    }

    /**
     * Import all {@link BuildWorld} from a given list of world names.
     *
     * @param player    The player who is creating the world
     * @param creator   The player who should be set as the creator of the world
     * @param worldList The list of world to be imported
     */
    public void importWorlds(Player player, String[] worldList, Generator generator, Builder creator) {
        int worlds = worldList.length;
        int delay = configValues.getImportDelay();

        Messages.sendMessage(player, "worlds_importall_started", new AbstractMap.SimpleEntry<>("%amount%", String.valueOf(worlds)));
        Messages.sendMessage(player, "worlds_importall_delay", new AbstractMap.SimpleEntry<>("%delay%", String.valueOf(delay)));
        importingAllWorlds = true;

        AtomicInteger worldsImported = new AtomicInteger(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                int i = worldsImported.getAndIncrement();
                if (i >= worlds) {
                    this.cancel();
                    importingAllWorlds = false;
                    Messages.sendMessage(player, "worlds_importall_finished");
                    return;
                }

                String worldName = worldList[i];
                if (getBuildWorld(worldName) != null) {
                    Messages.sendMessage(player, "worlds_importall_world_already_imported", new AbstractMap.SimpleEntry<>("%world%", worldName));
                    return;
                }

                String invalidChar = Arrays.stream(worldName.split(""))
                        .filter(c -> c.matches("[^A-Za-z\\d/_-]") || c.matches(plugin.getConfigValues().getInvalidNameCharacters()))
                        .findFirst()
                        .orElse(null);
                if (invalidChar != null) {
                    Messages.sendMessage(player, "worlds_importall_invalid_character",
                            new AbstractMap.SimpleEntry<>("%world%", worldName),
                            new AbstractMap.SimpleEntry<>("%char%", invalidChar)
                    );
                    return;
                }

                if (importWorld(player, worldName, creator, generator, null, false)) {
                    Messages.sendMessage(player, "worlds_importall_world_imported", new AbstractMap.SimpleEntry<>("%world%", worldName));
                }
            }
        }.runTaskTimer(plugin, 0, 20L * delay);
    }

    public boolean isImportingAllWorlds() {
        return importingAllWorlds;
    }

    /**
     * Delete an existing {@link BuildWorld}.
     * In comparison to {@link #unimportWorld(BuildWorld, boolean)}, deleting a world deletes the world's directory.
     *
     * @param player     The player who issued the deletion
     * @param buildWorld The world to be deleted
     */
    public void deleteWorld(Player player, BuildWorld buildWorld) {
        if (!buildWorlds.containsValue(buildWorld)) {
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
        this.buildWorlds.remove(buildWorld.getName());
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
        player.closeInventory();

        String oldName = buildWorld.getName();
        if (oldName.equalsIgnoreCase(newName)) {
            Messages.sendMessage(player, "worlds_rename_same_name");
            return;
        }

        if (Arrays.stream(newName.split("")).anyMatch(c -> c.matches("[^A-Za-z\\d/_-]") || c.matches(configValues.getInvalidNameCharacters()))) {
            Messages.sendMessage(player, "worlds_world_creation_invalid_characters");
        }
        String parsedNewName = newName
                .replaceAll("[^A-Za-z\\d/_-]", "")
                .replaceAll(configValues.getInvalidNameCharacters(), "")
                .replace(" ", "_")
                .trim();
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
        for (Chunk chunk : oldWorld.getLoadedChunks()) {
            chunk.unload(true);
        }
        Bukkit.unloadWorld(oldWorld, true);
        Bukkit.getWorlds().remove(oldWorld);
        this.buildWorlds.remove(oldName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            worldConfig.getFile().set("worlds." + parsedNewName, worldConfig.getFile().getConfigurationSection("worlds." + buildWorld.getName()));
            worldConfig.getFile().set("worlds." + oldName, null);
        });

        File oldWorldFile = new File(Bukkit.getWorldContainer(), oldName);
        File newWorldFile = new File(Bukkit.getWorldContainer(), parsedNewName);
        FileUtils.copy(oldWorldFile, newWorldFile);
        FileUtils.deleteDirectory(oldWorldFile);

        buildWorld.setName(parsedNewName);
        this.addBuildWorld(buildWorld);
        World newWorld = new BuildWorldCreator(plugin, buildWorld).generateBukkitWorld(false);
        Location spawnLocation = oldWorld.getSpawnLocation();
        spawnLocation.setWorld(newWorld);

        removedPlayers.stream()
                .filter(Objects::nonNull)
                .forEach(pl -> PaperLib.teleportAsync(pl, spawnLocation.add(0.5, 0, 0.5)));

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
        Location customSpawn = buildWorld.getData().getCustomSpawnLocation();
        if (customSpawn != null) {
            location = customSpawn;
        } else {
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
        }

        Location finalLocation = location;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PaperLib.teleportAsync(player, finalLocation).whenComplete((completed, throwable) -> {
                if (!completed) {
                    return;
                }

                Titles.clearTitle(player);
                player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

                if (!finalLocation.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
                    player.setFlying(player.getAllowFlight());
                }
            });
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

        String permission = buildWorld.getData().permission().get();
        if (permission.equals("-")) {
            return true;
        }

        if (buildWorld.isCreator(player) || buildWorld.isBuilder(player)) {
            return true;
        }

        return player.hasPermission(permission);
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
        worldConfig.saveWorlds(getBuildWorlds());
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
        WorldData worldData = parseWorldData(configuration, worldName);
        long creationDate = configuration.isLong("worlds." + worldName + ".date") ? configuration.getLong("worlds." + worldName + ".date") : -1;
        List<Builder> builders = parseBuilders(configuration, worldName);
        String generatorName = configuration.getString("worlds." + worldName + ".chunk-generator");
        CustomGenerator customGenerator = new CustomGenerator(generatorName, parseChunkGenerator(worldName, generatorName));

        this.addBuildWorld(new BuildWorld(
                worldName,
                creator,
                creatorId,
                worldType,
                worldData,
                creationDate,
                customGenerator,
                builders
        ));
    }

    private WorldData parseWorldData(FileConfiguration configuration, String worldName) {
        final String path = "worlds." + worldName + ".data";
        // Load legacy configurations
        if (configuration.getString(path) == null) {
            String customSpawn = configuration.getString("worlds." + worldName + ".spawn");
            String permission = configuration.getString("worlds." + worldName + ".permission");
            String project = configuration.getString("worlds." + worldName + ".project");

            Difficulty difficulty = Difficulty.valueOf(configuration.getString("worlds." + worldName + ".difficulty", "PEACEFUL").toUpperCase());
            XMaterial material = parseMaterial(configuration, "worlds." + worldName + ".item", worldName);
            WorldStatus worldStatus = WorldStatus.valueOf(configuration.getString("worlds." + worldName + ".status"));

            boolean blockBreaking = !configuration.isBoolean("worlds." + worldName + ".block-breaking") || configuration.getBoolean("worlds." + worldName + ".block-breaking");
            boolean blockInteractions = !configuration.isBoolean("worlds." + worldName + ".block-interactions") || configuration.getBoolean("worlds." + worldName + ".block-interactions");
            boolean blockPlacement = !configuration.isBoolean("worlds." + worldName + ".block-placement") || configuration.getBoolean("worlds." + worldName + ".block-placement");
            boolean buildersEnabled = configuration.isBoolean("worlds." + worldName + ".builders-enabled") && configuration.getBoolean("worlds." + worldName + ".builders-enabled");
            boolean explosions = !configuration.isBoolean("worlds." + worldName + ".explosions") || configuration.getBoolean("worlds." + worldName + ".explosions");
            boolean mobAi = !configuration.isBoolean("worlds." + worldName + ".mobai") || configuration.getBoolean("worlds." + worldName + ".mobai");
            boolean physics = configuration.getBoolean("worlds." + worldName + ".physics");
            boolean privateWorld = configuration.isBoolean("worlds." + worldName + ".private") && configuration.getBoolean("worlds." + worldName + ".private");

            return new WorldData(
                    worldName,
                    customSpawn, permission, project, difficulty, material, worldStatus, blockBreaking, blockInteractions,
                    blockPlacement, buildersEnabled, explosions, mobAi, physics, privateWorld, -1, -1, -1
            );
        }

        String customSpawn = configuration.getString("worlds." + worldName + ".spawn");
        String permission = configuration.getString(path + ".permission");
        String project = configuration.getString(path + ".project");

        Difficulty difficulty = Difficulty.valueOf(configuration.getString(path + ".difficulty").toUpperCase());
        XMaterial material = parseMaterial(configuration, path + ".material", worldName);
        WorldStatus worldStatus = WorldStatus.valueOf(configuration.getString(path + ".status"));

        boolean blockBreaking = configuration.getBoolean(path + ".block-breaking");
        boolean blockInteractions = configuration.getBoolean(path + ".block-interactions");
        boolean blockPlacement = configuration.getBoolean(path + ".block-placement");
        boolean buildersEnabled = configuration.getBoolean(path + ".builders-enabled");
        boolean explosions = configuration.getBoolean(path + ".explosions");
        boolean mobAi = configuration.getBoolean(path + ".mob-ai");
        boolean physics = configuration.getBoolean(path + ".physics");
        boolean privateWorld = configuration.getBoolean(path + ".private");

        long lastEdited = configuration.getLong(path + ".last-edited");
        long lastLoaded = configuration.getLong(path + ".last-loaded");
        long lastUnloaded = configuration.getLong(path + ".last-unloaded");

        return new WorldData(
                worldName,
                customSpawn, permission, project, difficulty, material, worldStatus, blockBreaking, blockInteractions,
                blockPlacement, buildersEnabled, explosions, mobAi, physics, privateWorld, lastEdited, lastLoaded, lastUnloaded
        );
    }

    private XMaterial parseMaterial(FileConfiguration configuration, String path, String worldName) {
        String itemString = configuration.getString(path);
        if (itemString == null) {
            itemString = XMaterial.BEDROCK.name();
            plugin.getLogger().warning("Could not find material for \"" + worldName + "\". Defaulting to BEDROCK.");
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
        final String path = "worlds." + worldName + ".creator-id";
        final String id = configuration.isString(path) ? configuration.getString(path) : null;

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