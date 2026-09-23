package com.example.lasercut.laser_cut_back.domain.archivo.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.lasercut.laser_cut_back.exception.BadRequestException;

import jakarta.annotation.PostConstruct;

@Service
public class DxfStorageService {

    private static final Logger logger = LoggerFactory.getLogger(DxfStorageService.class);

    @Value("${app.storage.dxf-dir:uploads/dxf}")
    private String storageDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
            logger.info("Directorio de almacenamiento DXF inicializado en: {}", this.rootLocation);
        } catch (IOException e) {
            logger.error("No se pudo crear el directorio de almacenamiento DXF: {}", e.getMessage());
        }
    }

    /**
     * Guarda el archivo MultipartFile en disco y retorna un identificador único (archivoId).
     */
    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No se puede almacenar un archivo vacío.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "archivo.dxf";
        }

        // Sanitizar nombre de archivo
        String cleanFilename = Paths.get(originalFilename).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        String archivoId = UUID.randomUUID().toString() + "_" + cleanFilename;
        Path destinationFile = this.rootLocation.resolve(archivoId).normalize();

        // Evitar ataques de Path Traversal
        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new BadRequestException("Ruta de archivo no válida.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Archivo DXF guardado con éxito: {}", archivoId);
            return archivoId;
        }
    }

    /**
     * Carga el archivo como Resource para ser descargado vía HTTP.
     */
    public Resource loadAsResource(String archivoId) {
        try {
            Path file = this.rootLocation.resolve(archivoId).normalize();
            if (!file.getParent().equals(this.rootLocation)) {
                throw new BadRequestException("Acceso a ruta de archivo inválida.");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("No se encontró el archivo DXF o no es legible: " + archivoId);
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Error al resolver URL del archivo: " + e.getMessage());
        }
    }

    /**
     * Obtiene el archivo como File para adjuntos de correo.
     */
    public File loadFile(String archivoId) {
        Path file = this.rootLocation.resolve(archivoId).normalize();
        if (!file.getParent().equals(this.rootLocation)) {
            return null;
        }
        File f = file.toFile();
        return f.exists() ? f : null;
    }

    public Path getRootLocation() {
        return rootLocation;
    }

}
