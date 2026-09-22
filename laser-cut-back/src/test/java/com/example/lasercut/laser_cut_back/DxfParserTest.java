package com.example.lasercut.laser_cut_back;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.lasercut.laser_cut_back.shared.util.DxfAnalysis;
import com.example.lasercut.laser_cut_back.shared.util.DxfParser;

import static org.junit.jupiter.api.Assertions.*;

public class DxfParserTest {

    private static final double EPSILON = 0.01;

    @Test
    @DisplayName("Debe calcular correctamente la longitud de arco en LWPOLYLINE con bulge = 1.0 (semicírculo)")
    public void testLwPolylineBulgeLength() throws Exception {
        // Semicírculo de diámetro 100 mm entre (0,0) y (100,0) con bulge = 1.0 (arco de 180°)
        // Longitud esperada: radio * PI = 50 * PI ≈ 157.0796 mm
        String dxf = 
            "0\nSECTION\n2\nENTITIES\n" +
            "0\nLWPOLYLINE\n" +
            "8\n0\n" +
            "90\n2\n" +
            "70\n0\n" +
            "10\n0.0\n20\n0.0\n" +
            "42\n1.0\n" +
            "10\n100.0\n20\n0.0\n" +
            "0\nENDSEC\n0\nEOF\n";

        DxfAnalysis analysis = DxfParser.analyze(new ByteArrayInputStream(dxf.getBytes(StandardCharsets.UTF_8)));

        assertEquals(100.0, analysis.width, EPSILON);
        double expectedArcLength = 50.0 * Math.PI;
        assertEquals(expectedArcLength, analysis.cutLengthMm, EPSILON);
    }

    @Test
    @DisplayName("Debe calcular correctamente una LWPOLYLINE cerrada con 2 arcos que forman un círculo completo")
    public void testClosedLwPolylineTwoBulges() throws Exception {
        // Círculo formado por 2 semicírculos cerrados con diámetro 100 mm
        // Perímetro esperado: PI * 100 ≈ 314.159 mm
        String dxf = 
            "0\nSECTION\n2\nENTITIES\n" +
            "0\nLWPOLYLINE\n" +
            "8\n0\n" +
            "90\n2\n" +
            "70\n1\n" + // Flag 1 = cerrado
            "10\n0.0\n20\n0.0\n" +
            "42\n1.0\n" +
            "10\n100.0\n20\n0.0\n" +
            "42\n1.0\n" +
            "0\nENDSEC\n0\nEOF\n";

        DxfAnalysis analysis = DxfParser.analyze(new ByteArrayInputStream(dxf.getBytes(StandardCharsets.UTF_8)));

        double expectedPerimeter = 100.0 * Math.PI;
        assertEquals(expectedPerimeter, analysis.cutLengthMm, EPSILON);
    }

    @Test
    @DisplayName("Debe calcular la longitud de corte en POLYLINE clásica R12 con VERTEX y bulge")
    public void testClassicPolylineBulge() throws Exception {
        String dxf = 
            "0\nSECTION\n2\nENTITIES\n" +
            "0\nPOLYLINE\n" +
            "8\n0\n" +
            "66\n1\n" +
            "70\n0\n" +
            "0\nVERTEX\n" +
            "8\n0\n" +
            "10\n0.0\n20\n0.0\n" +
            "42\n1.0\n" +
            "0\nVERTEX\n" +
            "8\n0\n" +
            "10\n100.0\n20\n0.0\n" +
            "0\nSEQEND\n" +
            "0\nENDSEC\n0\nEOF\n";

        DxfAnalysis analysis = DxfParser.analyze(new ByteArrayInputStream(dxf.getBytes(StandardCharsets.UTF_8)));

        double expectedArcLength = 50.0 * Math.PI;
        assertEquals(expectedArcLength, analysis.cutLengthMm, EPSILON);
    }

    @Test
    @DisplayName("Debe incluir entidades SPLINE en el cálculo de longitud de corte")
    public void testSplineLength() throws Exception {
        // Spline 2D con puntos en X e Y (width > 0, height > 0)
        String dxf = 
            "0\nSECTION\n2\nENTITIES\n" +
            "0\nSPLINE\n" +
            "8\n0\n" +
            "70\n8\n" +
            "71\n3\n" +
            "72\n8\n" +
            "73\n4\n" +
            "40\n0.0\n40\n0.0\n40\n0.0\n40\n0.0\n40\n1.0\n40\n1.0\n40\n1.0\n40\n1.0\n" +
            "10\n0.0\n20\n0.0\n30\n0.0\n" +
            "10\n10.0\n20\n20.0\n30\n0.0\n" +
            "10\n20.0\n20\n0.0\n30\n0.0\n" +
            "10\n30.0\n20\n20.0\n30\n0.0\n" +
            "0\nENDSEC\n0\nEOF\n";

        DxfAnalysis analysis = DxfParser.analyze(new ByteArrayInputStream(dxf.getBytes(StandardCharsets.UTF_8)));

        assertTrue(analysis.width > 0);
        assertTrue(analysis.height > 0);
        assertTrue(analysis.cutLengthMm > 0);
    }

    @Test
    @DisplayName("Debe incluir bloques INSERT en la longitud de corte y aplicar el factor de escala")
    public void testInsertBlockWithScale() throws Exception {
        // Bloque en L con una línea horizontal de 50 mm y una vertical de 30 mm (longitud total bloque = 80 mm)
        // 1) escala 1.0 -> 80 mm
        // 2) escala 2.0 -> 160 mm
        // Total esperado: 240 mm
        String dxf = 
            "0\nSECTION\n2\nBLOCKS\n" +
            "0\nBLOCK\n" +
            "2\nBLOCK_PIEZA\n" +
            "70\n0\n" +
            "10\n0.0\n20\n0.0\n30\n0.0\n" +
            "0\nLINE\n" +
            "8\n0\n" +
            "10\n0.0\n20\n0.0\n30\n0.0\n" +
            "11\n50.0\n21\n0.0\n31\n0.0\n" +
            "0\nLINE\n" +
            "8\n0\n" +
            "10\n50.0\n20\n0.0\n30\n0.0\n" +
            "11\n50.0\n21\n30.0\n31\n0.0\n" +
            "0\nENDBLK\n" +
            "0\nENDSEC\n" +
            "0\nSECTION\n2\nENTITIES\n" +
            "0\nINSERT\n" +
            "2\nBLOCK_PIEZA\n" +
            "8\n0\n" +
            "10\n0.0\n20\n0.0\n30\n0.0\n" +
            "41\n1.0\n" + // ScaleX = 1.0
            "0\nINSERT\n" +
            "2\nBLOCK_PIEZA\n" +
            "8\n0\n" +
            "10\n100.0\n20\n100.0\n30\n0.0\n" +
            "41\n2.0\n" + // ScaleX = 2.0
            "0\nENDSEC\n0\nEOF\n";

        DxfAnalysis analysis = DxfParser.analyze(new ByteArrayInputStream(dxf.getBytes(StandardCharsets.UTF_8)));

        assertEquals(240.0, analysis.cutLengthMm, EPSILON);
    }

    @Test
    @DisplayName("Debe sumar correctamente círculos y líneas en un rectángulo con agujero central")
    public void testRectangleWithHole() throws Exception {
        // Rectángulo 200 x 100 mm (perímetro = 600 mm) formado por 4 líneas
        // Agujero central circular radio 25 mm (perímetro = 2 * PI * 25 ≈ 157.08 mm)
        // Corte total esperado ≈ 757.08 mm
        String dxf = 
            "0\nSECTION\n2\nENTITIES\n" +
            // Línea 1
            "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n200.0\n21\n0.0\n" +
            // Línea 2
            "0\nLINE\n8\n0\n10\n200.0\n20\n0.0\n11\n200.0\n21\n100.0\n" +
            // Línea 3
            "0\nLINE\n8\n0\n10\n200.0\n20\n100.0\n11\n0.0\n21\n100.0\n" +
            // Línea 4
            "0\nLINE\n8\n0\n10\n0.0\n20\n100.0\n11\n0.0\n21\n0.0\n" +
            // Círculo centro (100, 50), radio 25
            "0\nCIRCLE\n8\n0\n10\n100.0\n20\n50.0\n40\n25.0\n" +
            "0\nENDSEC\n0\nEOF\n";

        DxfAnalysis analysis = DxfParser.analyze(new ByteArrayInputStream(dxf.getBytes(StandardCharsets.UTF_8)));

        assertEquals(200.0, analysis.width, EPSILON);
        assertEquals(100.0, analysis.height, EPSILON);
        double expectedLength = 600.0 + (2.0 * Math.PI * 25.0);
        assertEquals(expectedLength, analysis.cutLengthMm, EPSILON);
    }

}
