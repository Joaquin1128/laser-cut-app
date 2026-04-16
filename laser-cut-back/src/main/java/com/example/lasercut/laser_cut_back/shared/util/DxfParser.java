package com.example.lasercut.laser_cut_back.shared.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.lasercut.laser_cut_back.exception.BadRequestException;

public class DxfParser {

    private static final Logger logger = LoggerFactory.getLogger(DxfParser.class);
    private static final String[] TIPOS_ENTIDAD = {"LINE", "ARC", "CIRCLE", "POLYLINE", "LWPOLYLINE"};

    private static void validarArcosCerrados(DXFDocument doc) {
        final double TOLERANCIA_ANGULO = 0.01;
        final double ANGULO_COMPLETO = 360.0;

        @SuppressWarnings("unchecked")
        Iterator<DXFLayer> layerIterator = doc.getDXFLayerIterator();
        int totalArcosAbiertos = 0;

        while (layerIterator.hasNext()) {
            DXFLayer layer = layerIterator.next();

            @SuppressWarnings("unchecked")
            List<DXFEntity> entities = layer.getDXFEntities("ARC");

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
            logger.error("Archivo DXF rechazado: {} arco(s) no cerrado(s) detectado(s)", totalArcosAbiertos);
            throw new BadRequestException("El archivo contiene arcos abiertos. Todos los arcos deben estar cerrados (360°).");
        }

        logger.info("Validación de arcos cerrados completada: todos los arcos están cerrados correctamente.");
    }

    /**
     * Suma la longitud de todas las entidades de corte del documento.
     * Cada clase geométrica de Kabeja expone getLength() con el valor correcto:
     *   DXFLine     → distancia euclidiana entre extremos
     *   DXFArc      → longitud de arco (radio × ángulo en radianes)
     *   DXFCircle   → perímetro (2π × radio)
     *   DXFPolyline / DXFLWPolyline → suma de segmentos entre vértices
     */
    private static double calcularLongitudCorte(DXFDocument doc) {
        double total = 0.0;

        @SuppressWarnings("unchecked")
        Iterator<DXFLayer> layerIterator = doc.getDXFLayerIterator();

        while (layerIterator.hasNext()) {
            DXFLayer layer = layerIterator.next();

            for (String tipo : TIPOS_ENTIDAD) {
                @SuppressWarnings("unchecked")
                List<DXFEntity> entities = layer.getDXFEntities(tipo);
                if (entities == null) continue;

                for (DXFEntity e : entities) {
                    if (e instanceof DXFLine)            total += ((DXFLine) e).getLength();
                    else if (e instanceof DXFArc)        total += ((DXFArc) e).getLength();
                    else if (e instanceof DXFCircle)     total += ((DXFCircle) e).getLength();
                    else if (e instanceof DXFLWPolyline) total += ((DXFLWPolyline) e).getLength();
                    else if (e instanceof DXFPolyline)   total += ((DXFPolyline) e).getLength();
                }
            }
        }

        return total;
    }

    /**
     * Parsea el DXF una sola vez y retorna dimensiones + longitud total de corte.
     */
    public static DxfAnalysis analyze(InputStream is) throws IOException {
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

            double cutLength = calcularLongitudCorte(doc);

            logger.info("DXF analizado -> ancho={} mm, alto={} mm, longitud_corte={} mm", width, height, cutLength);
            return new DxfAnalysis(width, height, cutLength);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al parsear DXF con Kabeja", e);
            throw new BadRequestException("Error al procesar el archivo DXF: " + e.getMessage());
        }
    }

    /**
     * Retorna solo las dimensiones del bounding box. Usado por ArchivoService.
     */
    public static double[] getWidthHeightMillimeters(InputStream is) throws IOException {
        DxfAnalysis result = analyze(is);
        return new double[]{result.width, result.height};
    }

}
