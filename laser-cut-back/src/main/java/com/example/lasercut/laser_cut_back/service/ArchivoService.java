package com.example.lasercut.laser_cut_back.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.lasercut.laser_cut_back.dto.ArchivoResponse;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.util.DxfParser;
import com.example.lasercut.laser_cut_back.util.DxfPreviewGenerator;

@Service
public class ArchivoService {

    private static final Logger logger = LoggerFactory.getLogger(ArchivoService.class);

    public ArchivoResponse analizar(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo DXF no puede estar vacío.");
        }

        String name = archivo.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".dxf")) {
            throw new BadRequestException("El archivo debe ser un .dxf.");
        }

        byte[] fileBytes = archivo.getBytes();

        try (InputStream dimensionsStream = new ByteArrayInputStream(fileBytes)) {
            double[] wh = DxfParser.getWidthHeightMillimeters(dimensionsStream);
            double ancho = wh[0];
            double alto = wh[1];

            try (InputStream previewStream = new ByteArrayInputStream(fileBytes)) {
                String vistaPrevia = DxfPreviewGenerator.generarVistaPreviaBase64(previewStream);

                ArchivoResponse resp = new ArchivoResponse();
                resp.setNombre(name);
                resp.setAncho(ancho);
                resp.setAlto(alto);
                resp.setVistaPreviaBase64(vistaPrevia);

                logger.info("Archivo analizado: {} (ancho={}mm, alto={}mm)", name, ancho, alto);

                return resp;
            }
        }
    }

}
