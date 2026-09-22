package com.example.lasercut.laser_cut_back;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.lasercut.laser_cut_back.config.MargenConfig;
import com.example.lasercut.laser_cut_back.domain.catalogo.model.Material;
import com.example.lasercut.laser_cut_back.domain.catalogo.repository.MaterialRepository;
import com.example.lasercut.laser_cut_back.domain.cotizacion.dto.CotizacionResponse;
import com.example.lasercut.laser_cut_back.domain.cotizacion.service.CotizacionService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CotizacionServiceTest {

    private CotizacionService cotizacionService;
    private MaterialRepository materialRepository;
    private MargenConfig margenConfig;

    @BeforeEach
    public void setUp() {
        materialRepository = Mockito.mock(MaterialRepository.class);
        Material material = new Material();
        material.setNombre("hierro");
        when(materialRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(material));

        margenConfig = new MargenConfig();
        List<MargenConfig.Tramo> tramos = new ArrayList<>();

        MargenConfig.Tramo t1 = new MargenConfig.Tramo();
        t1.setPrecioHasta(5000);
        t1.setMargen(1.50);
        tramos.add(t1);

        MargenConfig.Tramo t2 = new MargenConfig.Tramo();
        t2.setPrecioHasta(20000);
        t2.setMargen(1.20);
        tramos.add(t2);

        MargenConfig.Tramo t3 = new MargenConfig.Tramo();
        t3.setPrecioHasta(50000);
        t3.setMargen(1.00);
        tramos.add(t3);

        MargenConfig.Tramo t4 = new MargenConfig.Tramo();
        t4.setPrecioHasta(9999999);
        t4.setMargen(0.70);
        tramos.add(t4);

        margenConfig.setTramos(tramos);

        cotizacionService = new CotizacionService(materialRepository, margenConfig);
        ReflectionTestUtils.setField(cotizacionService, "factorDensidad", 8.0);
        ReflectionTestUtils.setField(cotizacionService, "precioPorKg", 5000.0);
        ReflectionTestUtils.setField(cotizacionService, "tasaRecargoPorMm", 0.0002);
        ReflectionTestUtils.setField(cotizacionService, "recargoMaximo", 0.50);
    }

    private MockMultipartFile crearDxfMock(String content) {
        return new MockMultipartFile(
            "archivo",
            "test_pieza.dxf",
            "application/dxf",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Debe cotizar correctamente una pieza en milímetros (mm)")
    public void testCotizacionEnMilimetros() throws Exception {
        // Línea de 100 x 50 mm
        String dxf = 
            "0\nSECTION\n2\nENTITIES\n" +
            "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n50.0\n" +
            "0\nENDSEC\n0\nEOF\n";

        MockMultipartFile file = crearDxfMock(dxf);
        CotizacionResponse resp = cotizacionService.calcular(file, 2.0, "hierro", 1, "mm");

        assertNotNull(resp);
        assertEquals(100.0, resp.getAncho(), 0.1);
        assertEquals(50.0, resp.getAlto(), 0.1);
        // peso = (100 * 50 * 2 * 8) / 1_000_000 = 80_000 / 1_000_000 = 0.08 kg
        assertEquals(0.08, resp.getPeso(), 0.001);
        assertTrue(resp.getPrecioUnitario() > 0);
        assertTrue(resp.getPrecioTotal() > 0);
    }

    @Test
    @DisplayName("Debe convertir correctamente dimensiones y corte con factor 25.4 al cotizar en pulgadas (inch)")
    public void testCotizacionEnPulgadas() throws Exception {
        // En el DXF las medidas son 10" x 5" (numéricamente 10.0 x 5.0)
        String dxf = 
            "0\nSECTION\n2\nENTITIES\n" +
            "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n10.0\n21\n5.0\n" +
            "0\nENDSEC\n0\nEOF\n";

        MockMultipartFile file = crearDxfMock(dxf);
        CotizacionResponse resp = cotizacionService.calcular(file, 2.0, "hierro", 1, "inch");

        assertNotNull(resp);
        // Ancho en mm: 10 * 25.4 = 254.0 mm
        assertEquals(254.0, resp.getAncho(), 0.1);
        // Alto en mm: 5 * 25.4 = 127.0 mm
        assertEquals(127.0, resp.getAlto(), 0.1);

        // peso = (254.0 * 127.0 * 2 * 8) / 1_000_000 ≈ 0.5161 kg
        assertEquals(0.5161, resp.getPeso(), 0.005);
        assertTrue(resp.getPrecioUnitario() > 0);
    }

}

