/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.util;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Set;

public class FileUtils {

    /**
     * Copies a file to a new location preserving the file date.
     *
     * @param source An existing file to copy, must not be {@code null}
     * @param target The new file, must not be {@code null}
     */
    public static void copy(@NotNull File source, @NotNull File target) {
        try {
            Set<String> ignore = Sets.newHashSet("uid.dat", "session.lock");
            if (ignore.contains(source.getName())) {
                return;
            }

            if (source.isDirectory()) {
                if (!target.exists() && !target.mkdirs()) {
                    throw new IOException("Couldn't create directory: " + target.getName());
                }

                for (String fileName : source.list()) {
                    if (ignore.contains(fileName)) {
                        continue;
                    }

                    File sourceFile = new File(source, fileName);
                    File targetFile = new File(target, fileName);
                    copy(sourceFile, targetFile);
                }
            } else {
                InputStream inputStream = Files.newInputStream(source.toPath());
                OutputStream outputStream = Files.newOutputStream(target.toPath());
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory Directory to delete
     */
    public static void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }

    /**
     * Gets the creation date of a file.
     *
     * @param file The file to be checked
     * @return The amount of milliseconds that have passed since {@code January 1, 1970 UTC}, until the file was created
     */
    public static long getDirectoryCreation(File file) {
        long creation = System.currentTimeMillis();
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileTime time = attrs.creationTime();
            creation = time.toMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return creation;
    }
}