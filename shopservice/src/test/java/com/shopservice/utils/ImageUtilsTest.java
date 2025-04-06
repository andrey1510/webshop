package com.shopservice.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ImageUtilsTest {

    private Path oldFilePath;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        Path tempDir = Paths.get("build/tmp/temp-test-files");
        Files.createDirectories(tempDir);
        oldFilePath = Files.createTempFile(tempDir, "test", ".jpg");
    }

    @AfterEach
    @SneakyThrows
    public void tearDown() {
        if (oldFilePath != null && Files.exists(oldFilePath)) Files.deleteIfExists(oldFilePath);
    }

    @Test
    public void testIsValidImageExtension() {
        assertTrue(ImageUtils.isValidImageExtension("image.jpg"));
        assertTrue(ImageUtils.isValidImageExtension("image.jpeg"));
        assertTrue(ImageUtils.isValidImageExtension("image.png"));
        assertFalse(ImageUtils.isValidImageExtension("image.pdf"));
    }

    @Test
    public void testIsValidImageSize() {
        long validSize = 2 * 1024 * 1024; // 2 MB
        long oversized = 8 * 1024 * 1024; // 8 MB
        assertTrue(ImageUtils.isValidImageSize(validSize));
        assertFalse(ImageUtils.isValidImageSize(oversized));
    }

    @Test
    public void testGenerateUniqueImageName() {
        String originalFileName = "imageName.jpg";
        String uniqueName = ImageUtils.generateUniqueImageName(originalFileName);
        assertNotNull(uniqueName);
        assertNotEquals(originalFileName, uniqueName);
    }

    @Test
    @SneakyThrows
    public void testDeleteImageIfExists() {
        assertTrue(Files.exists(oldFilePath));
        ImageUtils.deleteImageIfExists(oldFilePath.toString());
        assertFalse(Files.exists(oldFilePath));
    }
}
