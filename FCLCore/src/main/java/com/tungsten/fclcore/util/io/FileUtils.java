/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
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
package com.tungsten.fclcore.util.io;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;

import androidx.annotation.NonNull;

import com.tungsten.fclcore.util.Lang;
import com.tungsten.fclcore.util.StringUtils;
import com.tungsten.fclcore.util.function.ExceptionalConsumer;

public final class FileUtils {

    private FileUtils() {
    }

    public static boolean canCreateDirectory(String path) {
        try {
            return canCreateDirectory(Paths.get(path));
        } catch (InvalidPathException e) {
            return false;
        }
    }

    public static boolean canCreateDirectory(Path path) {
        if (Files.isDirectory(path)) return true;
        else if (Files.exists(path)) return false;
        else {
            Path lastPath = path; // always not exist
            path = path.getParent();
            // find existent ancestor
            while (path != null && !Files.exists(path)) {
                lastPath = path;
                path = path.getParent();
            }
            if (path == null) return false; // all ancestors are nonexistent
            if (!Files.isDirectory(path)) return false; // ancestor is file
            try {
                Files.createDirectory(lastPath); // check permission
                Files.delete(lastPath); // safely delete empty directory
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public static String getNameWithoutExtension(String fileName) {
        return StringUtils.substringBeforeLast(fileName, '.');
    }

    public static String getNameWithoutExtension(File file) {
        return StringUtils.substringBeforeLast(file.getName(), '.');
    }

    public static String getNameWithoutExtension(Path file) {
        return StringUtils.substringBeforeLast(getName(file), '.');
    }

    public static String getExtension(File file) {
        return StringUtils.substringAfterLast(file.getName(), '.');
    }

    public static String getExtension(Path file) {
        return StringUtils.substringAfterLast(getName(file), '.');
    }

    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";

        int lastSlashIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        String fileNameWithoutPath = (lastSlashIndex == -1) ? fileName : fileName.substring(lastSlashIndex + 1);

        String extension = StringUtils.substringAfterLast(fileNameWithoutPath, '.');

        return extension.isEmpty() ? "" : extension;
    }


    /**
     * This method is for normalizing ZipPath since Path.normalize of ZipFileSystem does not work properly.
     */
    public static String normalizePath(String path) {
        return StringUtils.addPrefix(StringUtils.removeSuffix(path, "/", "\\"), "/");
    }

    public static String getName(Path path) {
        if (path.getFileName() == null) return "";
        return StringUtils.removeSuffix(path.getFileName().toString(), "/", "\\");
    }

    public static String getName(Path path, String candidate) {
        if (path.getFileName() == null) return candidate;
        else return getName(path);
    }

    public static String readText(File file) throws IOException {
        return readText(file, UTF_8);
    }

    public static String readText(File file, Charset charset) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), charset);
    }

    public static String readText(Path file) throws IOException {
        return readText(file, UTF_8);
    }

    public static String readText(Path file, Charset charset) throws IOException {
        return new String(Files.readAllBytes(file), charset);
    }

    public static void writeTextWithAppendMode(File file, String text) throws IOException {
        writeBytesWithAppendMode(file.toPath(), text.getBytes(UTF_8));
    }

    public static void writeTextWithAppendMode(Path file, String text) throws IOException {
        writeBytesWithAppendMode(file, text.getBytes(UTF_8));
    }

    public static void writeBytesWithAppendMode(Path file, byte[] data) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Write plain text to file. Characters are encoded into bytes using UTF-8.
     * <p>
     * We don't care about platform difference of line separator. Because readText accept all possibilities of line separator.
     * It will create the file if it does not exist, or truncate the existing file to empty for rewriting.
     * All characters in text will be written into the file in binary format. Existing data will be erased.
     *
     * @param file the path to the file
     * @param text the text being written to file
     * @throws IOException if an I/O error occurs
     */
    public static void writeText(File file, String text) throws IOException {
        writeText(file, text, UTF_8);
    }

    /**
     * Write plain text to file. Characters are encoded into bytes using UTF-8.
     * <p>
     * We don't care about platform difference of line separator. Because readText accept all possibilities of line separator.
     * It will create the file if it does not exist, or truncate the existing file to empty for rewriting.
     * All characters in text will be written into the file in binary format. Existing data will be erased.
     *
     * @param file the path to the file
     * @param text the text being written to file
     * @throws IOException if an I/O error occurs
     */
    public static void writeText(Path file, String text) throws IOException {
        writeText(file, text, UTF_8);
    }

    /**
     * Write plain text to file.
     * <p>
     * We don't care about platform difference of line separator. Because readText accept all possibilities of line separator.
     * It will create the file if it does not exist, or truncate the existing file to empty for rewriting.
     * All characters in text will be written into the file in binary format. Existing data will be erased.
     *
     * @param file    the path to the file
     * @param text    the text being written to file
     * @param charset the charset to use for encoding
     * @throws IOException if an I/O error occurs
     */
    public static void writeText(File file, String text, Charset charset) throws IOException {
        writeBytes(file, text.getBytes(charset));
    }

    /**
     * Write plain text to file.
     * <p>
     * We don't care about platform difference of line separator. Because readText accept all possibilities of line separator.
     * It will create the file if it does not exist, or truncate the existing file to empty for rewriting.
     * All characters in text will be written into the file in binary format. Existing data will be erased.
     *
     * @param file    the path to the file
     * @param text    the text being written to file
     * @param charset the charset to use for encoding
     * @throws IOException if an I/O error occurs
     */
    public static void writeText(Path file, String text, Charset charset) throws IOException {
        writeBytes(file, text.getBytes(charset));
    }

    /**
     * Write byte array to file.
     * It will create the file if it does not exist, or truncate the existing file to empty for rewriting.
     * All bytes in byte array will be written into the file in binary format. Existing data will be erased.
     *
     * @param file  the path to the file
     * @param data the data being written to file
     * @throws IOException if an I/O error occurs
     */
    public static void writeBytes(File file, byte[] data) throws IOException {
        writeBytes(file.toPath(), data);
    }

    /**
     * Write byte array to file.
     * It will create the file if it does not exist, or truncate the existing file to empty for rewriting.
     * All bytes in byte array will be written into the file in binary format. Existing data will be erased.
     *
     * @param file  the path to the file
     * @param data the data being written to file
     * @throws IOException if an I/O error occurs
     */
    public static void writeBytes(Path file, byte[] data) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, data);
    }

    public static void deleteDirectory(File directory)
            throws IOException {
        if (!directory.exists())
            return;

        // 修复：正确判断是否为目录，而不是依赖文件名
        if(!directory.isDirectory()) throw new IllegalArgumentException("Path is not a directory: " + directory);

        if (!isSymlink(directory))
            cleanDirectory(directory);

        if(!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    public static void deleteDirectory(String... directory)
            throws IOException {
        for(String dir : directory) {
            deleteDirectory(new File(dir));
        }
    }


    public static boolean deleteDirectoryQuietly(File directory) {
        try {
            deleteDirectory(directory);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Copy directory.
     * Paths of all files relative to source directory will be the same as the ones relative to destination directory.
     *
     * @param src  the source directory.
     * @param dest the destination directory, which will be created if not existing.
     * @throws IOException if an I/O error occurs.
     */
    public static void copyDirectory(Path src, Path dest) throws IOException {
        copyDirectory(src, dest, path -> true);
    }

    public static void copyDirectory(Path src, Path dest, Predicate<String> filePredicate) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!filePredicate.test(src.relativize(file).toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                Path destFile = dest.resolve(src.relativize(file).toString());
                Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!filePredicate.test(src.relativize(dir).toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                Path destDir = dest.resolve(src.relativize(dir).toString());
                Files.createDirectories(destDir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void cleanDirectory(File directory)
            throws IOException {
        if (!directory.exists()) {
            if (!makeDirectory(directory))
                throw new IOException("Failed to create directory: " + directory);
            return;
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null)
            throw new IOException("Failed to list contents of " + directory);

        IOException exception = null;
        for (File file : files)
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }

        if (null != exception)
            throw exception;
    }

    public static boolean cleanDirectoryQuietly(File directory) {
        try {
            cleanDirectory(directory);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void forceDelete(File file)
            throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent)
                    throw new FileNotFoundException("File does not exist: " + file);
                throw new IOException("Unable to delete file: " + file);
            }
        }
    }

    public static boolean isSymlink(File file)
            throws IOException {
        Objects.requireNonNull(file, "File must not be null");
        if (File.separatorChar == '\\')
            return false;
        File fileInCanonicalDir;
        if (file.getParent() == null)
            fileInCanonicalDir = file;
        else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    public static void copyFile(File srcFile, File destFile)
            throws IOException {
        Objects.requireNonNull(srcFile, "Source must not be null");
        Objects.requireNonNull(destFile, "Destination must not be null");
        if (!srcFile.exists())
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        if (srcFile.isDirectory())
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath()))
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        File parentFile = destFile.getParentFile();
        if (parentFile != null && !FileUtils.makeDirectory(parentFile))
            throw new IOException("Destination '" + parentFile + "' directory cannot be created");
        if (destFile.exists() && !destFile.canWrite())
            throw new IOException("Destination '" + destFile + "' exists but is read-only");

        Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void copyFile(Path srcFile, Path destFile)
            throws IOException {
        Objects.requireNonNull(srcFile, "Source must not be null");
        Objects.requireNonNull(destFile, "Destination must not be null");
        if (!Files.exists(srcFile))
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        if (Files.isDirectory(srcFile))
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        Path parentFile = destFile.getParent();
        Files.createDirectories(parentFile);
        if (Files.exists(destFile) && !Files.isWritable(destFile))
            throw new IOException("Destination '" + destFile + "' exists but is read-only");

        Files.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void moveFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile);
        srcFile.delete();
    }

    public static boolean makeDirectory(File directory) {
        directory.mkdirs();
        return directory.isDirectory();
    }

    public static boolean makeFile(File file) {
        return makeDirectory(file.getAbsoluteFile().getParentFile()) && (file.exists() || Lang.test(file::createNewFile));
    }

    public static List<File> listFilesByExtension(File file, String extension) {
        List<File> result = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null)
            for (File it : files)
                if (extension.equals(getExtension(it)))
                    result.add(it);
        return result;
    }

    /**
     * Tests whether the file is convertible to [java.nio.file.Path] or not.
     *
     * @param file the file to be tested
     * @return true if the file is convertible to Path.
     */
    public static boolean isValidPath(File file) {
        try {
            file.toPath();
            return true;
        } catch (InvalidPathException ignored) {
            return false;
        }
    }

    public static Optional<Path> tryGetPath(String first, String... more) {
        if (first == null) return Optional.empty();
        try {
            return Optional.of(Paths.get(first, more));
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    public static Path tmpSaveFile(Path file) {
        return file.toAbsolutePath().resolveSibling("." + file.getFileName().toString() + ".tmp");
    }

    public static void saveSafely(Path file, String content) throws IOException {
        Path tmpFile = tmpSaveFile(file);
        try (BufferedWriter writer = Files.newBufferedWriter(tmpFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            writer.write(content);
        }

        try {
            if (Files.exists(file) && Files.getAttribute(file, "dos:hidden") == Boolean.TRUE) {
                Files.setAttribute(tmpFile, "dos:hidden", true);
            }
        } catch (Throwable ignored) {
        }

        Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void saveSafely(Path file, ExceptionalConsumer<? super OutputStream, IOException> action) throws IOException {
        Path tmpFile = tmpSaveFile(file);

        try (OutputStream os = Files.newOutputStream(tmpFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            action.accept(os);
        }

        try {
            if (Files.exists(file) && Files.getAttribute(file, "dos:hidden") == Boolean.TRUE) {
                Files.setAttribute(tmpFile, "dos:hidden", true);
            }
        } catch (Throwable ignored) {
        }

        Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 判断给定路径是否是文件/文件夹，并且具有读写权限
     * 如果文件/文件夹不存在，则尝试创建目录来检查权限
     *
     * @param path 文件或文件夹路径
     * @return 如果是文件或文件夹且有读写权限，返回true，否则返回false
    **/
    public static boolean checkPermission(String path) {
        if(path == null || path.isBlank()) return false;
        File file = new File(path);

        if(file.exists()) {
            if(file.isFile() || file.isDirectory()) return file.canRead() && file.canWrite();
            return false;
        }

        // 文件或文件夹不存在时，进行权限检查
        String testPath;
        String extension = getExtension(path);

        if (!extension.isEmpty()) {
            File parentFile = file.getParentFile();
            testPath = (parentFile != null) ? parentFile.getAbsolutePath() : ".";
        }else testPath = path;
        return canCreateDirectory(testPath);
    }

    /**
     * 强制删除文件或目录（静默版本，不抛出异常）
     *
     * @param file 要删除的文件或目录
     * @return true 如果删除成功，false 如果删除失败
    **/
    public static boolean forceDeleteQuietly(File file) {
        try {
            forceDelete(file);
            return true;
        }catch(IOException e) {
            return false;
        }
    }

    public static void forceDelete(String... files) {
        for(String file : files) {
            try {
                forceDelete(new File(file));
            }catch(Exception ignored) {}
        }
    }

    public static void batchDelete(File... files) {
        if(files != null) {
            for(File file : files) {
                if (file.isFile()) file.delete();
                else deleteDirectoryQuietly(file);
            }
        }
    }

    /**
     * 判断 assets 下某个文件夹是否存在
     * @param context 应用的上下文
     * @param folderNames 需要判断的文件夹名称
     * @return 如果文件夹存在，返回 true；否则返回 false
    **/
    public static boolean assetsDirExist(Context context, String... folderNames) {
        if (folderNames == null || folderNames.length == 0) return false;

        try {
            for(String folderName : folderNames) {
                String[] fileList = context.getAssets().list(folderName);
                if(fileList == null || fileList.length == 0) return false;
            }
            // 如果所有文件夹都存在，返回 true
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
