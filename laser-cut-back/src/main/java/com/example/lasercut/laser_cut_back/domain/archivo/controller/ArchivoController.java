package com.example.lasercut.laser_cut_back.domain.archivo.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.lasercut.laser_cut_back.domain.archivo.dto.ArchivoResponse;
import com.example.lasercut.laser_cut_back.domain.archivo.service.ArchivoService;

@RestController
@RequestMapping("/api")
public class ArchivoController {

    private final ArchivoService archivoService;

    public ArchivoController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping(path = "/analizar-archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArchivoResponse> analizarArchivo(@RequestParam MultipartFile archivo) throws IOException {
    ArchivoResponse resp = archivoService.analizar(archivo);
    return ResponseEntity.ok(resp);
    }
    
}
