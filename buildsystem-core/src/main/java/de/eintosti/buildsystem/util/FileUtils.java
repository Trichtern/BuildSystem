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

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

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
        try (Stream<Path> walk = Files.walk(directory.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
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