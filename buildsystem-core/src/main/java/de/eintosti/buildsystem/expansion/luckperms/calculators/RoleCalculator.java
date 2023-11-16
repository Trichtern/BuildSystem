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
package de.eintosti.buildsystem.expansion.luckperms.calculators;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.world.BuildWorldManager;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class RoleCalculator implements ContextCalculator<Player> {

    private static final String KEY = "buildsystem:role";

    private final BuildWorldManager worldManager;

    public RoleCalculator(BuildSystemPlugin plugin) {
        this.worldManager = plugin.getWorldManager();
    }

    @Override
    public void calculate(@NonNull Player player, @NonNull ContextConsumer contextConsumer) {
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getWorld());
        contextConsumer.accept(KEY, Role.matchRole(player, buildWorld).toString());
    }

    @NotNull
    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        for (Role role : Role.values()) {
            builder.add(KEY, role.toString());
        }
        return builder.build();
    }

    private enum Role {
        /**
         * The creator of a {@link BuildWorld}.
         */
        CREATOR,

        /**
         * A player which has been added to the list of trusted players and is therefore allowed to build in a {@link BuildWorld}.
         */
        BUILDER,

        /**
         * A player which is neither the {@link #CREATOR} nor a {@link #BUILDER} in a {@link BuildWorld}.
         */
        GUEST;

        public static Role matchRole(Player player, BuildWorld buildWorld) {
            if (buildWorld == null) {
                return GUEST;
            }

            if (buildWorld.isCreator(player)) {
                return CREATOR;
            } else if (buildWorld.isBuilder(player.getUniqueId())) {
                return BUILDER;
            } else {
                return GUEST;
            }
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}