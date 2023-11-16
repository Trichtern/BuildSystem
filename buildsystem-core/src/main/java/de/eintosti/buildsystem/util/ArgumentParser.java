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
package de.eintosti.buildsystem.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArgumentParser {

    private final List<String> args;
    private final Map<String, List<String>> map;
    private final Set<String> flags;

    public ArgumentParser(String[] arguments) {
        this.args = Arrays.asList(arguments);
        this.map = new HashMap<>();
        this.flags = new HashSet<>();
        map();
    }

    /**
     * Gets whether the provided string is used as an argument.<br>
     * Useful for figuring out if an argument has a value.
     *
     * @param name The name of the argument
     * @return {@code true} if the argument is present, otherwise {@code false}
     */
    public boolean isArgument(String name) {
        return args.stream()
                .map(arg -> arg.replace("-", ""))
                .anyMatch(name::equalsIgnoreCase);
    }

    /**
     * Gets whether the flag is present in the array of arguments.
     * A flag is a true or false argument with no value.
     * <p>
     * For example: {@code -nogui}
     *
     * @param name The name of the flag
     * @return {@code true} if the flag is present, otherwise {@code false}
     */
    public boolean getFlag(String name) {
        return flags.contains(name);
    }

    /**
     * Gets the value of an argument.
     * <p>
     * For example: {@code -name XYZ}
     *
     * @param name The name of the argument
     * @return The argument value if present, otherwise {@code null}.
     */
    @Nullable
    public String getValue(String name) {
        List<String> value = map.get(name);
        if (value != null) {
            return String.join(" ", value);
        }
        return null;
    }

    public void map() {
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                continue;
            }

            if (args.indexOf(arg) == (args.size() - 1) || args.get(args.indexOf(arg) + 1).startsWith("-")) {
                flags.add(arg.replace("-", ""));
            } else {
                List<String> argumentValues = new ArrayList<>();
                int i = 1;
                while (args.indexOf(arg) + i != args.size() && !args.get(args.indexOf(arg) + i).startsWith("-")) {
                    argumentValues.add(args.get(args.indexOf(arg) + i));
                    i++;
                }
                map.put(arg.replace("-", ""), argumentValues);
            }
        }
    }
}