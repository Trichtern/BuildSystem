
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
package de.eintosti.buildsystem.util.color.patterns;

import de.eintosti.buildsystem.util.color.ColorPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexPattern implements ColorPattern {

    private static final Pattern DEFAULT_HEX = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Pattern BRACKET_HEX = Pattern.compile("\\{#[0-9a-fA-F]{6}}");
    private static final Pattern SPIGOT_HEX = Pattern.compile("&x[&0-9a-fA-F]{12}");

    @Override
    public String process(String input) {
        String text = applyFormats(input);
        Matcher matcher = DEFAULT_HEX.matcher(text);
        while (matcher.find()) {
            String hexCode = matcher.group();
            text = text.replace(hexCode, toChatColor(hexCode));
        }
        return text;
    }

    private String applyFormats(String input) {
        String text = input;

        text = parseDefaultFormat(text);
        text = parseBracketFormat(text);
        text = parseSpigotFormat(text);

        return text;
    }

    //&#RRGGBB
    private String parseDefaultFormat(String input) {
        return input.replace("&#", "#");
    }

    //{#RRGGBB}
    private String parseBracketFormat(String input) {
        String text = input;
        Matcher matcher = BRACKET_HEX.matcher(text);

        while (matcher.find()) {
            String hexCode = matcher.group();
            String fixed = hexCode.substring(2, 8);
            text = text.replace(hexCode, "#" + fixed);
        }

        return text;
    }

    //&x&R&R&G&G&B&B
    private String parseSpigotFormat(String input) {
        String text = input.replace('§', '&');

        Matcher matcher = SPIGOT_HEX.matcher(text);
        while (matcher.find()) {
            String hexCode = matcher.group();
            String fixed = hexCode.substring(3).replace("&", "");
            text = text.replace(hexCode, "#" + fixed);
        }

        return text;
    }

    private String toChatColor(String hexCode) {
        StringBuilder magic = new StringBuilder("§x");
        char[] colorChars = hexCode.substring(1).toCharArray();

        for (char c : colorChars) {
            magic.append('§').append(c);
        }

        return magic.toString();
    }
}
