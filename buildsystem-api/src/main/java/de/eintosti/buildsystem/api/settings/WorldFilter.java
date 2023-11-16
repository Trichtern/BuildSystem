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
package de.eintosti.buildsystem.api.settings;

import de.eintosti.buildsystem.api.world.BuildWorld;

import java.util.function.Predicate;

public interface WorldFilter {

    enum Mode {
        NONE("world_filter_mode_none"),
        STARTS_WITH("world_filter_mode_starts_with"),
        CONTAINS("world_filter_mode_contains"),
        MATCHES("world_filter_mode_matches");

        private final String loreKey;

        Mode(String loreKey) {
            this.loreKey = loreKey;
        }

        public String getLoreKey() {
            return loreKey;
        }

        public Mode getNext() {
            switch (this) {
                default: // NONE
                    return STARTS_WITH;
                case STARTS_WITH:
                    return CONTAINS;
                case CONTAINS:
                    return MATCHES;
                case MATCHES:
                    return NONE;
            }
        }
    }

    /**
     * Gets the current mode.
     *
     * @return The mode
     */
    Mode getMode();

    /**
     * Sets the current mode.
     *
     * @param mode The mode
     */
    void setMode(Mode mode);

    /**
     * Gets the text which the filter is applied to.
     *
     * @return The text the filter is applied to
     */
    String getText();

    /**
     * Sets the text which the filter is applied to.
     *
     * @param text The text
     */
    void setText(String text);

    /**
     * Apply the filter to a {@link BuildWorld}.
     *
     * @return The filter predicate
     */
    Predicate<BuildWorld> apply();
}