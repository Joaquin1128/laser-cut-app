package com.example.lasercut.laser_cut_back.domain.cotizacion.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.lasercut.laser_cut_back.config.MargenConfig;
import com.example.lasercut.laser_cut_back.domain.cotizacion.dto.CotizacionResponse;
import com.example.lasercut.laser_cut_back.domain.catalogo.repository.MaterialRepository;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.shared.util.DxfAnalysis;
import com.example.lasercut.laser_cut_back.shared.util.DxfParser;

@Service
public class CotizacionService {

    private static final Logger logger = LoggerFactory.getLogger(CotizacionService.class);

    private static final String MM = "mm";
    private static final String INCH = "inch";

    private final MaterialRepository materialRepository;
    private final MargenConfig margenConfig;

    @Value("${pricing.factor.densidad:8.0}")
    private double factorDensidad;

    @Value("${pricing.precio.por.kg:5000.0}")
    private double precioPorKg;

    /**
     * Recargo por longitud de corte: factor multiplicado por la longitud en mm
     * da el porcentaje adicional sobre el precio base.
     * Ejemplo con tasa=0.0002: 500mm → +10%, 1000mm → +20%
     */
    @Value("${pricing.corte.tasa.por.mm:0.0002}")
    private double tasaRecargoPorMm;

    /**
     * Porcentaje máximo de recargo por longitud de corte (ej: 0.50 = 50%).
     * Evita que piezas muy complejas se vuelvan prohibitivas.
     */
    @Value("${pricing.corte.recargo.maximo:0.50}")
    private double recargoMaximo;

    public CotizacionService(MaterialRepository materialRepository, MargenConfig margenConfig) {
        this.materialRepository = materialRepository;
        this.margenConfig = margenConfig;
    }

    private void validateInputs(MultipartFile archivo, double espesorMm, String material, int cantidad) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo DXF no puede estar vacío.");
        }
        String name = archivo.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".dxf")) {
            throw new BadRequestException("El archivo debe ser un .dxf.");
        }
        if (espesorMm <= 0) {
            throw new BadRequestException("El espesor debe ser mayor que cero.");
        }
        if (cantidad <= 0) {
            throw new BadRequestException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > 30) {
            throw new BadRequestException("La cantidad no debe ser mayor que treinta.");
        }
        if (material == null) {
            throw new BadRequestException("El material es requerido.");
        }
        if (materialRepository.findByNombreIgnoreCase(material.trim().toLowerCase()).isEmpty()) {
            throw new BadRequestException("Material no soportado");
        }
    }

    private double round(double v, int decimals) {
        BigDecimal bd = new BigDecimal(Double.toString(v));
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public CotizacionResponse calcular(MultipartFile archivo, double espesorMm, String material, int cantidad, String unidad) throws IOException {
        validateInputs(archivo, espesorMm, material, cantidad);

        try (InputStream stream = archivo.getInputStream()) {
            DxfAnalysis analysis = DxfParser.analyze(stream);

            double ancho = analysis.width;
            double alto = analysis.height;
            double longitudCorte = analysis.cutLengthMm;

            if (INCH.equalsIgnoreCase(unidad)) {
                ancho *= 10;
                alto *= 10;
                longitudCorte *= 10;
            } else if (!MM.equalsIgnoreCase(unidad)) {
                throw new BadRequestException("Unidad no soportada. Opciones válidas: 'mm' o 'inch'.");
            }

            // Precio base por área y peso
            double peso = (ancho * alto * espesorMm * factorDensidad) / 1_000_000.0;
            double precioBase = peso * precioPorKg;

            // Recargo por longitud de corte: proporcional al precio base
            double factorRecargo = Math.min(longitudCorte * tasaRecargoPorMm, recargoMaximo);
            double precioFabrica = precioBase * (1.0 + factorRecargo);

            // Margen de la plataforma sobre el precio de fábrica
            double margen = margenConfig.resolverMargen(precioFabrica);
            double precioUnitario = precioFabrica * (1.0 + margen);

            double precioTotal = precioUnitario * cantidad;

            peso = round(peso, 4);
            precioUnitario = round(precioUnitario, 2);
            precioTotal = round(precioTotal, 2);

            logger.info(
                "Cotización: material={}, ancho={}mm, alto={}mm, espesor={}mm, peso={}kg, " +
                "longitudCorte={}mm, factorRecargo={}%, precioFabrica={}, margen={}%, " +
                "precioUnitario={}, cantidad={}, unidad={}, precioTotal={}",
                material, round(ancho, 2), round(alto, 2), round(espesorMm, 2), peso,
                round(longitudCorte, 1), round(factorRecargo * 100, 1),
                round(precioFabrica, 2), round(margen * 100, 1),
                precioUnitario, cantidad, unidad, precioTotal
            );

            CotizacionResponse resp = new CotizacionResponse();
            resp.setMaterial(material);
            resp.setAncho(round(ancho, 2));
            resp.setAlto(round(alto, 2));
            resp.setEspesor(round(espesorMm, 2));
            resp.setPeso(peso);
            resp.setPrecioUnitario(precioUnitario);
            resp.setCantidad(cantidad);
            resp.setUnidad(unidad);
            resp.setPrecioTotal(precioTotal);

            return resp;
        }
    }

}
