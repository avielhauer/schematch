package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.utils.MetadataUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MetanomeTest {
    Path basePath = Paths.get(System.getProperty("user.dir")).resolve("data").resolve("Pubs").resolve("pubs1_pubs2").resolve("source");
    String fileName = "authors";
    Path testFilePath = basePath.resolve(fileName + ".csv");
    @Test
    void getMetadataPath_includeFileName() {
        Path result = MetadataUtils.getMetadataPath(testFilePath, true);

        assertNotNull(result);
        assertEquals(basePath.getParent().resolve("metadata").resolve("source").resolve(fileName), result);
        assertTrue(Files.isDirectory(result.getParent()));
    }

    @Test
    void getMetadataPath_excludeFileName() {
        Path result = MetadataUtils.getMetadataPath(testFilePath, false);

        assertNotNull(result);
        assertEquals(basePath.getParent().resolve("metadata").resolve("source"), result);
        assertTrue(Files.isDirectory(result));
    }

    @Test
    void getMetadataPath_nullWhenNoParentDirectory() {
        Path result = MetadataUtils.getMetadataPath(testFilePath, true);

        assertNull(result);
    }

    @Test
    void getMetadataPath_nullWhenMetadataFolderDoesNotExist() {
        Path result = MetadataUtils.getMetadataPath(testFilePath, true);

        assertNull(result);
    }
}
