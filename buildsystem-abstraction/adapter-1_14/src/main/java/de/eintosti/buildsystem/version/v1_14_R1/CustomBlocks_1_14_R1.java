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
package de.eintosti.buildsystem.version.v1_14_R1;

import de.eintosti.buildsystem.version.customblocks.CustomBlock;
import de.eintosti.buildsystem.version.customblocks.CustomBlocks;
import de.eintosti.buildsystem.version.util.DirectionUtil;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

public class CustomBlocks_1_14_R1 implements CustomBlocks {

    private final JavaPlugin plugin;

    public CustomBlocks_1_14_R1(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setBlock(BlockPlaceEvent event, String key) {
        CustomBlock customBlock = CustomBlock.getCustomBlock(key);
        if (customBlock == null) {
            plugin.getLogger().warning("Could not find custom block with key: " + key);
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        Bukkit.getScheduler().runTask(plugin, () -> {
            switch (customBlock) {
                case FULL_OAK_BARCH:
                    block.setType(Material.OAK_WOOD);
                    break;
                case FULL_SPRUCE_BARCH:
                    block.setType(Material.SPRUCE_WOOD);
                    break;
                case FULL_BIRCH_BARCH:
                    block.setType(Material.BIRCH_WOOD);
                    break;
                case FULL_JUNGLE_BARCH:
                    block.setType(Material.JUNGLE_WOOD);
                    break;
                case FULL_ACACIA_BARCH:
                    block.setType(Material.ACACIA_WOOD);
                    break;
                case FULL_DARK_OAK_BARCH:
                    block.setType(Material.DARK_OAK_WOOD);
                    break;
                case RED_MUSHROOM:
                    block.setType(Material.RED_MUSHROOM_BLOCK);
                    break;
                case BROWN_MUSHROOM:
                    block.setType(Material.BROWN_MUSHROOM_BLOCK);
                    break;
                case FULL_MUSHROOM_STEM:
                    block.setType(Material.MUSHROOM_STEM);
                    break;
                case MUSHROOM_STEM:
                    block.setType(Material.MUSHROOM_STEM);
                    MultipleFacing mushroomStem = (MultipleFacing) block.getBlockData();
                    mushroomStem.setFace(BlockFace.UP, false);
                    mushroomStem.setFace(BlockFace.DOWN, false);
                    block.setBlockData(mushroomStem);
                    break;
                case MUSHROOM_BLOCK:
                    block.setType(Material.MUSHROOM_STEM);
                    MultipleFacing mushroomBlock = (MultipleFacing) block.getBlockData();
                    for (BlockFace blockFace : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                        mushroomBlock.setFace(blockFace, false);
                    }
                    block.setBlockData(mushroomBlock);
                    break;
                case SMOOTH_STONE:
                    block.setType(Material.SMOOTH_STONE);
                    break;
                case DOUBLE_STONE_SLAB:
                    block.setType(Material.SMOOTH_STONE_SLAB);
                    Slab slab = (Slab) block.getBlockData();
                    slab.setType(Slab.Type.DOUBLE);
                    block.setBlockData(slab);
                    break;
                case SMOOTH_SANDSTONE:
                    block.setType(Material.SMOOTH_SANDSTONE);
                    break;
                case SMOOTH_RED_SANDSTONE:
                    block.setType(Material.SMOOTH_RED_SANDSTONE);
                    break;
                case POWERED_REDSTONE_LAMP:
                    block.setType(Material.REDSTONE_LAMP);
                    Lightable lightable = (Lightable) block.getBlockData();
                    lightable.setLit(true);
                    block.setBlockData(lightable);
                    break;
                case BURNING_FURNACE:
                    block.setType(Material.FURNACE);
                    Furnace furnace = (Furnace) block.getState();
                    furnace.setBurnTime(Short.MAX_VALUE);
                    furnace.update();
                    rotateBlock(block, player, DirectionUtil.getBlockDirection(player, false));
                    break;
                case PISTON_HEAD:
                    block.setType(Material.PISTON_HEAD);
                    rotateBlock(block, player, DirectionUtil.getBlockDirection(player, true));
                    break;
                case COMMAND_BLOCK:
                    block.setType(Material.COMMAND_BLOCK);
                    rotateBlock(block, player, DirectionUtil.getBlockDirection(player, true));
                    break;
                case BARRIER:
                    block.setType(Material.BARRIER);
                    break;
                case INVISIBLE_ITEM_FRAME:
                    // Invalid server version
                    break;
                case MOB_SPAWNER:
                    block.setType(Material.SPAWNER);
                    break;
                case NETHER_PORTAL:
                    block.setType(Material.NETHER_PORTAL);
                    rotateBlock(block, player, DirectionUtil.getBlockDirection(player, false));
                    break;
                case END_PORTAL:
                    block.setType(Material.END_PORTAL);
                    break;
                case DRAGON_EGG:
                    block.setType(Material.DRAGON_EGG);
                    break;
                default:
                    return;
            }

            event.setCancelled(true);
        });
    }

    @Override
    public void setPlant(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Block adjacent = block.getRelative(event.getBlockFace());
        adjacent.setType(event.getItem().getType());
    }

    @Override
    public void modifySlab(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || !(block.getBlockData() instanceof Slab)) {
            return;
        }

        Slab slab = (Slab) block.getBlockData();
        if (slab.getType() != Slab.Type.DOUBLE) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (isTopHalf(player)) {
            slab.setType(Slab.Type.BOTTOM);
        } else {
            slab.setType(Slab.Type.TOP);
        }

        block.setBlockData(slab);
    }

    private boolean isTopHalf(Player player) {
        RayTraceResult result = player.rayTraceBlocks(6);
        if (result == null) {
            return false;
        }
        return Math.abs(result.getHitPosition().getY() % 1) < 0.5;
    }

    @Override
    public void toggleIronTrapdoor(PlayerInteractEvent event) {
        event.setCancelled(true);
        open(event.getClickedBlock());
    }

    @Override
    public void toggleIronDoor(PlayerInteractEvent event) {
        event.setCancelled(true);
        open(event.getClickedBlock());
    }

    private void open(Block block) {
        if (block == null) {
            return;
        }

        Openable openable = (Openable) block.getBlockData();
        openable.setOpen(!openable.isOpen());
        block.setBlockData(openable);
    }

    @Override
    public void rotateBlock(Block block, Player player, BlockFace direction) {
        BlockData blockData = block.getBlockData();

        if (blockData instanceof Directional) {
            Directional directional = (Directional) blockData;
            directional.setFacing(direction);
            block.setBlockData(directional);
        } else if (blockData instanceof Orientable) {
            Orientable orientable = (Orientable) blockData;
            Axis axis;
            switch (direction) {
                case UP:
                case DOWN:
                    axis = Axis.Y;
                    break;
                case EAST:
                case WEST:
                    axis = Axis.X;
                    break;
                default:
                    axis = Axis.Z;
                    break;
            }
            orientable.setAxis(axis);
            block.setBlockData(orientable);
        } else if (blockData instanceof Sign) {
            Sign sign = (Sign) blockData;
            sign.setRotation(direction);
            block.setBlockData(sign);
        }
    }
}