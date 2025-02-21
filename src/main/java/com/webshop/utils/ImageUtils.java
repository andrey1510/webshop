package com.webshop.utils;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ImageUtils {

    private static final String[] ALLOWED_EXTENSIONS = {"jpeg", "jpg", "png"};
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024; // 3 MB

    public static boolean isValidImageExtension(String fileName) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(FilenameUtils.getExtension(fileName))) return true;
        }
        return false;
    }

    public static boolean isValidImageSize(long size) {
        return size <= MAX_FILE_SIZE;
    }

    public static String generateUniqueImageName(String originalFileName) {
        return UUID.randomUUID() + "." + FilenameUtils.getExtension(originalFileName);
    }

    public static void deleteImageIfExists(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            Path path = Paths.get(filePath);
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                System.err.println("Ошибка при удалении файла изображения: " + filePath);
            }
        }
    }
}
