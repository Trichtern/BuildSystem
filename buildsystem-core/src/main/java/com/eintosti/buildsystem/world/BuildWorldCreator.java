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
import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.Messages;
import com.eintosti.buildsystem.config.ConfigValues;
import com.eintosti.buildsystem.util.FileUtils;
import com.eintosti.buildsystem.util.external.PlayerChatInput;
import com.eintosti.buildsystem.world.data.WorldType;
import com.eintosti.buildsystem.world.generator.CustomGenerator;
import com.eintosti.buildsystem.world.generator.voidgenerator.DeprecatedVoidGenerator;
import com.eintosti.buildsystem.world.generator.voidgenerator.ModernVoidGenerator;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;

/**
 * @author Trichtern
 * @since 2.21.0
 */
public class BuildWorldCreator {

    private final BuildSystem plugin;
    private final WorldManager worldManager;

    private String worldName;
    private String template = null;
    private WorldType worldType = WorldType.NORMAL;
    private CustomGenerator customGenerator = null;
    private boolean privateWorld = false;
    private Difficulty difficulty;

    public BuildWorldCreator(BuildSystem plugin, @NotNull String name) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();

        setName(name);
        setDifficulty(plugin.getConfigValues().getWorldDifficulty());
    }

    public BuildWorldCreator(BuildSystem plugin, BuildWorld buildWorld) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();

        setName(buildWorld.getName());
        setType(buildWorld.getType());
        setCustomGenerator(buildWorld.getCustomGenerator());
        setPrivate(buildWorld.isPrivate());
        setDifficulty(buildWorld.getDifficulty());
    }

    public BuildWorldCreator setName(String name) {
        this.worldName = name;
        return this;
    }

    public BuildWorldCreator setTemplate(String template) {
        this.template = ChatColor.stripColor(template);
        return this;
    }

    public BuildWorldCreator setType(WorldType type) {
        this.worldType = type;
        return this;
    }

    public BuildWorldCreator setCustomGenerator(CustomGenerator customGenerator) {
        this.customGenerator = customGenerator;
        return this;
    }

    public BuildWorldCreator setPrivate(boolean privateWorld) {
        this.privateWorld = privateWorld;
        return this;
    }

    public BuildWorldCreator setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    /**
     * Depending on the {@link BuildWorld}'s {@link WorldType}, the corresponding {@link World} will be generated in a different way.
     * Then, if the creation of the world was successful and the config is set accordingly, the player is teleported to the world.
     *
     * @param player The player who is creating the world
     */
    public void createWorld(Player player) {
        switch (worldType) {
            default:
                if (!createPredefinedWorld(player)) {
                    return;
                }
                break;
            case CUSTOM:
                if (!createCustomWorld(player)) {
                    return;
                }
                break;
            case TEMPLATE:
                if (!createTemplateWorld(player)) {
                    return;
                }
                break;
        }
    }

    /**
     * Generate a {@link BuildWorld} with a predefined generator.
     *
     * @param player The player who is creating the world
     * @return {@code true} if the world was successfully created, {@code false otherwise}
     */
    private boolean createPredefinedWorld(Player player) {
        if (worldManager.worldExists(player, worldName)) {
            return false;
        }

        BuildWorld buildWorld = new BuildWorld(
                worldName,
                player.getName(),
                player.getUniqueId(),
                worldType,
                System.currentTimeMillis(),
                privateWorld,
                null
        );
        worldManager.getBuildWorlds().add(buildWorld);

        Messages.sendMessage(player, "worlds_world_creation_started",
                new AbstractMap.SimpleEntry<>("%world%", worldName),
                new AbstractMap.SimpleEntry<>("%type%", worldType.getName())
        );
        finishPreparationsAndGenerate(buildWorld);
        teleportAfterCreation(player);
        Messages.sendMessage(player, "worlds_creation_finished");
        return true;
    }

    /**
     * Generate a {@link BuildWorld} with a custom generator.
     *
     * @param player The player who is creating the world
     * @return {@code true} if the world was successfully created, {@code false otherwise}
     * @author Ein_Jojo
     */
    private boolean createCustomWorld(Player player) {
        if (worldManager.worldExists(player, worldName)) {
            return false;
        }

        new PlayerChatInput(plugin, player, "enter_generator_name", input -> {
            String[] generatorInfo = input.split(":");
            if (generatorInfo.length == 1) {
                generatorInfo = new String[]{generatorInfo[0], generatorInfo[0]};
            }

            ChunkGenerator chunkGenerator = worldManager.getChunkGenerator(generatorInfo[0], generatorInfo[1], worldName);
            if (chunkGenerator == null) {
                Messages.sendMessage(player, "worlds_import_unknown_generator");
                XSound.ENTITY_ITEM_BREAK.play(player);
                return;
            }

            this.customGenerator = new CustomGenerator(generatorInfo[0], chunkGenerator);
            plugin.getLogger().info("Using custom world generator: " + customGenerator.getName());

            worldManager.getBuildWorlds().add(new BuildWorld(
                    worldName,
                    player.getName(),
                    player.getUniqueId(),
                    WorldType.CUSTOM,
                    System.currentTimeMillis(),
                    privateWorld,
                    customGenerator
            ));

            Messages.sendMessage(player, "worlds_world_creation_started",
                    new AbstractMap.SimpleEntry<>("%world%", worldName),
                    new AbstractMap.SimpleEntry<>("%type%", worldType.getName())
            );
            generateBukkitWorld();
            teleportAfterCreation(player);
            Messages.sendMessage(player, "worlds_creation_finished");
        });
        return true;
    }

    /**
     * Generate a {@link BuildWorld} with a template.
     *
     * @param player The player who is creating the world
     * @return {@code true} if the world was successfully created, {@code false otherwise}
     */
    private boolean createTemplateWorld(Player player) {
        boolean worldExists = worldManager.getBuildWorld(worldName) != null;
        File worldFile = new File(Bukkit.getWorldContainer(), worldName);
        if (worldExists || worldFile.exists()) {
            Messages.sendMessage(player, "worlds_world_exists");
            return false;
        }

        File templateFile = new File(plugin.getDataFolder() + File.separator + "templates" + File.separator + template);
        if (!templateFile.exists()) {
            Messages.sendMessage(player, "worlds_template_does_not_exist");
            return false;
        }

        BuildWorld buildWorld = new BuildWorld(
                worldName,
                player.getName(),
                player.getUniqueId(),
                WorldType.TEMPLATE,
                System.currentTimeMillis(),
                privateWorld,
                null
        );
        worldManager.getBuildWorlds().add(buildWorld);

        Messages.sendMessage(player, "worlds_template_creation_started",
                new AbstractMap.SimpleEntry<>("%world%", worldName),
                new AbstractMap.SimpleEntry<>("%template%", template)
        );
        FileUtils.copy(templateFile, worldFile);
        Bukkit.createWorld(WorldCreator.name(worldName)
                .type(org.bukkit.WorldType.FLAT)
                .generateStructures(false));
        teleportAfterCreation(player);
        Messages.sendMessage(player, "worlds_creation_finished");
        return true;
    }

    /**
     * Certain {@link WorldType}s require modifications to the world after its generation.
     *
     * @param buildWorld The build world object
     */
    private void finishPreparationsAndGenerate(BuildWorld buildWorld) {
        WorldType worldType = buildWorld.getType();
        World bukkitWorld = generateBukkitWorld();
        if (bukkitWorld == null) {
            return;
        }

        switch (worldType) {
            case VOID:
                if (plugin.getConfigValues().isVoidBlock()) {
                    bukkitWorld.getBlockAt(0, 64, 0).setType(Material.GOLD_BLOCK);
                }
                bukkitWorld.setSpawnLocation(0, 65, 0);
                break;
            case FLAT:
                int y = XMaterial.supports(18) ? -60 : 4;
                bukkitWorld.setSpawnLocation(0, y, 0);
                break;
            default:
                break;
        }
    }

    @Nullable
    public World generateBukkitWorld() {
        return generateBukkitWorld(true);
    }

    /**
     * Generate the {@link World} linked to a {@link BuildWorld}.
     *
     * @param checkVersion Should the world version be checked
     * @return The world object
     */
    @Nullable
    public World generateBukkitWorld(boolean checkVersion) {
        if (checkVersion && !Boolean.getBoolean("Paper.ignoreWorldDataVersion")) {
            int worldVersion = parseDataVersion();
            int serverVersion = plugin.getServerVersion().getDataVersion();
            if (worldVersion > serverVersion) {
                plugin.getLogger().warning(String.format("\"%s\" was created in a newer version of Minecraft (%s > %s). Skipping...", worldName, worldVersion, serverVersion));
                return null;
            }
        }

        WorldCreator worldCreator = new WorldCreator(worldName);
        org.bukkit.WorldType bukkitWorldType;

        switch (worldType) {
            case VOID:
                worldCreator.generateStructures(false);
                bukkitWorldType = org.bukkit.WorldType.FLAT;
                if (XMaterial.supports(17)) {
                    worldCreator.generator(new ModernVoidGenerator());
                } else if (XMaterial.supports(13)) {
                    worldCreator.generator(new DeprecatedVoidGenerator());
                } else {
                    worldCreator.generatorSettings("2;0;1");
                }
                break;

            case FLAT:
            case PRIVATE:
                worldCreator.generateStructures(false);
                bukkitWorldType = org.bukkit.WorldType.FLAT;
                break;

            case NETHER:
                worldCreator.generateStructures(true);
                bukkitWorldType = org.bukkit.WorldType.NORMAL;
                worldCreator.environment(World.Environment.NETHER);
                break;

            case END:
                worldCreator.generateStructures(true);
                bukkitWorldType = org.bukkit.WorldType.NORMAL;
                worldCreator.environment(World.Environment.THE_END);
                break;

            case CUSTOM:
                if (customGenerator != null) {
                    worldCreator.generator(customGenerator.getChunkGenerator());
                }
                // Drop through

            default:
                worldCreator.generateStructures(true);
                bukkitWorldType = org.bukkit.WorldType.NORMAL;
                worldCreator.environment(World.Environment.NORMAL);
                break;
        }
        worldCreator.type(bukkitWorldType);

        World bukkitWorld = Bukkit.createWorld(worldCreator);
        if (bukkitWorld != null) {
            ConfigValues configValues = plugin.getConfigValues();
            bukkitWorld.setDifficulty(difficulty);
            bukkitWorld.setTime(configValues.getNoonTime());
            bukkitWorld.getWorldBorder().setSize(configValues.getWorldBorderSize());
            bukkitWorld.setKeepSpawnInMemory(configValues.isTeleportAfterCreation());
            configValues.getDefaultGameRules().forEach(bukkitWorld::setGameRuleValue);
        }

        updateDataVersion();
        return bukkitWorld;
    }

    /**
     * Parses the world's data version, as stored in {@code level.dat}.
     *
     * @return The world's data version if found, otherwise -1 if unable to parse
     * @see <a href="https://minecraft.fandom.com/wiki/Data_version">Data version</a>
     */
    public int parseDataVersion() {
        File levelFile = new File(Bukkit.getWorldContainer() + File.separator + worldName, "level.dat");
        if (!levelFile.exists()) {
            return -1;
        }

        try {
            CompoundTag level = new Nbt().fromFile(levelFile);
            CompoundTag data = level.get("Data");
            IntTag dataVersion = data.getInt("DataVersion");

            return dataVersion != null ? dataVersion.getValue() : -1;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * The {@code level.dat} file is not updated when a newer Minecraft version loads chunks, making the world not loadable.
     * Therefore, manually sets the world's {@code DataVersion} to the current server version, if lower.
     */
    private void updateDataVersion() {
        File levelFile = new File(Bukkit.getWorldContainer() + File.separator + worldName, "level.dat");
        if (!levelFile.exists()) {
            return;
        }

        try {
            Nbt nbt = new Nbt();
            CompoundTag level = nbt.fromFile(levelFile);
            CompoundTag data = level.get("Data");
            IntTag dataVersion = data.getInt("DataVersion");

            int serverVersion = plugin.getServerVersion().getDataVersion();
            if (dataVersion.getValue() < serverVersion) {
                dataVersion.setValue(serverVersion);
                nbt.toFile(level, levelFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void teleportAfterCreation(Player player) {
        if (!plugin.getConfigValues().isTeleportAfterCreation()) {
            return;
        }

        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            return;
        }

        buildWorld.manageUnload();
        worldManager.teleport(player, buildWorld);
    }
}