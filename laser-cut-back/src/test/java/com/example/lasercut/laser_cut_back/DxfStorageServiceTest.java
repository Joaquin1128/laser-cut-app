package com.example.lasercut.laser_cut_back;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.lasercut.laser_cut_back.domain.archivo.service.DxfStorageService;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;

public class DxfStorageServiceTest {

    @TempDir
    Path tempDir;

    private DxfStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new DxfStorageService();
        ReflectionTestUtils.setField(storageService, "storageDir", tempDir.toString());
        storageService.init();
    }

    @Test
    void testStoreAndLoadDxf() throws IOException {
        String content = "0\nSECTION\n2\nENTITIES\n0\nENDSEC\n0\nEOF\n";
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "pieza_test.dxf",
                "application/octet-stream",
                content.getBytes()
        );

        String archivoId = storageService.store(file);
        assertNotNull(archivoId);
        assertTrue(archivoId.endsWith("pieza_test.dxf"));

        Resource resource = storageService.loadAsResource(archivoId);
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals("pieza_test.dxf", resource.getFilename().substring(resource.getFilename().indexOf("_") + 1));
    }

    @Test
    void testStoreEmptyFileThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "archivo",
                "vacio.dxf",
                "application/octet-stream",
                new byte[0]
        );

        assertThrows(BadRequestException.class, () -> storageService.store(emptyFile));
    }

    @Test
    void testLoadNonExistentFileThrowsException() {
        assertThrows(BadRequestException.class, () -> storageService.loadAsResource("no_existe.dxf"));
    }

}
