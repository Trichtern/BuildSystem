/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.tabcomplete;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author einTosti
 */
abstract class ArgumentSorter {

    public void addArgument(String input, String argument, List<String> arrayList) {
        if (input.equals("") || StringUtils.startsWithIgnoreCase(argument, input)) {
            arrayList.add(argument);
        }
    }
}