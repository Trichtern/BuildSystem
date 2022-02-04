/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.listener;

import com.cryptomorin.xseries.XMaterial;
import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.manager.WorldManager;
import com.eintosti.buildsystem.object.world.BuildWorld;
import com.eintosti.buildsystem.object.world.Builder;
import com.eintosti.buildsystem.object.world.WorldStatus;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author einTosti
 */
public class WorldManipulateListener implements Listener {

    private final BuildSystem plugin;
    private final WorldManager worldManager;

    public WorldManipulateListener(BuildSystem plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        if (!manageWorldInteraction(player, event, buildWorld.isBlockBreaking())) {
            setStatus(buildWorld, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        if (!manageWorldInteraction(player, event, buildWorld.isBlockPlacement())) {
            setStatus(buildWorld, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();

        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        if (event.getEntity() instanceof ArmorStand) {
            manageWorldInteraction(player, event, buildWorld.isBlockInteractions());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        manageWorldInteraction(player, event, buildWorld.isBlockInteractions());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack != null && itemStack.getType() == plugin.getConfigValues().getWorldEditWand().parseMaterial()) {
            return;
        }

        Player player = event.getPlayer();
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        manageWorldInteraction(player, event, buildWorld.isBlockInteractions());

        if (!buildWorld.isPhysics() && event.getClickedBlock() != null) {
            if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == XMaterial.FARMLAND.parseMaterial()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Not every player can always interact with the {@link BuildWorld} they are in.
     * <p>
     * Reasons an interaction could be cancelled:<br>
     * - The world has its {@link WorldStatus} set to archived<br>
     * - The world has a setting enabled which disallows certain events<br>
     * - The world only allows {@link Builder}s to build and the player is not such a builder<br>
     * <p>
     * However, a player can override these reasons if:<br>
     * - The player has the permission `buildsystem.admin`<br>
     * - The player has the permission `buildsystem.bypass.archive`<br>
     * - The player has used `/build` to enter build-mode<br>
     *
     * @param player the player who manipulated the world
     * @param event  the event which was called by the world manipulation
     * @return if the event called when the player performs an action was cancelled
     */
    private boolean manageWorldInteraction(Player player, Event event, boolean worldSetting) {
        String worldName = player.getWorld().getName();
        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            return false;
        }

        if (disableArchivedWorlds(buildWorld, player, event)) {
            return true;
        }
        if (checkWorldSettings(player, event, worldSetting)) {
            return true;
        }
        if (checkBuilders(buildWorld, player, event)) {
            return true;
        }

        return false;
    }

    private boolean disableArchivedWorlds(BuildWorld buildWorld, Player player, Event event) {
        Cancellable cancellable = (Cancellable) event;

        if (plugin.canBypass(player)) {
            return false;
        }

        if (buildWorld.getStatus() == WorldStatus.ARCHIVE) {
            cancellable.setCancelled(true);
            denyPlayerInteraction(event);
            return true;
        }

        return false;
    }

    private boolean checkWorldSettings(Player player, Event event, boolean worldSetting) {
        Cancellable cancellable = (Cancellable) event;

        if (plugin.canBypass(player)) {
            return false;
        }

        if (!worldSetting) {
            cancellable.setCancelled(true);
            denyPlayerInteraction(event);
            return true;
        }

        return false;
    }

    private boolean checkBuilders(BuildWorld buildWorld, Player player, Event event) {
        Cancellable cancellable = (Cancellable) event;

        if (plugin.canBypass(player)) {
            return false;
        }

        if (buildWorld.getCreatorId() != null && buildWorld.getCreatorId().equals(player.getUniqueId())) {
            return false;
        }

        if (buildWorld.isBuilders() && !buildWorld.isBuilder(player)) {
            cancellable.setCancelled(true);
            denyPlayerInteraction(event);
            return true;
        }

        return false;
    }

    private void denyPlayerInteraction(Event event) {
        if (event instanceof PlayerInteractEvent) {
            ((PlayerInteractEvent) event).setUseItemInHand(Event.Result.DENY);
            ((PlayerInteractEvent) event).setUseInteractedBlock(Event.Result.DENY);
        }
    }

    private void setStatus(BuildWorld buildWorld, Player player) {
        if (buildWorld.getStatus() == WorldStatus.NOT_STARTED) {
            buildWorld.setStatus(WorldStatus.IN_PROGRESS);
            plugin.getPlayerManager().forceUpdateSidebar(player);
        }
    }
}
