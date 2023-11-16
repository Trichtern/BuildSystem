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
package de.eintosti.buildsystem.listener;

import com.cryptomorin.xseries.XMaterial;
import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.world.BuildWorldManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class BlockPhysicsListener implements Listener {

    private final BuildWorldManager worldManager;

    public BlockPhysicsListener(BuildSystemPlugin plugin) {
        this.worldManager = plugin.getWorldManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        World bukkitWorld = block.getWorld();
        BuildWorld buildWorld = worldManager.getBuildWorld(bukkitWorld.getName());

        if (buildWorld == null || buildWorld.getData().physics().get()) {
            return;
        }

        XMaterial xMaterial = XMaterial.matchXMaterial(block.getType());
        BlockFace[] surroundingBlocks = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        switch (xMaterial) {
            case REDSTONE_BLOCK:
                for (BlockFace blockFace : surroundingBlocks) {
                    if (isCustomRedstoneLamp(block.getRelative(blockFace))) {
                        event.setCancelled(false);
                        return;
                    }
                }
                break;
            case REDSTONE_LAMP:
                for (BlockFace blockFace : surroundingBlocks) {
                    if (block.getRelative(blockFace).getType() == XMaterial.REDSTONE_BLOCK.parseMaterial()) {
                        event.setCancelled(false);
                        return;
                    }
                }
                break;
        }

        event.setCancelled(true);
    }

    private boolean isCustomRedstoneLamp(Block block) {
        List<MetadataValue> metadataValues = block.getMetadata("CustomRedstoneLamp");
        for (MetadataValue value : metadataValues) {
            if (value.asBoolean()) {
                return true;
            }
        }
        return block.getType().name().equals("REDSTONE_LAMP_ON");
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();
        BuildWorld buildWorld = worldManager.getBuildWorld(block.getWorld().getName());

        if (buildWorld != null && !buildWorld.getData().physics().get()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        BuildWorld buildWorld = worldManager.getBuildWorld(block.getWorld().getName());

        if (buildWorld != null && !buildWorld.getData().physics().get()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        Block block = event.getBlock();
        BuildWorld buildWorld = worldManager.getBuildWorld(block.getWorld().getName());

        if (buildWorld != null && !buildWorld.getData().physics().get()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        BuildWorld buildWorld = worldManager.getBuildWorld(block.getWorld().getName());

        if (buildWorld != null && !buildWorld.getData().physics().get()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        BuildWorld buildWorld = worldManager.getBuildWorld(block.getWorld().getName());

        if (buildWorld != null && !buildWorld.getData().physics().get()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        BuildWorld buildWorld = worldManager.getBuildWorld(block.getWorld().getName());

        if (buildWorld != null && !buildWorld.getData().physics().get()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        BuildWorld buildWorld = worldManager.getBuildWorld(block.getWorld().getName());

        if (buildWorld == null || buildWorld.getData().physics().get()) {
            return;
        }

        if (event.getEntityType().equals(EntityType.FALLING_BLOCK)) {
            event.setCancelled(true);
            event.getBlock().getState().update(false, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        XMaterial xMaterial = XMaterial.matchXMaterial(block.getType());

        if (isCustomRedstoneLamp(block)) {
            event.setNewCurrent(15);
        }

        if (xMaterial != XMaterial.REDSTONE_BLOCK) {
            return;
        }

        for (BlockFace blockFace : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            if (isCustomRedstoneLamp(block.getRelative(blockFace))) {
                event.setNewCurrent(15);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        World bukkitWorld = block.getWorld();
        BuildWorld buildWorld = worldManager.getBuildWorld(bukkitWorld.getName());

        if (buildWorld != null && !buildWorld.getData().explosions().get()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Location location = event.getLocation();
        World bukkitWorld = location.getWorld();
        BuildWorld buildWorld = worldManager.getBuildWorld(bukkitWorld.getName());

        if (buildWorld != null && !buildWorld.getData().explosions().get()) {
            event.setCancelled(true);
        }
    }
}
