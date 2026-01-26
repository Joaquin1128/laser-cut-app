package com.example.lasercut.laser_cut_back.shared.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.lasercut.laser_cut_back.exception.BadRequestException;

public class DxfParser {

    private static final Logger logger = LoggerFactory.getLogger(DxfParser.class);

    private static void validarArcosCerrados(DXFDocument doc) {
        final double TOLERANCIA_ANGULO = 0.01;
        final double ANGULO_COMPLETO = 360.0;
        final String ENTITY_TYPE_ARC = "ARC";

        @SuppressWarnings("unchecked")
        Iterator<DXFLayer> layerIterator = doc.getDXFLayerIterator();
        int totalArcosAbiertos = 0;

        while (layerIterator.hasNext()) {
            DXFLayer layer = layerIterator.next();
            
            @SuppressWarnings("unchecked")
            List<DXFEntity> entities = layer.getDXFEntities(ENTITY_TYPE_ARC);
            
            if (entities != null) {
                for (DXFEntity entity : entities) {
                    if (entity instanceof DXFArc) {
                        DXFArc arc = (DXFArc) entity;
                        double totalAngle = arc.getTotalAngle();
                        
                        if (Math.abs(totalAngle - ANGULO_COMPLETO) > TOLERANCIA_ANGULO) {
                            totalArcosAbiertos++;
                            logger.warn("Arco abierto detectado en capa '{}': ángulo total={}°", 
                                layer.getName(), totalAngle);
                        }
                    }
                }
            }
        }

        if (totalArcosAbiertos > 0) {
            String mensaje = "El archivo contiene arcos abiertos. Todos los arcos deben estar cerrados (360°).";
            logger.error("Archivo DXF rechazado: {} arco(s) no cerrado(s) detectado(s)", totalArcosAbiertos);
            throw new BadRequestException(mensaje);
        }

        logger.info("Validación de arcos cerrados completada: todos los arcos están cerrados correctamente.");
    }

    public static double[] getWidthHeightMillimeters(InputStream is) throws IOException {
        try {
            Parser parser = ParserBuilder.createDefaultParser();
            parser.parse(is, "UTF-8");

            DXFDocument doc = parser.getDocument();
            if (doc == null) {
                throw new BadRequestException("Kabeja no pudo leer el archivo DXF (documento nulo). Verifique que sea DXF R12 ASCII o R2000.");
            }

            validarArcosCerrados(doc);

            Bounds bounds = doc.getBounds();
            if (bounds == null) {
                throw new BadRequestException("No se pudo calcular el bounding box del DXF.");
            }

            double width = Math.abs(bounds.getMaximumX() - bounds.getMinimumX());
            double height = Math.abs(bounds.getMaximumY() - bounds.getMinimumY());

            if (width == 0 || height == 0) {
                throw new BadRequestException("Bounding box inválida (dimensión cero detectada).");
            }

            logger.info("Bounding box DXF -> ancho={} mm, alto={} mm", width, height);
            return new double[]{width, height};

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al parsear DXF con Kabeja", e);
            throw new BadRequestException("Error al procesar el archivo DXF: " + e.getMessage());
        }
    }

}
