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
package de.eintosti.buildsystem.world.data;

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.api.world.data.WorldData;
import de.eintosti.buildsystem.api.world.data.WorldStatus;
import de.eintosti.buildsystem.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BuildWorldData implements WorldData, ConfigurationSerializable {

    private final Map<String, DataType<?>> data = new HashMap<>();

    private final DataType<String> customSpawn = register("spawn");
    private final DataType<String> permission = register("permission");
    private final DataType<String> project = register("project");

    private final DataType<Difficulty> difficulty = register("difficulty", new DifficultyType());
    private final DataType<XMaterial> material = register("material", new MaterialType());
    private final DataType<WorldStatus> status = register("status", new StatusType());

    private final DataType<Boolean> blockBreaking = register("block-breaking");
    private final DataType<Boolean> blockInteractions = register("block-interactions");
    private final DataType<Boolean> blockPlacement = register("block-placement");
    private final DataType<Boolean> buildersEnabled = register("builders-enabled");
    private final DataType<Boolean> explosions = register("explosions");
    private final DataType<Boolean> mobAi = register("mob-ai");
    private final DataType<Boolean> physics = register("physics");
    private final DataType<Boolean> privateWorld = register("private");

    private final DataType<Long> lastEdited = register("last-edited");
    private final DataType<Long> lastLoaded = register("last-loaded");
    private final DataType<Long> lastUnloaded = register("last-unloaded");

    private String worldName;

    public <T> DataType<T> register(@NotNull String key) {
        return register(key, new DataType<>());
    }

    public <T> DataType<T> register(@NotNull String key, DataType<T> type) {
        this.data.put(key, type);
        return type;
    }

    public BuildWorldData(String worldName, ConfigValues configValues, boolean privateWorld) {
        this.customSpawn.set(null);
        this.permission.set(configValues.getDefaultPermission(privateWorld).replace("%world%", worldName));
        this.project.set("-");

        this.difficulty.set(configValues.getWorldDifficulty());
        this.status.set(WorldStatus.NOT_STARTED);

        this.blockBreaking.set(configValues.isWorldBlockBreaking());
        this.blockInteractions.set(configValues.isWorldBlockInteractions());
        this.blockPlacement.set(configValues.isWorldBlockPlacement());
        this.buildersEnabled.set(configValues.isWorldBuildersEnabled(privateWorld));
        this.explosions.set(configValues.isWorldExplosions());
        this.mobAi.set(configValues.isWorldMobAi());
        this.physics.set(configValues.isWorldPhysics());
        this.privateWorld.set(privateWorld);

        this.lastEdited.set((long) -1);
        this.lastLoaded.set((long) -1);
        this.lastUnloaded.set((long) -1);

        this.worldName = worldName;
    }

    public BuildWorldData(String worldName, String customSpawn, String permission, String project, Difficulty difficulty, XMaterial material, WorldStatus worldStatus, boolean blockBreaking, boolean blockInteractions, boolean blockPlacement, boolean buildersEnabled, boolean explosions, boolean mobAi, boolean physics, boolean privateWorld, long lastLoaded, long lastUnloaded, long lastEdited) {
        this.customSpawn.set(customSpawn);
        this.permission.set(permission);
        this.project.set(project);

        this.difficulty.set(difficulty);
        this.material.set(material);
        this.status.set(worldStatus);

        this.blockBreaking.set(blockBreaking);
        this.blockInteractions.set(blockInteractions);
        this.blockPlacement.set(blockPlacement);
        this.buildersEnabled.set(buildersEnabled);
        this.explosions.set(explosions);
        this.mobAi.set(mobAi);
        this.physics.set(physics);
        this.privateWorld.set(privateWorld);

        this.lastEdited.set(lastEdited);
        this.lastLoaded.set(lastLoaded);
        this.lastUnloaded.set(lastUnloaded);

        this.worldName = worldName;
    }

    @Override
    public DataType<String> customSpawn() {
        return customSpawn;
    }

    @Override
    @Nullable
    public Location getCustomSpawnLocation() {
        String customSpawn = customSpawn().get();
        if (customSpawn == null) {
            return null;
        }

        String[] spawnString = customSpawn.split(";");
        return new Location(
                Bukkit.getWorld(worldName),
                Double.parseDouble(spawnString[0]),
                Double.parseDouble(spawnString[1]),
                Double.parseDouble(spawnString[2]),
                Float.parseFloat(spawnString[3]),
                Float.parseFloat(spawnString[4])
        );
    }

    @Override
    public DataType<String> permission() {
        return permission;
    }

    @Override
    public DataType<String> project() {
        return project;
    }

    @Override
    public DataType<Difficulty> difficulty() {
        return difficulty;
    }

    @Override
    public DataType<XMaterial> material() {
        return material;
    }

    @Override
    public DataType<WorldStatus> status() {
        return status;
    }

    @Override
    public DataType<Boolean> blockBreaking() {
        return blockBreaking;
    }

    @Override
    public DataType<Boolean> blockInteractions() {
        return blockInteractions;
    }

    @Override
    public DataType<Boolean> blockPlacement() {
        return blockPlacement;
    }

    @Override
    public DataType<Boolean> buildersEnabled() {
        return buildersEnabled;
    }

    @Override
    public DataType<Boolean> explosions() {
        return explosions;
    }

    @Override
    public DataType<Boolean> mobAi() {
        return mobAi;
    }

    @Override
    public DataType<Boolean> physics() {
        return physics;
    }

    @Override
    public DataType<Boolean> privateWorld() {
        return privateWorld;
    }

    @Override
    public DataType<Long> lastEdited() {
        return lastEdited;
    }

    @Override
    public DataType<Long> lastLoaded() {
        return lastLoaded;
    }

    @Override
    public DataType<Long> lastUnloaded() {
        return lastUnloaded;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return data.entrySet().stream()
                .filter(entry -> entry.getValue().get() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getConfigFormat()));
    }

    public static class DataType<T> implements Type<T> {
        private T value;

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            this.value = value;
        }

        protected Object getConfigFormat() {
            return value;
        }
    }

    public static class DifficultyType extends DataType<Difficulty> {
        @Override
        protected Object getConfigFormat() {
            return super.get().name();
        }
    }

    public static class MaterialType extends DataType<XMaterial> {
        @Override
        protected Object getConfigFormat() {
            return super.get().name();
        }
    }

    public static class StatusType extends DataType<WorldStatus> {
        @Override
        protected Object getConfigFormat() {
            return super.get().name();
        }
    }
}