/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.listener;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import de.eintosti.buildsystem.BuildSystem;
import de.eintosti.buildsystem.world.BuildWorld;
import de.eintosti.buildsystem.world.WorldManager;
import de.eintosti.buildsystem.world.data.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class EditSessionListener implements Listener {

    private final WorldManager worldManager;

    public EditSessionListener(BuildSystem plugin) {
        this.worldManager = plugin.getWorldManager();
        WorldEdit.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        Actor actor = event.getActor();
        if (actor == null || !actor.isPlayer()) {
            return;
        }

        Player player = Bukkit.getPlayer(actor.getName());
        if (player == null) {
            return;
        }

        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld().getName());
        if (buildWorld == null) {
            return;
        }

        if (event.getStage() == EditSession.Stage.BEFORE_CHANGE) {
            disableArchivedWorlds(buildWorld, player, event);
            checkBuilders(buildWorld, player, event);
        }
    }

    private void disableArchivedWorlds(BuildWorld buildWorld, Player player, EditSessionEvent event) {
        if (worldManager.canBypassBuildRestriction(player)) {
            return;
        }

        if (buildWorld.getStatus() == WorldStatus.ARCHIVE) {
            event.setExtent(new NullExtent());
        }
    }

    private void checkBuilders(BuildWorld buildWorld, Player player, EditSessionEvent event) {
        if (worldManager.canBypassBuildRestriction(player)) {
            return;
        }

        if (buildWorld.isCreator(player)) {
            return;
        }

        if (buildWorld.isBuilders() && !buildWorld.isBuilder(player)) {
            event.setExtent(new NullExtent());
        }
    }
}